import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Generador de tests UNIT / INTEGRATION para EduSync.
 *
 * Uso:
 *   java GenerarTest.java --clase backend/src/main/java/.../CrearEstudianteService.java
 *   java GenerarTest.java --clase ... --tipo integration --escribir
 *   java GenerarTest.java --clase ... --tipo unit --escribir --run
 *
 * Por defecto: tipo=unit, solo analiza. --escribir llama al LLM (temperature=0).
 * Proveedores .env: ollama | open-webui | openai | gemini.
 */
public class GenerarTest {

    private static final String SYSTEM_PROMPT_UNIT = """
            Eres un generador de tests JUnit 5 UNITARIOS para EduSync (Java 25, Spring Boot 4.1.0,
            arquitectura hexagonal, Mockito + AssertJ).
            REGLAS:
            - Devuelve SOLO codigo Java (clase de test completa), sin markdown ni explicaciones.
            - Nombres de metodo en camelCase descriptivo; nunca test1/testCaso.
            - NO dupliques ningun metodo de la lista EXISTENTES (mismo nombre = prohibido).
            - Solo genera escenarios de la lista FALTANTES (o equivalentes claros).
            - Maximo 5 metodos @Test nuevos.
            - Usa Mockito en puertos/repos; no inventes error codes que no esten en el codigo.
            - MUST NOT floor() en motor generico; MUST NOT PII real; MUST NOT reimplementar formulas.
            - Incluye @Tag("agente") en la clase.
            """;

    private static final String SYSTEM_PROMPT_INTEGRATION = """
            Eres un generador de tests de INTEGRACION para EduSync (Java 25, Spring Boot 4.1.0).
            Patron OBLIGATORIO del proyecto (copiar estructura de los *IntegrationTest existentes):
            - @SpringBootTest(webEnvironment = RANDOM_PORT)
            - @AutoConfigureTestRestTemplate
            - @Testcontainers
            - PostgreSQLContainer("postgres:15") + @DynamicPropertySource
            - TestRestTemplate + login JWT (seed sysadmin) + crear tenant/admin si hace falta
            - AssertJ sobre status HTTP y body (ErrorResponse.codigo() cuando aplique)
            - Package del modulo (ej. com.edusync.academico), NO subpaquete application
            REGLAS:
            - Devuelve SOLO codigo Java completo, sin markdown.
            - NO dupliques metodos @Test de EXISTENTES.
            - Solo escenarios FALTANTES. Maximo 3 metodos @Test nuevos.
            - NO uses MockMvc/Mockito para el flujo HTTP; es integracion real.
            - NO inventes endpoints ni error codes; usa los del Controller/DTOs del contexto.
            - PII sintetica (RUDE inventado, nombres de prueba). Incluye @Tag("agente").
            - Si el archivo ya existe, igual puedes devolver clase completa con SOLO metodos nuevos
              (+ helpers privados nuevos SOLO si no existen ya con ese nombre).
            - PROHIBIDO convertir helpers privados existentes (crearTenantYAutenticarAdmin, autenticarComo,
              crearGestion, crearCurso, crearParalelo, etc.) en metodos @Test.
            - Un @Test nuevo NO debe llamarse igual que un helper privado del archivo.
            """;

    private static final Pattern TEST_METHOD =
            Pattern.compile("@Test\\b[\\s\\S]*?\\bvoid\\s+(\\w+)\\s*\\(", Pattern.MULTILINE);
    private static final Pattern PUBLIC_METHOD =
            Pattern.compile("\\bpublic\\s+[\\w.<>,\\[\\]\\s]+\\s+(\\w+)\\s*\\(", Pattern.MULTILINE);
    private static final Pattern BLOQUE_CODIGO =
            Pattern.compile("```(?:java)?\\r?\\n(.*?)```", Pattern.DOTALL);
    private static final Pattern LINEA_PACKAGE =
            Pattern.compile("^\\s*package\\s+[\\w.]+\\s*;\\s*$", Pattern.MULTILINE);

    private enum TipoTest {
        UNIT, INTEGRATION;

