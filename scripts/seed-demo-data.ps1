<#
.SYNOPSIS
  Puebla EduSync (API local) con datos de prueba multi-tenant.

.DESCRIPTION
  Crea (o reutiliza) dos tenants con gestion escolar, cursos, paralelos,
  materias (>=5 por curso), profesores (cada uno con >=1 materia) y
  estudiantes (>=5 inscritos por curso). Usa la API REST para respetar
  hashing de passwords, seeds de periodos/secciones y RLS.

  Requisitos:
    - PostgreSQL levantado (infra/docker-compose.yml)
    - Backend en http://localhost:8080 (mvn spring-boot:run)
    - SYSADMIN seed activo (sysadmin@edusync.local / changeme_local_dev)

.PARAMETER BaseUrl
  URL base de la API. Default: http://localhost:8080

.PARAMETER Password
  Password inicial compartida para admin/secretaria/profesores (min 8 chars).
  Default de desarrollo local: secreto123

.PARAMETER Force
  Si ya hay cursos en el tenant, vuelve a crear el set academico (puede duplicar
  materias/estudiantes). Por defecto omite el seed academico si ya hay cursos.

.EXAMPLE
  .\scripts\seed-demo-data.ps1
  .\scripts\seed-demo-data.ps1 -BaseUrl http://localhost:8080 -Force
#>
[CmdletBinding()]
param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$Password = "secreto123",
  [string]$SysAdminEmail = $env:SYSADMIN_EMAIL,
  [string]$SysAdminPassword = $env:SYSADMIN_PASSWORD,
  [switch]$Force
)

$ErrorActionPreference = "Stop"

if (-not $SysAdminEmail) { $SysAdminEmail = "sysadmin@edusync.local" }
if (-not $SysAdminPassword) { $SysAdminPassword = "changeme_local_dev" }

function Write-Step([string]$Message) {
  Write-Host "`n==> $Message" -ForegroundColor Cyan
}

function Invoke-Api {
  param(
    [Parameter(Mandatory)][ValidateSet("GET", "POST", "PUT", "PATCH")][string]$Method,
    [Parameter(Mandatory)][string]$Path,
    [hashtable]$Headers = @{},
    $Body = $null
  )
  $uri = "$BaseUrl$Path"
  $params = @{
    Method      = $Method
    Uri         = $uri
    Headers     = $Headers
    ContentType = "application/json"
  }
  if ($null -ne $Body) {
    $params.Body = ($Body | ConvertTo-Json -Depth 8 -Compress)
  }
  try {
    return Invoke-RestMethod @params
  }
  catch {
    $status = $null
    $detail = $_.Exception.Message
    if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
      $detail = $_.ErrorDetails.Message
    }
    if ($_.Exception.Response) {
      $status = [int]$_.Exception.Response.StatusCode
    }
    throw "HTTP $status $Method $Path failed: $detail"
  }
}

function Get-AuthHeaders([string]$Email, [string]$Pass) {
  $login = Invoke-Api -Method POST -Path "/api/v1/auth/login" -Body @{
    email    = $Email
    password = $Pass
  }
  return @{ Authorization = "Bearer $($login.accessToken)" }
}

function Try-Login([string]$Email, [string]$Pass) {
  try {
    return Get-AuthHeaders -Email $Email -Pass $Pass
  }
  catch {
    return $null
  }
}

function Ensure-Tenant {
  param(
    [hashtable]$SysHeaders,
    [string]$Nombre,
    [string]$AdminNombre,
    [string]$AdminEmail,
    [string]$Password
  )

  $existing = Try-Login -Email $AdminEmail -Pass $Password
  if ($existing) {
    Write-Host "  Tenant '$Nombre' ya existe (login admin OK). Reutilizando." -ForegroundColor Yellow
    return $existing
  }

  Write-Host "  Creando tenant '$Nombre'..."
  $tenant = Invoke-Api -Method POST -Path "/api/v1/plataforma/tenants" -Headers $SysHeaders -Body @{
    nombre                       = $Nombre
    fechaInicioSuscripcion       = "2026-01-01"
    fechaVencimientoSuscripcion  = "2026-12-31"
  }

  Invoke-Api -Method POST -Path "/api/v1/plataforma/tenants/$($tenant.id)/admins" -Headers $SysHeaders -Body @{
    nombreCompleto = $AdminNombre
    email          = $AdminEmail
    password       = $Password
  } | Out-Null

  return Get-AuthHeaders -Email $AdminEmail -Pass $Password
}

