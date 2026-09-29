# Plan E2E — EduSync (sección ADMIN demo / tenant)

> Alcance: login ADMIN + consolas Usuarios / académico. No SYSADMIN/Tenants.
> No verificar textos de LLM. Datos de escritura con timestamp único.

## Resumen

Usuario demo con rol ADMIN: redirect a `/usuarios`. Nav: Usuarios, Cursos,
Gestión Escolar, Materias, Estudiantes, Profesores. Sin enlace Tenants.

## 1. Login ADMIN

### 1.1 Camino feliz ADMIN
**Pasos:** Abrir /login → credenciales E2E_ADMIN_* → Iniciar sesión
**Resultado esperado:** URL `/usuarios`; heading Usuarios; sin enlace Tenants

### 1.2 Nav de consolas ADMIN
**Pasos:** Login ADMIN → observar barra de navegación
**Resultado esperado:** enlaces Usuarios, Cursos, Gestión Escolar, Materias, Estudiantes, Profesores visibles

## 2. Listas

### 2.1 Gestión Escolar
**Pasos:** Login ADMIN → clic Gestión Escolar
**Resultado esperado:** URL gestiones-escolares; heading; enlace + Nueva Gestión Escolar

### 2.2 Cursos
**Pasos:** Login ADMIN → clic Cursos
**Resultado esperado:** heading Cursos; enlace + Nuevo Curso

### 2.3 Estudiantes
**Pasos:** Login ADMIN → clic Estudiantes
**Resultado esperado:** heading Estudiantes; enlace + Nuevo Estudiante

## 3. Escrituras

### 3.1 Alta de Curso
**Pasos:** Login ADMIN → Cursos → + Nuevo Curso → Nombre único → Crear Curso
**Resultado esperado:** vuelve a lista; el nombre aparece en pantalla

### 3.2 Alta de Estudiante
**Pasos:** Login ADMIN → Estudiantes → + Nuevo → RUDE único + nombre → Crear
**Resultado esperado:** detalle del estudiante (heading con nombre, texto RUDE, enlace Volver a Estudiantes)

### 3.3 (borde) Nuevo Usuario sin rol
**Pasos:** Login ADMIN → + Nuevo Usuario
**Resultado esperado:** formulario visible; botón Crear Usuario deshabilitado sin roles