        static TipoTest parse(String s) throws Fallo {
            if (s == null || s.isBlank()) {
                return UNIT;
            }
            return switch (s.trim().toLowerCase(Locale.ROOT)) {
                case "unit", "unidad", "u" -> UNIT;
                case "integration", "integracion", "it", "i" -> INTEGRATION;
                default -> throw new Fallo("--tipo invalido: " + s
                        + " (usa unit | integration)");
            };
        }
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Fallo f) {
            System.err.println("error: " + f.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("error inesperado: " + e);
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run(String[] args) throws Exception {
        Args a = Args.parse(args);
        Path raiz = raizRepo();
        if (a.clase == null || a.clase.isBlank()) {
            throw new Fallo("falta --clase <ruta-a-*.java>\n"
                    + "ejemplo unit: java GenerarTest.java --clase "
                    + "backend/src/main/java/com/edusync/.../CrearEstudianteService.java\n"
                    + "ejemplo IT:   java GenerarTest.java --tipo integration --clase "
                    + "backend/src/main/java/com/edusync/.../EstudianteController.java --escribir");
        }

        TipoTest tipo = a.tipo;
        Path clase = resolver(raiz, a.clase, "--clase");
        Path salida = a.salida != null
                ? dentroDeRaiz(raiz, a.salida, "--salida")
                : testPathPara(clase, tipo);

        if (a.runOnly) {
            if (!Files.exists(salida)) {
                throw new Fallo("no existe " + rel(raiz, salida) + " — genera antes con --escribir");
            }
            ejecutarMaven(raiz, nombreSinExtension(salida));
            return;
        }

        List<Path> testsExistentes = descubrirTests(raiz, clase, salida, tipo);
        List<Path> contexto = new ArrayList<>();
        for (String c : a.contexto) {
            contexto.add(resolver(raiz, c, "--contexto"));
        }
        if (tipo == TipoTest.INTEGRATION) {
            Path ctrl = encontrarController(raiz, clase);
            if (ctrl != null && !contexto.contains(ctrl)) {
                contexto.add(0, ctrl);
            }
            // Un IT hermano como referencia de estilo (si existe otro)
            Path refIt = encontrarIntegrationDeReferencia(raiz, salida);
            if (refIt != null && !testsExistentes.contains(refIt) && !contexto.contains(refIt)) {
                contexto.add(refIt);
            }
        }
        String tarea = resolverTarea(raiz, a.tarea, tipo);

        String claseSrc = Files.readString(clase, StandardCharsets.UTF_8);
        Set<String> metodosExistentes = new LinkedHashSet<>();
        for (Path t : testsExistentes) {
            metodosExistentes.addAll(extraerTestMethods(Files.readString(t, StandardCharsets.UTF_8)));
        }
        List<String> publicMethods = extraerPublicMethods(claseSrc);
        List<String> faltantes = inferirFaltantes(tipo, tarea, claseSrc, publicMethods, metodosExistentes);

        imprimirAnalisis(raiz, tipo, clase, salida, testsExistentes, metodosExistentes, faltantes);

        if (faltantes.isEmpty()) {
            System.out.println("Nada que generar: los escenarios basicos ya estan cubiertos "
                    + "o no hay huecos claros. Ajusta --tarea si quieres otro foco.");
            return;
        }
        if (!a.escribir) {
            System.out.println();
            System.out.println("Modo analisis (no se escribio nada). Para generar:");
            System.out.println("  java GenerarTest.java --tipo " + tipo.name().toLowerCase(Locale.ROOT)
                    + " --clase " + rel(raiz, clase) + " --escribir");
            return;
        }

        String codigoNuevo = llamarYExtraer(raiz, tipo, clase, contexto, testsExistentes, salida,
                tarea, metodosExistentes, faltantes);
        Set<String> nombresReservados = new LinkedHashSet<>(metodosExistentes);
        if (Files.exists(salida)) {
            nombresReservados.addAll(extraerMetodosDeclarados(
                    Files.readString(salida, StandardCharsets.UTF_8)));
        }
        List<String> generados = extraerTestMethods(codigoNuevo);
        List<String> duplicados = new ArrayList<>();
        List<String> nuevos = new ArrayList<>();
        for (String m : generados) {
            if (nombresReservados.contains(m)) {
                duplicados.add(m);
            } else {
                nuevos.add(m);
            }
        }
        if (!duplicados.isEmpty()) {
            System.out.println("aviso: el modelo propuso duplicados/helpers (se omiten): "
                    + String.join(", ", duplicados));
        }
        if (nuevos.isEmpty()) {
            System.out.println("el modelo no aporto metodos nuevos utiles; no se modifica nada.");
            return;
        }

        if (Files.exists(salida)) {
            String actual = Files.readString(salida, StandardCharsets.UTF_8);
            String fusion = fusionarMetodos(actual, codigoNuevo, nombresReservados);
            Files.writeString(salida, fusion, StandardCharsets.UTF_8);
            System.out.println("actualizado (solo metodos nuevos): " + rel(raiz, salida));
        } else {
            Files.createDirectories(salida.getParent());
            String limpio = quitarMetodos(codigoNuevo, new LinkedHashSet<>(duplicados));
            if (!limpio.contains("@Tag")) {
                limpio = insertarTrasImports(limpio, "@Tag(\"agente\")\n");
                if (!limpio.contains("import org.junit.jupiter.api.Tag;")) {
                    limpio = insertarImports(limpio, List.of("import org.junit.jupiter.api.Tag;"));
                }
            }
            Files.writeString(salida, limpio, StandardCharsets.UTF_8);
            System.out.println("creado: " + rel(raiz, salida));
        }
        System.out.println("metodos agregados: " + String.join(", ", nuevos));
        System.out.println("HITL: revisa el archivo. Verde != correcto. La IA no aprueba.");
        if (tipo == TipoTest.INTEGRATION) {
            System.out.println("nota: integration requiere Docker (Testcontainers PostgreSQL 15).");
        }

        if (a.run) {
            ejecutarMaven(raiz, nombreSinExtension(salida));
        } else {
            System.out.println("para ejecutar: java GenerarTest.java --tipo "
                    + tipo.name().toLowerCase(Locale.ROOT)
                    + " --clase " + rel(raiz, clase) + " --run-only");
            System.out.println("  o: cd backend && mvn.cmd -Dtest="
                    + nombreSinExtension(salida) + " test");
        }
    }

    // ----------------------------------------------------------- analisis

    private static void imprimirAnalisis(Path raiz, TipoTest tipo, Path clase, Path salida,
                                         List<Path> tests, Set<String> metodos,
                                         List<String> faltantes) {
        System.out.println("========== ANALISIS ==========");
        System.out.println("Tipo:      " + tipo);
        System.out.println("Clase:     " + rel(raiz, clase));
        System.out.println("Salida:    " + rel(raiz, salida)
                + (Files.exists(salida) ? " (existe)" : " (nuevo)"));
        System.out.println("Tests leidos (" + tests.size() + "):");
        if (tests.isEmpty()) {
            System.out.println("  (ninguno — se creara el archivo de salida)");
        } else {
            for (Path t : tests) {
                System.out.println("  - " + rel(raiz, t));
            }
        }
        System.out.println("Metodos @Test existentes (" + metodos.size() + "):");
        if (metodos.isEmpty()) {
            System.out.println("  (ninguno)");
        } else {
            for (String m : metodos) {
                System.out.println("  - " + m);
            }
        }
        System.out.println("Escenarios faltantes propuestos (" + faltantes.size() + "):");
        for (int i = 0; i < faltantes.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + faltantes.get(i));
        }
        System.out.println("==============================");
    }

    private static List<Path> descubrirTests(Path raiz, Path clase, Path salida, TipoTest tipo)
            throws IOException {
        Set<Path> out = new LinkedHashSet<>();
        if (Files.exists(salida)) {
            out.add(salida);
        }
        Path testJavaRoot = raiz.resolve("backend/src/test/java");
        if (!Files.isDirectory(testJavaRoot)) {
            return new ArrayList<>(out);
        }
        String simple = nombreSinExtension(clase);
        String stem = stemAgregado(simple);

        if (tipo == TipoTest.INTEGRATION) {
            try (Stream<Path> walk = Files.walk(testJavaRoot)) {
                walk.filter(p -> p.getFileName().toString().endsWith("IntegrationTest.java"))
                        .forEach(p -> {
                            String name = p.getFileName().toString();
                            if (name.equals(stem + "IntegrationTest.java")
                                    || name.equals(simple + "IntegrationTest.java")
                                    || name.contains(stem)) {
                                out.add(p);
                            }
                        });
            }
            return new ArrayList<>(out);
        }

        try (Stream<Path> walk = Files.walk(testJavaRoot)) {
            walk.filter(p -> p.toString().endsWith("Test.java"))
                    .filter(p -> !p.getFileName().toString().contains("Integration"))
                    .forEach(p -> {
                        String name = p.getFileName().toString();
                        if (name.equals(simple + "Test.java")
                                || name.equals(simple + "AgenteTest.java")) {
                            out.add(p);
                            return;
                        }
                        try {
                            String src = Files.readString(p, StandardCharsets.UTF_8);
                            if (src.contains(simple)
                                    && (name.startsWith(simple)
                                    || src.contains("new " + simple)
                                    || src.contains(simple + "("))) {
                                out.add(p);
                            }
                        } catch (IOException ignored) {
                            // skip
                        }
                    });
        }
        String path = clase.toString().replace('\\', '/');
        if (path.contains("/application/service/") && simple.endsWith("Service") && !stem.isBlank()) {
            Path domainGuess = guessDomainTest(testJavaRoot, clase, stem);
            if (domainGuess != null && Files.exists(domainGuess)) {
                out.add(domainGuess);
            }
        }
        return new ArrayList<>(out);
    }

    private static Path encontrarController(Path raiz, Path clase) throws IOException {
        String stem = stemAgregado(nombreSinExtension(clase));
        if (stem.isBlank()) {
            return null;
        }
        Path mainJava = raiz.resolve("backend/src/main/java");
        if (!Files.isDirectory(mainJava)) {
            return null;
        }
        String want = stem + "Controller.java";
        try (Stream<Path> walk = Files.walk(mainJava)) {
            return walk.filter(p -> p.getFileName().toString().equals(want))
                    .findFirst()
                    .orElse(null);
        }
    }

    /** Otro *IntegrationTest del mismo modulo como plantilla de estilo (max 1). */
    private static Path encontrarIntegrationDeReferencia(Path raiz, Path salidaObjetivo)
            throws IOException {
        Path testJavaRoot = raiz.resolve("backend/src/test/java");
        if (!Files.isDirectory(testJavaRoot)) {
            return null;
        }
        Path modulo = salidaObjetivo.getParent();
        if (modulo == null || !Files.isDirectory(modulo)) {
            return null;
        }
        try (Stream<Path> list = Files.list(modulo)) {
            return list.filter(p -> p.getFileName().toString().endsWith("IntegrationTest.java"))
                    .filter(p -> !p.equals(salidaObjetivo))
                    .findFirst()
                    .orElse(null);
        }
    }

    /** CrearEstudianteService / EstudianteController / Estudiante → Estudiante */
    private static String stemAgregado(String simple) {
        String s = simple;
        s = s.replaceFirst("Controller$", "");
        s = s.replaceFirst("Service$", "");
        s = s.replaceFirst("^(Crear|Listar|Obtener|Actualizar|Cambiar|Upsert|Registrar)", "");
        return s;
    }

    private static Path guessDomainTest(Path testJavaRoot, Path clase, String stem) {
        Path pkg = clase.getParent();
        if (pkg == null || pkg.getParent() == null || pkg.getParent().getParent() == null) {
            return null;
        }
        Path module = pkg.getParent().getParent();
        String moduleName = module.getFileName().toString();
        return testJavaRoot.resolve("com/edusync/" + moduleName + "/domain/" + stem + "Test.java");
    }

    private static Path testPathPara(Path claseMain, TipoTest tipo) throws Fallo {
        String s = claseMain.toAbsolutePath().normalize().toString().replace('\\', '/');
        String marker = "/src/main/java/";
        int idx = s.indexOf(marker);
        if (idx < 0) {
            throw new Fallo("--clase debe estar bajo backend/src/main/java/...");
        }
        String prefix = s.substring(0, idx);
        String rest = s.substring(idx + marker.length());
        if (!rest.endsWith(".java")) {
            throw new Fallo("--clase debe ser un .java");
        }
        String withoutJava = rest.substring(0, rest.length() - 5);
        if (tipo == TipoTest.UNIT) {
            return Path.of(prefix + "/src/test/java/" + withoutJava + "Test.java");
        }
        // Integration: com/edusync/{modulo}/{Stem}IntegrationTest.java
        String[] parts = withoutJava.split("/");
        if (parts.length < 3 || !parts[0].equals("com") || !parts[1].equals("edusync")) {
            throw new Fallo("no se pudo resolver modulo para integration desde " + withoutJava);
        }
        String modulo = parts[2]; // academico | identidad | plataforma | shared
        String simple = parts[parts.length - 1];
        String stem = stemAgregado(simple);
        if (stem.isBlank()) {
            stem = simple;
        }
        return Path.of(prefix + "/src/test/java/com/edusync/" + modulo + "/"
                + stem + "IntegrationTest.java");
    }

    private static List<String> inferirFaltantes(TipoTest tipo, String tarea, String claseSrc,
                                                   List<String> methods,
                                                   Set<String> existing) {
        Set<String> out = new LinkedHashSet<>();
        for (String line : tarea.split("\\R")) {
            String t = line.strip();
            if (t.startsWith("-") || t.startsWith("*") || t.matches("^\\d+[.)].*")) {
                t = t.replaceFirst("^[-*\\d.)]+\\s*", "").strip();
                if (t.length() > 3 && !cubre(existing, t)) {
                    out.add(t);
                }
            }
        }
        String lowerSrc = claseSrc.toLowerCase(Locale.ROOT);

        if (tipo == TipoTest.INTEGRATION) {
            if (!cubre(existing, "aislamiento") && !cubre(existing, "crosstenant")
                    && !cubre(existing, "otrotenant") && !cubre(existing, "404")) {
                out.add("aislamiento cross-tenant (404 al acceder recurso de otro tenant)");
            }
            if ((lowerSrc.contains("duplic") || lowerSrc.contains("conflict")
                    || lowerSrc.contains("409") || lowerSrc.contains("unique"))
                    && !cubre(existing, "duplic") && !cubre(existing, "409")
                    && !cubre(existing, "conflict")) {
                out.add("HTTP 409 conflicto de negocio (ej. RUDE/codigo duplicado)");
            }
            if (!cubre(existing, "401") && !cubre(existing, "403")
                    && !cubre(existing, "unauthorized") && !cubre(existing, "sinauth")) {
                out.add("rechazo sin JWT o con rol insuficiente (401/403)");
            }
            if (existing.isEmpty() || (!cubre(existing, "alta") && !cubre(existing, "crear")
                    && !cubre(existing, "post") && !cubre(existing, "exito"))) {
                out.add("happy path HTTP: crear/listar recurso y validar status + body");
            }
            out.removeIf(s -> cubre(existing, s));
            List<String> list = new ArrayList<>(out);
            if (list.size() > 3) {
                return list.subList(0, 3);
            }
            return list;
        }

        if ((lowerSrc.contains("duplic") || lowerSrc.contains("existepor") || lowerSrc.contains("unique"))
                && !cubre(existing, "duplic") && !cubre(existing, "yaexiste")
                && !cubre(existing, "409")) {
            out.add("rechazo por duplicado / ya existe");
        }
        if (!cubre(existing, "null") && !cubre(existing, "nulo")) {
            out.add("entrada nula en dependencia critica (si aplica al codigo real)");
        }
        if ((lowerSrc.contains("repository") || lowerSrc.contains("port"))
                && !cubre(existing, "falla") && !cubre(existing, "failure")
                && !cubre(existing, "guardar") && !cubre(existing, "repositor")) {
            out.add("fallo del puerto/repositorio al persistir");
        }
        for (String m : methods) {
            if (Set.of("equals", "hashCode", "toString", "main").contains(m)) {
                continue;
            }
            if (!cubre(existing, m) && !cubre(existing, "crear") && m.equals("crear")) {
                if (!cubre(existing, "cuando") && !cubre(existing, "exito")
                        && !cubre(existing, "unico") && existing.stream().noneMatch(
                        e -> e.toLowerCase(Locale.ROOT).contains("crea"))) {
                    out.add("happy path del metodo " + m);
                }
            }
        }
        out.removeIf(s -> cubre(existing, s));
        List<String> list = new ArrayList<>(out);
        if (list.size() > 5) {
            return list.subList(0, 5);
        }
        return list;
    }

    private static boolean cubre(Set<String> existing, String hint) {
        String h = hint.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (h.isBlank()) {
            return false;
        }
        for (String e : existing) {
            String n = e.toLowerCase(Locale.ROOT);
            if (n.contains(h) || h.contains(n.replaceAll("[^a-z0-9]", ""))) {
                return true;
            }
            // tokens
            for (String tok : hint.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
                if (tok.length() >= 4 && n.contains(tok)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ----------------------------------------------------------- LLM + merge

    private static String llamarYExtraer(Path raiz, TipoTest tipo, Path clase, List<Path> contexto,
                                         List<Path> tests, Path salida, String tarea,
                                         Set<String> existentes, List<String> faltantes)
            throws Exception {
        StringBuilder ctx = new StringBuilder();
        ctx.append(bloque("CLASE", clase));
        for (Path p : contexto) {
            ctx.append("\n\n").append(bloque("CONTEXTO", p));
        }
        for (Path p : tests) {
            ctx.append("\n\n").append(bloque("TEST EXISTENTE (no duplicar metodos)", p));
        }
        ctx.append("\n\nEXISTENTES:\n");
        for (String m : existentes) {
            ctx.append("- ").append(m).append('\n');
        }
        ctx.append("\nFALTANTES (generar solo estos, sin inventar error codes):\n");
        for (String f : faltantes) {
            ctx.append("- ").append(f).append('\n');
        }

        String paquete = paqueteDesdeRuta(salida);
        String nombre = nombreSinExtension(salida);
        String user = "TIPO: " + tipo + "\nPACKAGE: " + paquete + "\nCLASE TEST: " + nombre + "\n\n"
                + ctx + "\n\nTAREA:\n" + tarea
                + "\n\nSi el archivo de test ya existe, igual devuelve una clase COMPLETA "
                + "con SOLO los metodos nuevos (pueden incluir helpers privados si hacen falta).";

        String system = tipo == TipoTest.INTEGRATION
                ? SYSTEM_PROMPT_INTEGRATION : SYSTEM_PROMPT_UNIT;
        Config cfg = Config.leer(raiz);
        System.out.println("LLM -> " + cfg.resumen() + " [" + tipo + "]");
        long t0 = System.currentTimeMillis();
        LlmRespuesta resp = llamarModelo(cfg, system, user);
        System.out.printf("ok en %.1fs (tokens in=%s out=%s)%n",
                (System.currentTimeMillis() - t0) / 1000.0,
                resp.promptTokens == null ? "?" : resp.promptTokens,
                resp.completionTokens == null ? "?" : resp.completionTokens);
        return extraerCodigo(resp.contenido);
    }

    /**
     * Inserta en el test existente solo metodos @Test cuyo nombre no exista.
     */
    private static String fusionarMetodos(String actual, String generado, Set<String> ya)
            throws Fallo {
        List<String> blocks = extraerBloquesTest(generado);
        StringBuilder inject = new StringBuilder();
        for (String block : blocks) {
            Matcher m = Pattern.compile("\\bvoid\\s+(\\w+)\\s*\\(").matcher(block);
            if (!m.find()) {
                continue;
            }
            String name = m.group(1);
            if (ya.contains(name)) {
                continue;
            }
            inject.append("\n").append(block.strip()).append("\n");
            ya.add(name);
        }
        if (inject.isEmpty()) {
            throw new Fallo("no quedaron metodos nuevos tras filtrar duplicados");
        }
        if (!actual.contains("@Tag(\"agente\")") && !actual.contains("@Tag(\"auditado\")")) {
            // mark file touched by agent once
            actual = insertarTrasImports(actual, "@Tag(\"agente\")\n");
            if (!actual.contains("import org.junit.jupiter.api.Tag;")) {
                actual = insertarImports(actual, List.of("import org.junit.jupiter.api.Tag;"));
            }
        }
        int brace = actual.lastIndexOf('}');
        if (brace < 0) {
            throw new Fallo("archivo de test sin '}' de cierre");
        }
        return actual.substring(0, brace) + inject + "}\n";
    }

    private static List<String> extraerBloquesTest(String src) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile(
                "((?:@[\\w.(,)=\\s\"]+\\s*)*@Test\\b[\\s\\S]*?\\n\\s*\\})",
                Pattern.MULTILINE).matcher(src);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    private static String quitarMetodos(String src, Set<String> names) {
        if (names.isEmpty()) {
            return src;
        }
        String result = src;
        for (String name : names) {
            result = result.replaceAll(
                    "(?:@[\\w.(,)=\\s\"]+\\s*)*@Test\\b[\\s\\S]*?\\bvoid\\s+"
                            + Pattern.quote(name) + "\\s*\\([\\s\\S]*?\\n\\s*\\}\\s*",
                    "");
        }
        return result;
    }

    private static void ejecutarMaven(Path raiz, String testClass) throws Exception {
        String mvn = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                ? "mvn.cmd" : "mvn";
        ProcessBuilder pb = new ProcessBuilder(mvn, "-q", "-Dtest=" + testClass, "test");
        pb.directory(raiz.resolve("backend").toFile());
        pb.redirectErrorStream(true);
        System.out.println("ejecutando: " + mvn + " -Dtest=" + testClass + " test");
        Process p = pb.start();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        int code = p.waitFor();
        System.out.println(code == 0 ? "mvn: OK" : "mvn: FALLO (exit=" + code + ")");
        System.out.println("Recuerda: PASSED != aprobado por humano.");
        if (code != 0) {
            System.exit(code);
        }
    }

    // ----------------------------------------------------------- helpers

    /** Todos los metodos void/typed del archivo (incluye helpers privados). */
    private static Set<String> extraerMetodosDeclarados(String src) {
        Set<String> names = new LinkedHashSet<>();
        Matcher m = Pattern.compile(
                "^\\s*(?:public|private|protected)?\\s*(?:static\\s+)?"
                        + "(?:void|[A-Z][\\w.<>,\\[\\]\\s]*)\\s+(\\w+)\\s*\\(",
                Pattern.MULTILINE).matcher(src);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static List<String> extraerTestMethods(String src) {
        List<String> names = new ArrayList<>();
        Matcher m = TEST_METHOD.matcher(src);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static List<String> extraerPublicMethods(String src) {
        List<String> names = new ArrayList<>();
        Matcher m = PUBLIC_METHOD.matcher(src);
        String className = "";
        Matcher cm = Pattern.compile("\\b(?:class|record)\\s+(\\w+)").matcher(src);
        if (cm.find()) {
            className = cm.group(1);
        }
        while (m.find()) {
            String name = m.group(1);
            if (!name.equals(className)) {
                names.add(name);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(names));
    }

    private static String bloque(String etiqueta, Path ruta) throws IOException {
        return "### " + etiqueta + ": " + ruta.getFileName()
                + "\n```java\n" + Files.readString(ruta, StandardCharsets.UTF_8) + "\n```";
    }

    private static String resolverTarea(Path raiz, String tarea, TipoTest tipo) throws IOException {
        if (tarea == null || tarea.isBlank()) {
            if (tipo == TipoTest.INTEGRATION) {
                return "Genera solo tests de integracion HTTP faltantes "
                        + "(happy path, error de negocio 4xx, aislamiento tenant). "
                        + "No dupliques metodos existentes. No inventes endpoints ni codes.";
            }
            return "Genera solo tests unitarios faltantes (negativos / borde). "
                    + "No dupliques metodos existentes. No inventes excepciones ni codes.";
        }
        boolean pareceRuta = !tarea.contains("\n") && !tarea.contains("\r")
                && (tarea.endsWith(".md") || tarea.endsWith(".txt")
                || tarea.contains("/") || tarea.contains("\\"));
        if (pareceRuta) {
            try {
                Path p = raiz.resolve(tarea).normalize();
                if (p.startsWith(raiz) && Files.isRegularFile(p)) {
                    return Files.readString(p, StandardCharsets.UTF_8);
                }
            } catch (java.nio.file.InvalidPathException ignored) {
                // literal
            }
        }
        return tarea;
    }

    private static Path raizRepo() {
        Path cwd = Path.of("").toAbsolutePath();
        Path c = cwd;
        for (int i = 0; i < 6 && c != null; i++) {
            if (Files.exists(c.resolve("AGENTS.md"))) {
                return c;
            }
            c = c.getParent();
        }
        Path conv = cwd.getParent() != null ? cwd.getParent().getParent() : null;
        if (conv != null && Files.exists(conv.resolve("AGENTS.md"))) {
            return conv;
        }
        return cwd;
    }

    private static Path dentroDeRaiz(Path raiz, String dado, String flag) throws Fallo {
        Path r = raiz.resolve(dado).normalize();
        if (!r.startsWith(raiz)) {
            throw new Fallo(flag + " sale de la raiz del repo");
        }
        return r;
    }

    private static Path resolver(Path raiz, String dado, String flag) throws Fallo {
        Path r = dentroDeRaiz(raiz, dado, flag);
        if (!Files.exists(r)) {
            throw new Fallo(flag + " no encontrado: " + r);
        }
        return r;
    }

    private static String rel(Path raiz, Path p) {
        return raiz.relativize(p.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    private static String nombreSinExtension(Path p) {
        String n = p.getFileName().toString();
        int i = n.lastIndexOf('.');
        return i < 0 ? n : n.substring(0, i);
    }

    private static String paqueteDesdeRuta(Path salida) {
        List<String> partes = new ArrayList<>();
        for (Path p : salida.getParent()) {
            partes.add(p.toString());
        }
        int idx = partes.indexOf("java");
        if (idx < 0 || idx + 1 >= partes.size()) {
            return "";
        }
        return String.join(".", partes.subList(idx + 1, partes.size()));
    }

    private static String extraerCodigo(String respuesta) {
        Matcher m = BLOQUE_CODIGO.matcher(respuesta);
        String codigo = (m.find() ? m.group(1) : respuesta).strip() + "\n";
        List<String> imports = new ArrayList<>();
        if (codigo.contains("@Test") && !codigo.contains("import org.junit.jupiter.api.Test;")) {
            imports.add("import org.junit.jupiter.api.Test;");
        }
        if (codigo.contains("@Tag(") && !codigo.contains("import org.junit.jupiter.api.Tag;")) {
            imports.add("import org.junit.jupiter.api.Tag;");
        }
        if ((codigo.contains("Mockito.") || codigo.contains("when(") || codigo.contains("mock("))
                && !codigo.contains("import org.mockito")
                && !codigo.contains("import static org.mockito")) {
            imports.add("import static org.mockito.Mockito.*;");
        }
        if (!imports.isEmpty()) {
            codigo = insertarImports(codigo, imports);
        }
        return codigo;
    }

    private static String insertarImports(String codigo, List<String> imports) {
        String bloque = String.join("\n", imports) + "\n";
        Matcher pm = LINEA_PACKAGE.matcher(codigo);
        if (pm.find()) {
            int pos = pm.end();
            return codigo.substring(0, pos) + "\n\n" + bloque + codigo.substring(pos).stripLeading();
        }
        return bloque + codigo;
    }

    private static String insertarTrasImports(String codigo, String anotacion) {
        // insert before "class " or "public class"
        Matcher m = Pattern.compile("(?m)^((?:public\\s+)?(?:final\\s+)?class\\s+)").matcher(codigo);
        if (m.find()) {
            return codigo.substring(0, m.start()) + anotacion + codigo.substring(m.start());
        }
        return anotacion + codigo;
    }

    // -------------------------------------------------------------- config / LLM / JSON

    private record LlmRespuesta(String contenido, Long promptTokens, Long completionTokens) {
    }

    private static LlmRespuesta llamarModelo(Config cfg, String systemMsg, String userMsg)
            throws IOException, InterruptedException, Fallo {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", cfg.modelo);
        body.put("temperature", 0);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemMsg),
                Map.of("role", "user", "content", userMsg)));
        String payload = Json.escribir(body);
        HttpClient client = HttpClient.newBuilder().connectTimeout(cfg.timeout).build();
        int maxIntentos = 4;
        for (int intento = 1; intento <= maxIntentos; intento++) {
            HttpRequest.Builder reqB = HttpRequest.newBuilder()
                    .uri(URI.create(cfg.baseUrl + "/chat/completions"))
                    .timeout(cfg.timeout)
                    .header("Content-Type", "application/json; charset=utf-8");
            // Ollama local no exige Bearer; Open WebUI, OpenAI y Gemini sí.
            if (cfg.apiKey != null && !cfg.apiKey.isBlank() && !cfg.apiKey.equals("ollama")) {
                reqB.header("Authorization", "Bearer " + cfg.apiKey);
            }
            HttpResponse<String> resp = client.send(
                    reqB.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = resp.statusCode();
            if (code / 100 == 2) {
                return parsearChatCompletion(resp.body());
            }
            boolean reintable = code == 429 || code == 503;
            if (reintable && intento < maxIntentos) {
                long esperaMs = 2000L * intento * intento;
                System.out.printf("aviso: HTTP %d (intento %d/%d), reintento en %ds...%n",
                        code, intento, maxIntentos, esperaMs / 1000);
                Thread.sleep(esperaMs);
                continue;
            }
            throw new Fallo("HTTP " + code + ": " + resp.body());
        }
        throw new Fallo("sin respuesta del LLM tras reintentos");
    }

    private static LlmRespuesta parsearChatCompletion(String bodyJson) throws Fallo {
        Map<?, ?> raiz = (Map<?, ?>) Json.leer(bodyJson);
        List<?> choices = (List<?>) raiz.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new Fallo("respuesta sin choices");
        }
        Map<?, ?> msg = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        Long pin = null;
        Long pout = null;
        if (raiz.get("usage") instanceof Map<?, ?> u) {
            if (u.get("prompt_tokens") instanceof Number n) {
                pin = n.longValue();
            }
            if (u.get("completion_tokens") instanceof Number n) {
                pout = n.longValue();
            }
        }
        return new LlmRespuesta(String.valueOf(msg.get("content")), pin, pout);
    }

    private static final class Config {
        final String proveedor;
        final String baseUrl;
        final String apiKey;
        final String modelo;
        final Duration timeout;

        Config(String proveedor, String baseUrl, String apiKey, String modelo, Duration timeout) {
            this.proveedor = proveedor;
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.modelo = modelo;
            this.timeout = timeout;
        }

        static Config leer(Path raiz) throws IOException, Fallo {
            Map<String, String> env = leerDotenv(raiz.resolve(".env"));
            String proveedor = valor(env, "EDUSYNC_AI_PROVIDER", "ollama").toLowerCase(Locale.ROOT);
            if (proveedor.equals("openai") || proveedor.equals("chatgpt")) {
                // API oficial ChatGPT / OpenAI (compatible: base + /chat/completions).
                String base = valor(env, "OPENAI_BASE_URL", "https://api.openai.com/v1");
                String apiKey = valor(env, "OPENAI_API_KEY", "");
                if (apiKey.isBlank()) {
                    throw new Fallo("EDUSYNC_AI_PROVIDER=openai requiere OPENAI_API_KEY "
                            + "(en .env o variable de entorno; nunca commitear la clave)");
                }
                return new Config("openai", trimSlash(base),
                        apiKey,
                        valor(env, "OPENAI_MODEL", "gpt-4o-mini"),
                        Duration.ofSeconds(Long.parseLong(
                                valor(env, "OPENAI_TIMEOUT_SECONDS", "120"))));
            }
            if (proveedor.equals("gemini") || proveedor.equals("google")
                    || proveedor.equals("google-ai")) {
                // Gemini via endpoint OpenAI-compat (mismo /chat/completions que openai).
                // Docs: https://ai.google.dev/gemini-api/docs/openai
                String base = valor(env, "GEMINI_BASE_URL",
                        "https://generativelanguage.googleapis.com/v1beta/openai");
                String apiKey = valor(env, "GEMINI_API_KEY", "");
                if (apiKey.isBlank()) {
                    throw new Fallo("EDUSYNC_AI_PROVIDER=gemini requiere GEMINI_API_KEY "
                            + "(Google AI Studio; nunca commitear la clave)");
                }
                return new Config("gemini", trimSlash(base),
                        apiKey,
                        valor(env, "GEMINI_MODEL", "gemini-3.8-flash"),
                        Duration.ofSeconds(Long.parseLong(
                                valor(env, "GEMINI_TIMEOUT_SECONDS", "120"))));
            }
            if (proveedor.equals("open-webui")) {
                String base = valor(env, "OPEN_WEBUI_BASE_URL", "http://localhost:3000");
                return new Config(proveedor, trimSlash(base) + "/api",
                        valor(env, "OPEN_WEBUI_API_KEY", "sk-local"),
                        valor(env, "OPEN_WEBUI_MODEL", "llama3.1:latest"),
                        Duration.ofSeconds(Long.parseLong(
                                valor(env, "OPEN_WEBUI_TIMEOUT_SECONDS", "300"))));
            }
            // ollama (default) u otro nombre → mismo endpoint OpenAI-compat local
            return new Config(proveedor.isBlank() ? "ollama" : proveedor,
                    trimSlash(valor(env, "OLLAMA_BASE_URL", "http://localhost:11434")) + "/v1",
                    "ollama",
                    valor(env, "OLLAMA_MODEL", "llama3.1:latest"),
                    Duration.ofSeconds(Long.parseLong(
                            valor(env, "OLLAMA_TIMEOUT_SECONDS", "300"))));
        }

        String resumen() {
            return proveedor + " / " + modelo + " @ " + baseUrl;
        }

        private static String trimSlash(String s) {
            return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
        }

        private static String valor(Map<String, String> env, String k, String d) {
            String v = System.getenv(k);
            if (v != null && !v.isBlank()) {
                return v;
            }
            return env.getOrDefault(k, d);
        }

        private static Map<String, String> leerDotenv(Path archivo) throws IOException {
            Map<String, String> out = new LinkedHashMap<>();
            if (!Files.exists(archivo)) {
                return out;
            }
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String l = linea.strip();
                if (l.isEmpty() || l.startsWith("#")) {
                    continue;
                }
                int eq = l.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String val = l.substring(eq + 1).strip();
                if (val.length() >= 2 && ((val.startsWith("\"") && val.endsWith("\""))
                        || (val.startsWith("'") && val.endsWith("'")))) {
                    val = val.substring(1, val.length() - 1);
                }
                out.put(l.substring(0, eq).strip(), val);
            }
            return out;
        }
    }

    private static final class Args {
        String clase;
        String salida;
        String tarea;
        List<String> contexto = new ArrayList<>();
        TipoTest tipo = TipoTest.UNIT;
        boolean escribir;
        boolean run;
        boolean runOnly;

        static Args parse(String[] argv) throws Fallo {
            Args a = new Args();
            for (int i = 0; i < argv.length; i++) {
                String f = argv[i];
                switch (f) {
                    case "--escribir", "--write" -> {
                        a.escribir = true;
                        continue;
                    }
                    case "--run" -> {
                        a.run = true;
                        continue;
                    }
                    case "--run-only" -> {
                        a.runOnly = true;
                        continue;
                    }
                    case "--help", "-h" -> {
                        System.out.println("""
                                Uso:
                                  java GenerarTest.java --clase <src/main/.../Foo.java>
                                  java GenerarTest.java --tipo unit --clase ... --escribir
                                  java GenerarTest.java --tipo integration --clase ... --escribir

                                Flags:
                                  --clase       clase bajo prueba (Service/Controller)
                                  --tipo        unit (default) | integration
                                  --tarea       prompt opcional
                                  --contexto    archivo extra (repetible)
                                  --salida      override del archivo de test
                                  --escribir    llama LLM y agrega tests no duplicados
                                  --run         tras escribir, corre mvn -Dtest=...
                                  --run-only    solo ejecuta el test espejo
                                """);
                        System.exit(0);
                    }
                    default -> {
                    }
                }
                if (i + 1 >= argv.length) {
                    throw new Fallo("falta valor de " + f);
                }
                String v = argv[++i];
                switch (f) {
                    case "--clase" -> a.clase = v;
                    case "--salida" -> a.salida = v;
                    case "--tarea" -> a.tarea = v;
                    case "--contexto" -> a.contexto.add(v);
                    case "--tipo", "--layer" -> a.tipo = TipoTest.parse(v);
                    default -> throw new Fallo("flag desconocido: " + f + " (usa --help)");
                }
            }
            if (a.runOnly) {
                a.run = true;
                a.escribir = false;
            }
            return a;
        }
    }

    private static final class Fallo extends Exception {
        Fallo(String msg) {
            super(msg);
        }
    }

    private static final class Json {
        static String escribir(Object o) {
            StringBuilder sb = new StringBuilder();
            escribirValor(o, sb);
            return sb.toString();
        }

        @SuppressWarnings("unchecked")
        private static void escribirValor(Object o, StringBuilder sb) {
            if (o == null) {
                sb.append("null");
            } else if (o instanceof String s) {
                escribirString(s, sb);
            } else if (o instanceof Number || o instanceof Boolean) {
                sb.append(o);
            } else if (o instanceof Map<?, ?> m) {
                sb.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    escribirString(String.valueOf(e.getKey()), sb);
                    sb.append(':');
                    escribirValor(e.getValue(), sb);
                }
                sb.append('}');
            } else if (o instanceof List<?> l) {
                sb.append('[');
                for (int i = 0; i < l.size(); i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    escribirValor(l.get(i), sb);
                }
                sb.append(']');
            } else {
                escribirString(String.valueOf(o), sb);
            }
        }

        private static void escribirString(String s, StringBuilder sb) {
            sb.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> {
                        if (c < 0x20) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                    }
                }
            }
            sb.append('"');
        }

        static Object leer(String texto) throws Fallo {
            return new Parser(texto).valor();
        }

        private static final class Parser {
            private final String s;
            private int i;

            Parser(String s) {
                this.s = s;
            }

            Object valor() throws Fallo {
                saltar();
                char c = s.charAt(i);
                return switch (c) {
                    case '{' -> objeto();
                    case '[' -> arreglo();
                    case '"' -> string();
                    case 't', 'f' -> bool();
                    case 'n' -> {
                        if (!s.startsWith("null", i)) {
                            throw new Fallo("JSON null");
                        }
                        i += 4;
                        yield null;
                    }
                    default -> numero();
                };
            }

            Map<String, Object> objeto() throws Fallo {
                Map<String, Object> m = new LinkedHashMap<>();
                esperar('{');
                saltar();
                if (mirar() == '}') {
                    i++;
                    return m;
                }
                while (true) {
                    String k = string();
                    saltar();
                    esperar(':');
                    m.put(k, valor());
                    saltar();
                    if (mirar() == ',') {
                        i++;
                    } else if (mirar() == '}') {
                        i++;
                        break;
                    } else {
                        throw new Fallo("JSON objeto");
                    }
                }
                return m;
            }

            List<Object> arreglo() throws Fallo {
                List<Object> l = new ArrayList<>();
                esperar('[');
                saltar();
                if (mirar() == ']') {
                    i++;
                    return l;
                }
                while (true) {
                    l.add(valor());
                    saltar();
                    if (mirar() == ',') {
                        i++;
                    } else if (mirar() == ']') {
                        i++;
                        break;
                    } else {
                        throw new Fallo("JSON array");
                    }
                }
                return l;
            }

            String string() throws Fallo {
                esperar('"');
                StringBuilder sb = new StringBuilder();
                while (i < s.length()) {
                    char c = s.charAt(i++);
                    if (c == '"') {
                        return sb.toString();
                    }
                    if (c == '\\') {
                        char e = s.charAt(i++);
                        sb.append(switch (e) {
                            case '"', '\\', '/' -> e;
                            case 'n' -> '\n';
                            case 'r' -> '\r';
                            case 't' -> '\t';
                            case 'b' -> '\b';
                            case 'f' -> '\f';
                            case 'u' -> {
                                String hex = s.substring(i, i + 4);
                                i += 4;
                                yield (char) Integer.parseInt(hex, 16);
                            }
                            default -> throw new Fallo("escape");
                        });
                    } else {
                        sb.append(c);
                    }
                }
                throw new Fallo("string abierto");
            }

            Boolean bool() throws Fallo {
                if (s.startsWith("true", i)) {
                    i += 4;
                    return true;
                }
                if (s.startsWith("false", i)) {
                    i += 5;
                    return false;
                }
                throw new Fallo("bool");
            }

            Number numero() throws Fallo {
                int start = i;
                while (i < s.length() && "-+.eE0123456789".indexOf(s.charAt(i)) >= 0) {
                    i++;
                }
                String n = s.substring(start, i);
                if (n.contains(".") || n.contains("e") || n.contains("E")) {
                    return Double.parseDouble(n);
                }
                return Long.parseLong(n);
            }

            void esperar(char c) throws Fallo {
                if (i >= s.length() || s.charAt(i) != c) {
                    throw new Fallo("se esperaba " + c);
                }
                i++;
            }

            char mirar() throws Fallo {
                saltar();
                if (i >= s.length()) {
                    throw new Fallo("EOF");
                }
                return s.charAt(i);
            }

            void saltar() {
                while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                    i++;
                }
            }
        }
    }
}