function Ensure-Secretaria {
  param(
    [hashtable]$AdminHeaders,
    [string]$Nombre,
    [string]$Email,
    [string]$Password
  )
  $login = Try-Login -Email $Email -Pass $Password
  if ($login) {
    Write-Host "  Secretaria $Email ya existe."
    return
  }
  Invoke-Api -Method POST -Path "/api/v1/usuarios" -Headers $AdminHeaders -Body @{
    nombreCompleto   = $Nombre
    email            = $Email
    passwordInicial  = $Password
    roles            = @("SECRETARIA")
  } | Out-Null
  Write-Host "  Secretaria creada: $Email"
}

function New-Gestion {
  param([hashtable]$Headers, [string]$Nombre)
  return Invoke-Api -Method POST -Path "/api/v1/gestiones-escolares" -Headers $Headers -Body @{
    nombre      = $Nombre
    fechaInicio = "2026-02-02"
    fechaFin    = "2026-11-27"
  }
}

function New-CursoConParalelo {
  param([hashtable]$Headers, [string]$CursoNombre, [string]$ParaleloNombre = "A")
  $curso = Invoke-Api -Method POST -Path "/api/v1/cursos" -Headers $Headers -Body @{
    nombre = $CursoNombre
  }
  $paralelo = Invoke-Api -Method POST -Path "/api/v1/cursos/$($curso.id)/paralelos" -Headers $Headers -Body @{
    nombre = $ParaleloNombre
  }
  return [pscustomobject]@{
    CursoId    = $curso.id
    CursoNombre = $CursoNombre
    ParaleloId = $paralelo.id
  }
}

function New-Profesor {
  param(
    [hashtable]$Headers,
    [string]$Nombre,
    [string]$Email,
    [string]$Password
  )
  $login = Try-Login -Email $Email -Pass $Password
  if ($login) {
    # ProfesorResponse no expone email; buscar por usuarios del tenant.
    $q = [uri]::EscapeDataString($Email)
    $page = Invoke-Api -Method GET -Path "/api/v1/usuarios?q=$q&size=20" -Headers $Headers
    $found = @($page.content) | Where-Object { $_.email -eq $Email } | Select-Object -First 1
    if (-not $found) { throw "Profesor $Email existe en auth pero no en /usuarios" }
    return $found.id
  }
  $user = Invoke-Api -Method POST -Path "/api/v1/usuarios" -Headers $Headers -Body @{
    nombreCompleto  = $Nombre
    email           = $Email
    passwordInicial = $Password
    roles           = @("PROFESOR")
  }
  return $user.id
}

function New-MateriaAsignada {
  param(
    [hashtable]$Headers,
    [string]$Nombre,
    [string]$CursoId,
    [string]$ParaleloId,
    [string]$ProfesorId
  )
  $materia = Invoke-Api -Method POST -Path "/api/v1/materias" -Headers $Headers -Body @{
    nombre = $Nombre
  }
  Invoke-Api -Method POST -Path "/api/v1/materias/$($materia.id)/asignaciones-curso" -Headers $Headers -Body @{
    cursoId    = $CursoId
    paraleloId = $ParaleloId
  } | Out-Null
  Invoke-Api -Method POST -Path "/api/v1/materias/$($materia.id)/asignaciones-profesor" -Headers $Headers -Body @{
    profesorId = $ProfesorId
    cursoId    = $CursoId
    paraleloId = $ParaleloId
  } | Out-Null
  return $materia.id
}

function New-EstudianteInscrito {
  param(
    [hashtable]$Headers,
    [string]$Rude,
    [string]$Nombre,
    [string]$GestionId,
    [string]$CursoId,
    [string]$ParaleloId
  )
  $est = Invoke-Api -Method POST -Path "/api/v1/estudiantes" -Headers $Headers -Body @{
    rude           = $Rude
    nombreCompleto = $Nombre
    estado         = "ACTIVO"
  }
  Invoke-Api -Method POST -Path "/api/v1/inscripciones" -Headers $Headers -Body @{
    estudianteId     = $est.id
    gestionEscolarId = $GestionId
    cursoId          = $CursoId
    paraleloId       = $ParaleloId
    fechaInscripcion = "2026-02-10"
  } | Out-Null
  return $est.id
}

function Seed-TenantAcademico {
  param(
    [hashtable]$AdminHeaders,
    [string]$TenantLabel,
    [string]$GestionNombre,
    [object[]]$Profesores,
    [object[]]$CursosConEstudiantes,
    [string[]]$MateriasBase,
    [string]$Password,
    [string]$SecretariaNombre,
    [string]$SecretariaEmail
  )

  Write-Step "Seed academico: $TenantLabel"

  Ensure-Secretaria -AdminHeaders $AdminHeaders -Nombre $SecretariaNombre -Email $SecretariaEmail -Password $Password

  $cursosExistentes = Invoke-Api -Method GET -Path "/api/v1/cursos?size=5" -Headers $AdminHeaders
  $totalCursos = 0
  if ($cursosExistentes.totalElements) { $totalCursos = [int]$cursosExistentes.totalElements }
  elseif ($cursosExistentes.content) { $totalCursos = @($cursosExistentes.content).Count }

  if ($totalCursos -gt 0 -and -not $Force) {
    Write-Host "  Ya hay $totalCursos curso(s). Omite seed academico (usa -Force para forzar)." -ForegroundColor Yellow
    return
  }

  $gestion = New-Gestion -Headers $AdminHeaders -Nombre $GestionNombre
  Write-Host "  Gestion: $($gestion.nombre) ($($gestion.id))"

  $profIds = @{}
  foreach ($p in $Profesores) {
    $id = New-Profesor -Headers $AdminHeaders -Nombre $p.Nombre -Email $p.Email -Password $Password
    $profIds[$p.Email] = $id
    Write-Host "  Profesor: $($p.Nombre) <$($p.Email)>"
  }

  $profEmails = @($Profesores | ForEach-Object { $_.Email })
  $mi = 0

  foreach ($bloque in $CursosConEstudiantes) {
    $cp = New-CursoConParalelo -Headers $AdminHeaders -CursoNombre $bloque.Curso -ParaleloNombre "A"
    Write-Host "  Curso: $($bloque.Curso) A"

    foreach ($materiaNombre in $MateriasBase) {
      $emailProf = $profEmails[$mi % $profEmails.Count]
      $mi++
      New-MateriaAsignada `
        -Headers $AdminHeaders `
        -Nombre "$materiaNombre ($($bloque.Curso))" `
        -CursoId $cp.CursoId `
        -ParaleloId $cp.ParaleloId `
        -ProfesorId $profIds[$emailProf] | Out-Null
    }
    Write-Host "    $($MateriasBase.Count) materias asignadas"

    foreach ($est in $bloque.Estudiantes) {
      New-EstudianteInscrito `
        -Headers $AdminHeaders `
        -Rude $est.Rude `
        -Nombre $est.Nombre `
        -GestionId $gestion.id `
        -CursoId $cp.CursoId `
        -ParaleloId $cp.ParaleloId | Out-Null
    }
    Write-Host "    $($bloque.Estudiantes.Count) estudiantes inscritos"
  }
}

# --- datos ---

$materias = @(
  "Matematica",
  "Lenguaje y Comunicacion",
  "Ciencias Naturales",
  "Ciencias Sociales",
  "Educacion Fisica"
)

$demoProfesores = @(
  @{ Nombre = "Maria Elena Quispe";   Email = "maria.quispe@uedemo.edu.bo" }
  @{ Nombre = "Juan Pablo Mamani";    Email = "juan.mamani@uedemo.edu.bo" }
  @{ Nombre = "Ana Lucia Condori";    Email = "ana.condori@uedemo.edu.bo" }
  @{ Nombre = "Roberto Carlos Flores"; Email = "roberto.flores@uedemo.edu.bo" }
  @{ Nombre = "Carmen Rosa Rojas";    Email = "carmen.rojas@uedemo.edu.bo" }
)

$demoCursos = @(
  @{
    Curso = "Primero de Primaria"
    Estudiantes = @(
      @{ Rude = "2026001001"; Nombre = "Sofia Camila Vargas" }
      @{ Rude = "2026001002"; Nombre = "Mateo Andres Choque" }
      @{ Rude = "2026001003"; Nombre = "Valentina Isabel Peña" }
      @{ Rude = "2026001004"; Nombre = "Diego Alejandro Torrez" }
      @{ Rude = "2026001005"; Nombre = "Camila Fernanda Apaza" }
    )
  }
  @{
    Curso = "Segundo de Primaria"
    Estudiantes = @(
      @{ Rude = "2026002001"; Nombre = "Lucas Gabriel Mendoza" }
      @{ Rude = "2026002002"; Nombre = "Martina Alejandra Soto" }
      @{ Rude = "2026002003"; Nombre = "Nicolas Esteban Quisbert" }
      @{ Rude = "2026002004"; Nombre = "Emma Patricia Flores" }
      @{ Rude = "2026002005"; Nombre = "Thiago Mauricio Nina" }
    )
  }
  @{
    Curso = "Primero de Secundaria"
    Estudiantes = @(
      @{ Rude = "2026003001"; Nombre = "Adrian Jose Callisaya" }
      @{ Rude = "2026003002"; Nombre = "Lucia Beatriz Mamani" }
      @{ Rude = "2026003003"; Nombre = "Sebastian Raul Aguilar" }
      @{ Rude = "2026003004"; Nombre = "Isabella Maria Gutierrez" }
      @{ Rude = "2026003005"; Nombre = "Bruno Enrique Paredes" }
      @{ Rude = "2026003006"; Nombre = "Antonella Paz Rivero" }
    )
  }
)

$auxProfesores = @(
  @{ Nombre = "Pedro Antonio Silva";   Email = "pedro.silva@mariaaux.edu.bo" }
  @{ Nombre = "Laura Jimena Ortega";   Email = "laura.ortega@mariaaux.edu.bo" }
  @{ Nombre = "Miguel Angel Huanca";   Email = "miguel.huanca@mariaaux.edu.bo" }
  @{ Nombre = "Daniela Ruth Castro";   Email = "daniela.castro@mariaaux.edu.bo" }
  @{ Nombre = "Fernando Javier Loza";  Email = "fernando.loza@mariaaux.edu.bo" }
)

$auxCursos = @(
  @{
    Curso = "Tercero de Primaria"
    Estudiantes = @(
      @{ Rude = "2026101001"; Nombre = "Renata Olivia Miranda" }
      @{ Rude = "2026101002"; Nombre = "Joaquin Elias Ramos" }
      @{ Rude = "2026101003"; Nombre = "Paula Andrea Cordova" }
      @{ Rude = "2026101004"; Nombre = "Emiliano Jose Vargas" }
      @{ Rude = "2026101005"; Nombre = "Catalina Luz Herrera" }
    )
  }
  @{
    Curso = "Cuarto de Primaria"
    Estudiantes = @(
      @{ Rude = "2026102001"; Nombre = "Tomas Ignacio Ballivián" }
      @{ Rude = "2026102002"; Nombre = "Amanda Celeste Rocha" }
      @{ Rude = "2026102003"; Nombre = "Benjamin Omar Soruco" }
      @{ Rude = "2026102004"; Nombre = "Julieta Natalia Paz" }
      @{ Rude = "2026102005"; Nombre = "Santiago Ivan Mercado" }
    )
  }
)

# --- ejecucion ---

Write-Step "Comprobando API en $BaseUrl"
try {
  Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -UseBasicParsing -TimeoutSec 5 | Out-Null
}
catch {
  # actuator puede no estar expuesto; probar login endpoint con fallo esperado
  try {
    Invoke-WebRequest -Uri "$BaseUrl/v3/api-docs" -UseBasicParsing -TimeoutSec 5 | Out-Null
  }
  catch {
    Write-Host "No se pudo contactar $BaseUrl. Levanta el backend (mvn spring-boot:run) y reintenta." -ForegroundColor Red
    exit 1
  }
}

Write-Step "Login SYSADMIN ($SysAdminEmail)"
$sysHeaders = Get-AuthHeaders -Email $SysAdminEmail -Pass $SysAdminPassword

Write-Step "Tenant Demo"
$demoAdmin = Ensure-Tenant `
  -SysHeaders $sysHeaders `
  -Nombre "Unidad Educativa Demo San Jose" `
  -AdminNombre "Carlos Mendoza" `
  -AdminEmail "admin@uedemo.edu.bo" `
  -Password $Password

Seed-TenantAcademico `
  -AdminHeaders $demoAdmin `
  -TenantLabel "UE Demo San Jose" `
  -GestionNombre "Gestion 2026" `
  -Profesores $demoProfesores `
  -CursosConEstudiantes $demoCursos `
  -MateriasBase $materias `
  -Password $Password `
  -SecretariaNombre "Patricia Vargas" `
  -SecretariaEmail "secretaria@uedemo.edu.bo"

Write-Step "Tenant contraste (aislamiento RLS)"
$auxAdmin = Ensure-Tenant `
  -SysHeaders $sysHeaders `
  -Nombre "Colegio Maria Auxiliadora" `
  -AdminNombre "Luis Fernando Aguilar" `
  -AdminEmail "admin@mariaaux.edu.bo" `
  -Password $Password

Seed-TenantAcademico `
  -AdminHeaders $auxAdmin `
  -TenantLabel "Colegio Maria Auxiliadora" `
  -GestionNombre "Gestion 2026" `
  -Profesores $auxProfesores `
  -CursosConEstudiantes $auxCursos `
  -MateriasBase $materias `
  -Password $Password `
  -SecretariaNombre "Rosa Elena Mercado" `
  -SecretariaEmail "secretaria@mariaaux.edu.bo"

Write-Host ""
Write-Host "Seed completado." -ForegroundColor Green
Write-Host ""
Write-Host "Credenciales de prueba (password: $Password)" -ForegroundColor Green
Write-Host "--------------------------------------------" -ForegroundColor Green
Write-Host "SYSADMIN     $SysAdminEmail / (SYSADMIN_PASSWORD o changeme_local_dev)"
Write-Host ""
Write-Host "Tenant Demo - Unidad Educativa Demo San Jose"
Write-Host "  ADMIN        admin@uedemo.edu.bo"
Write-Host "  SECRETARIA   secretaria@uedemo.edu.bo"
Write-Host "  PROFESOR     maria.quispe@uedemo.edu.bo   (y 4 mas @uedemo.edu.bo)"
Write-Host ""
Write-Host "Tenant contraste - Colegio Maria Auxiliadora"
Write-Host "  ADMIN        admin@mariaaux.edu.bo"
Write-Host "  SECRETARIA   secretaria@mariaaux.edu.bo"
Write-Host "  PROFESOR     pedro.silva@mariaaux.edu.bo  (y 4 mas @mariaaux.edu.bo)"
Write-Host ""
Write-Host "Flujos sugeridos:"
Write-Host "  - Login ADMIN demo -> Cursos / Materias / Estudiantes / Profesores"
Write-Host "  - Login PROFESOR   -> Mis cursos / planilla"
Write-Host "  - Login ADMIN otro tenant -> verificar que NO ve datos del demo (RLS)"
