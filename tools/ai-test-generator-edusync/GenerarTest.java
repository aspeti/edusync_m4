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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GenerarTest.java -- agente generador de tests UNIT para EduSync (JUnit 5).
 *
 * Fase 1 (HITL): AI generates, human audits.
 *
 *   1) --proponer   analiza alcance y escribe sesion PROPOSED (sin LLM, sin escribir tests)
 *   2) --generar --aprobar-alcance [--sesion id]
 *                   llama al LLM (temperature=0) y escribe *Test.java con @Tag("agente")
 *   3) --ejecutar-tests --sesion id
 *                   corre mvn -Dtest=... y actualiza PASSED/FAILED (la IA NO aprueba)
 *   4) --decidir --sesion id --veredicto APPROVE|REJECT|MODIFIED [--nota ...]
 *                   decision humana obligatoria
 *
 * Reutiliza .env / OLLAMA_* / OPEN_WEBUI_* (misma config que shared.ai / ADR-0017).
 * JDK puro (JEP 330): {@code java GenerarTest.java ...} desde tools/ai-test-generator-edusync/.
 */
public class GenerarTest {

    private static final String LAYER = "UNIT";
    private static final String SESSIONS_REL = "docs/qa/ai-test-sessions";

    private static final String SYSTEM_PROMPT = """
            Eres un generador de tests JUnit 5 para EduSync (Java 25, Spring Boot 4.1.0,
            arquitectura hexagonal). Reglas OBLIGATORIAS (AGENTS.md):
            - Estilo Google Java Style; nombres de test en camelCase describiendo el
              comportamiento esperado (ej. dividirEntreCeroLanzaExcepcionConMensaje),
              nunca test1/testCasoTres.
            - No dupliques ningun test ya presente en el archivo de test manual entregado
              como contexto.
            - Prioriza: casos limite, negativos, cero, nulos, y las reglas de negocio
              explicitas en la clase de dominio entregada (invariantes, excepciones de
              negocio propias del dominio).
            - MUST NOT calcular promedios/redondeo dentro del propio test: el test siempre
              invoca al motor de dominio real, nunca reimplementa la formula.
            - MUST NOT usar floor() en el motor generico (ADR-0013) ni introducir PII real
              (rude/nombre/fecha de nacimiento reales) - usa valores sinteticos de prueba.
            - Si el metodo bajo prueba depende de un puerto de salida (repositorio, reloj,
              LlmPort), usa un doble de prueba (Mockito) en vez de una implementacion real.
            - Primera linea despues de los imports: @Tag("agente")
            - Devuelve SOLO un bloque de codigo Java completo (package, imports y la clase
              de test entera), sin explicaciones ni texto fuera del bloque de codigo.
            """;

    private static final String TAREA_DEFECTO =
            "Lee las clases de contexto y el/los test(s) manual(es) entregados. Genera SOLO "
            + "los tests que falten (sin duplicar el test manual) para los casos limite, "
            + "negativos y de regla de negocio de la clase principal. Maximo 8 tests nuevos.";

    private static final Pattern TEST_METHOD =
            Pattern.compile("@Test\\b[\\s\\S]*?\\bvoid\\s+(\\w+)\\s*\\(", Pattern.MULTILINE);
    private static final Pattern PUBLIC_METHOD =
            Pattern.compile("\\bpublic\\s+[\\w.<>,\\[\\]\\s]+\\s+(\\w+)\\s*\\(", Pattern.MULTILINE);
    private static final Pattern THROWS_EX =
            Pattern.compile("\\bthrows\\s+([\\w\\s,]+)");

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

        if (a.decidir) {
            decidir(raiz, a);
            return;
        }
        if (a.ejecutarTests) {
            ejecutarTests(raiz, a);
            return;
        }
        if (a.generar) {
            generar(raiz, a);
            return;
        }
        // Default seguro: proponer (nunca escribir tests sin --aprobar-alcance).
        proponer(raiz, a);
    }

    // ============================================================ HITL flows

    private static void proponer(Path raiz, Args a) throws Exception {
        Scope scope = Scope.desdeArgs(raiz, a);
        Proposal proposal = construirPropuesta(raiz, scope);

        String sessionId = a.sesion != null && !a.sesion.isBlank()
                ? a.sesion
                : nuevoSessionId(scope.nombreClaseObjetivo());
        Path sessionPath = sessionFile(raiz, sessionId);

        Map<String, Object> session = new LinkedHashMap<>();
        session.put("id", sessionId);
        session.put("layer", LAYER);
        session.put("status", "PROPOSED");
        session.put("createdAt", ahoraIso());
        session.put("updatedAt", ahoraIso());
        session.put("target", proposal.target);
        session.put("clase", rel(raiz, scope.clase));
        session.put("salida", rel(raiz, scope.salida));
        session.put("contexto", relAll(raiz, scope.contexto));
        session.put("testManual", relAll(raiz, scope.testManual));
        session.put("tarea", scope.tarea);
        session.put("existingTestMethods", proposal.existingTestMethods);
        session.put("existingTestCount", proposal.existingTestCount);
        session.put("salidaExists", proposal.salidaExists);
        session.put("proposedScenarios", proposal.scenarios);
        session.put("publicMethods", proposal.publicMethods);
        session.put("human", mapaHumanoVacio());
        session.put("generation", null);
        session.put("execution", null);
        session.put("note", "La IA NUNCA aprueba. Tras --generar, auditar (skill §4.5) y "
                + "usar --decidir --veredicto APPROVE|REJECT|MODIFIED.");

        escribirSession(sessionPath, session);
        imprimirPropuesta(proposal, sessionId, sessionPath, raiz);
        System.out.println();
        System.out.println("Siguiente paso (si apruebas el alcance):");
        System.out.println("  java GenerarTest.java --generar --aprobar-alcance --sesion " + sessionId);
    }

    private static void generar(Path raiz, Args a) throws Exception {
        if (!a.aprobarAlcance) {
            throw new Fallo("HITL: para escribir tests hace falta --aprobar-alcance. "
                    + "Primero corre sin flags o con --proponer; revisa la propuesta; "
                    + "luego: --generar --aprobar-alcance --sesion <id>");
        }

        Map<String, Object> session;
        Path sessionPath;
        Scope scope;

        if (a.sesion != null && !a.sesion.isBlank()) {
            sessionPath = sessionFile(raiz, a.sesion);
            session = leerSession(sessionPath);
            scope = Scope.desdeSession(raiz, session, a);
        } else {
            scope = Scope.desdeArgs(raiz, a);
            String sessionId = nuevoSessionId(scope.nombreClaseObjetivo());
            sessionPath = sessionFile(raiz, sessionId);
            Proposal proposal = construirPropuesta(raiz, scope);
            session = new LinkedHashMap<>();
            session.put("id", sessionId);
            session.put("layer", LAYER);
            session.put("status", "PROPOSED");
            session.put("createdAt", ahoraIso());
            session.put("target", proposal.target);
            session.put("clase", rel(raiz, scope.clase));
            session.put("salida", rel(raiz, scope.salida));
            session.put("contexto", relAll(raiz, scope.contexto));
            session.put("testManual", relAll(raiz, scope.testManual));
            session.put("tarea", scope.tarea);
            session.put("existingTestMethods", proposal.existingTestMethods);
            session.put("existingTestCount", proposal.existingTestCount);
            session.put("salidaExists", proposal.salidaExists);
            session.put("proposedScenarios", proposal.scenarios);
            session.put("publicMethods", proposal.publicMethods);
            session.put("human", mapaHumanoVacio());
            System.out.println("aviso: generacion sin --sesion previa; se creo sesion "
                    + sessionId + " (mejor: --proponer primero).");
        }

        if (Boolean.TRUE.equals(session.get("salidaExists"))
                && Files.exists(scope.salida)
                && !a.forzar) {
            throw new Fallo("la salida ya existe (" + rel(raiz, scope.salida)
                    + "). HITL: no se sobrescribe en silencio. Usa --forzar solo si "
                    + "confirmaste reemplazar un borrador @Tag(\"agente\"), o cambia --salida.");
        }

        StringBuilder contexto = new StringBuilder();
        contexto.append(bloque("CLASE PRINCIPAL", scope.clase));
        for (Path p : scope.contexto) {
            contexto.append("\n\n").append(bloque("CONTEXTO", p));
        }
        for (Path p : scope.testManual) {
            contexto.append("\n\n").append(bloque("TEST MANUAL (no duplicar)", p));
        }
        @SuppressWarnings("unchecked")
        List<String> escenarios = (List<String>) session.get("proposedScenarios");
        if (escenarios != null && !escenarios.isEmpty()) {
            contexto.append("\n\n### ESCENARIOS PROPUESTOS (priorizar, sin inventar fuera de alcance)\n");
            for (int i = 0; i < escenarios.size(); i++) {
                contexto.append((i + 1)).append(". ").append(escenarios.get(i)).append('\n');
            }
        }

        String paquete = paqueteDesdeRuta(scope.salida);
        String nombreClase = nombreSinExtension(scope.salida);
        String userContent = "PACKAGE de salida: " + paquete + "\n"
                + "NOMBRE de la clase de test: " + nombreClase + "\n\n"
                + "ARCHIVOS DEL PROYECTO:\n\n" + contexto + "\n\nTAREA:\n" + scope.tarea;

        Config cfg = Config.leer(raiz);
        System.out.println("agente -> " + cfg.resumen());
        long aproxTokens = (SYSTEM_PROMPT.length() + userContent.length()) / 4L;
        System.out.printf("contexto: %s + %d archivo(s) + %d test(s) manual(es)  "
                        + "(~%,d tokens estimados)%n",
                scope.clase.getFileName(), scope.contexto.size(), scope.testManual.size(),
                aproxTokens);

        long t0 = System.currentTimeMillis();
        LlmRespuesta resp = llamarModelo(cfg, SYSTEM_PROMPT, userContent);
        double segundos = (System.currentTimeMillis() - t0) / 1000.0;

        String codigo = extraerCodigo(resp.contenido);
        Files.createDirectories(scope.salida.getParent());
        Files.writeString(scope.salida, codigo, StandardCharsets.UTF_8);

        Map<String, Object> generation = new LinkedHashMap<>();
        generation.put("at", ahoraIso());
        generation.put("promptTokens", resp.promptTokens);
        generation.put("completionTokens", resp.completionTokens);
        generation.put("seconds", segundos);
        generation.put("model", cfg.modelo);
        generation.put("provider", cfg.proveedor);
        generation.put("tag", "agente");

        session.put("status", "GENERATED");
        session.put("updatedAt", ahoraIso());
        session.put("generation", generation);
        session.put("salidaExists", true);
        escribirSession(sessionPath, session);

        System.out.println("escrito: " + rel(raiz, scope.salida));
        System.out.printf("tokens: entrada=%s salida=%s  tiempo=%.1fs%n",
                resp.promptTokens == null ? "?" : resp.promptTokens,
                resp.completionTokens == null ? "?" : resp.completionTokens,
                segundos);
        System.out.println("estado sesion: GENERATED (NO aprobado). Auditoria humana obligatoria.");
        System.out.println("Siguiente:");
        System.out.println("  java GenerarTest.java --ejecutar-tests --sesion " + session.get("id"));
        System.out.println("  java GenerarTest.java --decidir --sesion " + session.get("id")
                + " --veredicto APPROVE|REJECT|MODIFIED");
    }

    private static void ejecutarTests(Path raiz, Args a) throws Exception {
        if (a.sesion == null || a.sesion.isBlank()) {
            throw new Fallo("--ejecutar-tests requiere --sesion <id>");
        }
        Path sessionPath = sessionFile(raiz, a.sesion);
        Map<String, Object> session = leerSession(sessionPath);
        String salidaRel = str(session.get("salida"));
        if (salidaRel == null) {
            throw new Fallo("sesion sin 'salida'");
        }
        Path salida = resolver(raiz, salidaRel, "salida");
        if (!Files.exists(salida)) {
            throw new Fallo("no existe el test generado: " + salida
                    + " (corre --generar --aprobar-alcance primero)");
        }
        String nombreClase = nombreSinExtension(salida);
        ProcessBuilder pb = new ProcessBuilder(
                "mvn", "-q", "-Dtest=" + nombreClase, "test");
        pb.directory(raiz.resolve("backend").toFile());
        pb.redirectErrorStream(true);
        System.out.println("ejecutando: cd backend && mvn -Dtest=" + nombreClase + " test");
        Process p = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (out.length() < 20_000) {
                    out.append(line).append('\n');
                }
            }
        }
        int code = p.waitFor();
        boolean passed = code == 0;

        Map<String, Object> execution = new LinkedHashMap<>();
        execution.put("at", ahoraIso());
        execution.put("command", "mvn -Dtest=" + nombreClase + " test");
        execution.put("exitCode", code);
        execution.put("passed", passed);
        execution.put("logTail", out.length() > 4000
                ? out.substring(out.length() - 4000)
                : out.toString());

        session.put("execution", execution);
        session.put("status", passed ? "PASSED" : "FAILED");
        session.put("updatedAt", ahoraIso());
        escribirSession(sessionPath, session);

        System.out.println();
        System.out.println("Resultado ejecucion: " + (passed ? "PASSED" : "FAILED")
                + " (exit=" + code + ")");
        System.out.println("HITL: un test en verde NO implica aprobado. Corre --decidir.");
        System.out.println("  java GenerarTest.java --decidir --sesion " + a.sesion
                + " --veredicto APPROVE|REJECT|MODIFIED");
    }

    private static void decidir(Path raiz, Args a) throws Exception {
        if (a.sesion == null || a.sesion.isBlank()) {
            throw new Fallo("--decidir requiere --sesion <id>");
        }
        if (a.veredicto == null || a.veredicto.isBlank()) {
            throw new Fallo("--decidir requiere --veredicto APPROVE|REJECT|MODIFIED");
        }
        String v = a.veredicto.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("APPROVE", "REJECT", "MODIFIED").contains(v)) {
            throw new Fallo("--veredicto invalido: " + a.veredicto
                    + " (usa APPROVE, REJECT o MODIFIED)");
        }

        Path sessionPath = sessionFile(raiz, a.sesion);
        Map<String, Object> session = leerSession(sessionPath);

        @SuppressWarnings("unchecked")
        Map<String, Object> human = session.get("human") instanceof Map<?, ?>
                ? new LinkedHashMap<>((Map<String, Object>) session.get("human"))
                : mapaHumanoVacio();
        human.put("verdict", v);
        human.put("decidedAt", ahoraIso());
        if (a.nota != null) {
            human.put("note", a.nota);
        }
        human.put("checklistReminder", List.of(
                "¿Se pone en rojo si rompo la función?",
                "¿El assert describe el comportamiento esperado (no una foto)?",
                "¿El nombre del método describe el comportamiento?"
        ));

        String status = switch (v) {
            case "APPROVE" -> "APPROVED";
            case "REJECT" -> "REJECTED";
            default -> "MODIFIED";
        };
        session.put("human", human);
        session.put("status", status);
        session.put("updatedAt", ahoraIso());
        escribirSession(sessionPath, session);

        System.out.println("Decision humana registrada: " + status);
        System.out.println("sesion: " + rel(raiz, sessionPath));
        if ("APPROVED".equals(status)) {
            System.out.println("Siguiente: cambia @Tag(\"agente\") → @Tag(\"auditado\") en el "
                    + "archivo de test tras confirmar el checklist de 3 preguntas.");
        }
    }

    // ============================================================ propuesta

    private static Proposal construirPropuesta(Path raiz, Scope scope) throws IOException {
        String claseSrc = Files.readString(scope.clase, StandardCharsets.UTF_8);
        List<String> publicMethods = extraerPublicMethods(claseSrc);
        List<String> existing = new ArrayList<>();
        for (Path p : scope.testManual) {
            existing.addAll(extraerTestMethods(Files.readString(p, StandardCharsets.UTF_8)));
        }
        boolean salidaExists = Files.exists(scope.salida);
        if (salidaExists) {
            existing.addAll(extraerTestMethods(
                    Files.readString(scope.salida, StandardCharsets.UTF_8)));
        }
        // dedupe preservando orden
        existing = new ArrayList<>(new LinkedHashSet<>(existing));

        List<String> scenarios = inferirEscenarios(scope.tarea, claseSrc, publicMethods, existing);
        String target = nombreSinExtension(scope.clase);
        if (!publicMethods.isEmpty()) {
            target = target + " (" + String.join(", ",
                    publicMethods.subList(0, Math.min(4, publicMethods.size()))) + ")";
        }

        Proposal p = new Proposal();
        p.target = target;
        p.existingTestMethods = existing;
        p.existingTestCount = existing.size();
        p.salidaExists = salidaExists;
        p.scenarios = scenarios;
        p.publicMethods = publicMethods;
        p.salidaRel = rel(raiz, scope.salida);
        p.claseRel = rel(raiz, scope.clase);
        return p;
    }

    private static void imprimirPropuesta(Proposal p, String sessionId, Path sessionPath,
                                          Path raiz) {
        System.out.println("========== PROPUESTA (dry-run) ==========");
        System.out.println("Target:              " + p.target);
        System.out.println("Test type:           Unit Test (JUnit 5 + Mockito + AssertJ)");
        System.out.println("Existing tests:      " + p.existingTestCount);
        if (!p.existingTestMethods.isEmpty()) {
            System.out.println("Existing methods:    "
                    + String.join(", ", p.existingTestMethods.subList(
                    0, Math.min(12, p.existingTestMethods.size())))
                    + (p.existingTestMethods.size() > 12 ? ", ..." : ""));
        }
        System.out.println("Existing coverage:   (no leido automaticamente en Fase 1; "
                + "baseline en docs/qa/README.md / JaCoCo)");
        System.out.println("Salida existe:       " + p.salidaExists);
        System.out.println("Proposed scenarios:  " + p.scenarios.size());
        for (int i = 0; i < p.scenarios.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + p.scenarios.get(i));
        }
        System.out.println("New tests (estimado): ~" + p.scenarios.size());
        System.out.println("Files that will be modified:");
        System.out.println("  - " + p.salidaRel + (p.salidaExists ? " (EXISTE)" : " (nuevo)"));
        System.out.println("Session:             " + sessionId);
        System.out.println("Session file:        " + rel(raiz, sessionPath));
        System.out.println("Status:              PROPOSED — esperando revision humana");
        System.out.println("=========================================");
    }

    private static List<String> inferirEscenarios(String tarea, String claseSrc,
                                                    List<String> methods,
                                                    List<String> existingTests) {
        Set<String> out = new LinkedHashSet<>();
        // Del prompt del desarrollador
        for (String line : tarea.split("\\R")) {
            String t = line.strip();
            if (t.startsWith("-") || t.startsWith("*") || t.matches("^\\d+[.)].*")) {
                t = t.replaceFirst("^[-*\\d.)]+\\s*", "").strip();
                if (t.length() > 3) {
                    out.add(t);
                }
            }
        }
        String lower = (tarea + "\n" + claseSrc).toLowerCase(Locale.ROOT);
        if (lower.contains("null") || lower.contains("nulo")) {
            out.add("null / valores nulos en entradas obligatorias");
        }
        if (lower.contains("duplic") || lower.contains("unique") || lower.contains("ya existe")) {
            out.add("conflicto por duplicado (unique / ya existe)");
        }
        if (lower.contains("valid") || lower.contains("blank") || lower.contains("vacío")
                || lower.contains("vacio")) {
            out.add("validacion: blank / vacio / fuera de rango");
        }
        if (lower.contains("repository") || lower.contains("port") || lower.contains("falla")
                || lower.contains("failure")) {
            out.add("fallo de dependencia (repositorio / puerto de salida)");
        }
        if (lower.contains("autoriz") || lower.contains("rol") || lower.contains("forbidden")
                || lower.contains("403")) {
            out.add("fallo de autorizacion / rol insuficiente");
        }
        Matcher th = THROWS_EX.matcher(claseSrc);
        while (th.find() && out.size() < 10) {
            String exs = th.group(1).replace('\n', ' ').strip();
            out.add("excepcion de dominio: " + exs);
        }
        for (String m : methods) {
            if (m.equals("equals") || m.equals("hashCode") || m.equals("toString")
                    || m.equals("builder") || m.equals("main")) {
                continue;
            }
            boolean covered = existingTests.stream()
                    .anyMatch(t -> t.toLowerCase(Locale.ROOT).contains(m.toLowerCase(Locale.ROOT)));
            if (!covered) {
                out.add("happy path / invariante de metodo: " + m);
            }
            if (out.size() >= 8) {
                break;
            }
        }
        if (out.isEmpty()) {
            out.add("happy path del caso de uso principal");
            out.add("entrada invalida / validacion de negocio");
            out.add("fallo de dependencia mockeada");
        }
        List<String> list = new ArrayList<>(out);
        if (list.size() > 8) {
            return list.subList(0, 8);
        }
        return list;
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
        while (m.find()) {
            String name = m.group(1);
            if (name.equals(nombreClaseSimple(src))) {
                continue; // constructor
            }
            names.add(name);
        }
        return new ArrayList<>(new LinkedHashSet<>(names));
    }

    private static String nombreClaseSimple(String src) {
        Matcher m = Pattern.compile("\\b(?:class|record|interface|enum)\\s+(\\w+)").matcher(src);
        return m.find() ? m.group(1) : "";
    }

    // ============================================================ sessions

    private static Path sessionsDir(Path raiz) throws IOException {
        Path dir = raiz.resolve(SESSIONS_REL);
        Files.createDirectories(dir);
        return dir;
    }

    private static Path sessionFile(Path raiz, String id) throws IOException, Fallo {
        if (!id.matches("[A-Za-z0-9._-]+")) {
            throw new Fallo("id de sesion invalido: " + id);
        }
        return sessionsDir(raiz).resolve(id + ".json");
    }

    private static String nuevoSessionId(String targetSimple) {
        String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String safe = targetSimple.replaceAll("[^A-Za-z0-9_-]", "");
        if (safe.length() > 40) {
            safe = safe.substring(0, 40);
        }
        return stamp + "-" + safe;
    }

    private static void escribirSession(Path path, Map<String, Object> session)
            throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, Json.escribirPretty(session) + "\n", StandardCharsets.UTF_8);
        System.out.println("sesion escrita: " + path.getFileName());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> leerSession(Path path) throws IOException, Fallo {
        if (!Files.exists(path)) {
            throw new Fallo("sesion no encontrada: " + path
                    + " (lista docs/qa/ai-test-sessions/)");
        }
        Object o = Json.leer(Files.readString(path, StandardCharsets.UTF_8));
        if (!(o instanceof Map<?, ?> m)) {
            throw new Fallo("sesion JSON invalida: " + path);
        }
        return new LinkedHashMap<>((Map<String, Object>) m);
    }

    private static Map<String, Object> mapaHumanoVacio() {
        Map<String, Object> h = new LinkedHashMap<>();
        h.put("verdict", null);
        h.put("note", null);
        h.put("decidedAt", null);
        return h;
    }

    // ============================================================ scope

    private static final class Scope {
        final Path clase;
        final Path salida;
        final List<Path> contexto;
        final List<Path> testManual;
        final String tarea;

        Scope(Path clase, Path salida, List<Path> contexto, List<Path> testManual, String tarea) {
            this.clase = clase;
            this.salida = salida;
            this.contexto = contexto;
            this.testManual = testManual;
            this.tarea = tarea;
        }

        String nombreClaseObjetivo() {
            return nombreSinExtension(clase);
        }

        static Scope desdeArgs(Path raiz, Args a) throws Exception {
            if (a.clase == null) {
                throw new Fallo("falta --clase (o usa --sesion de una propuesta previa)");
            }
            if (a.salida == null) {
                throw new Fallo("falta --salida");
            }
            Path claseP = resolver(raiz, a.clase, "--clase");
            List<Path> contextoP = resolverTodas(raiz, a.contexto, "--contexto");
            List<Path> manualP = resolverTodas(raiz, a.testManual, "--test-manual");
            Path salidaP = resolverSalida(raiz, a.salida);
            if (manualP.isEmpty()) {
                throw new Fallo("falta al menos un --test-manual de referencia (modo de fallo "
                        + "E_SIN_TEST_MANUAL del skill ai-test-generator-edusync).");
            }
            return new Scope(claseP, salidaP, contextoP, manualP, resolverTarea(raiz, a.tarea));
        }

        @SuppressWarnings("unchecked")
        static Scope desdeSession(Path raiz, Map<String, Object> session, Args a)
                throws Exception {
            String clase = a.clase != null ? a.clase : str(session.get("clase"));
            String salida = a.salida != null ? a.salida : str(session.get("salida"));
            List<String> contexto = a.contexto.isEmpty()
                    ? (List<String>) session.getOrDefault("contexto", List.of())
                    : a.contexto;
            List<String> manual = a.testManual.isEmpty()
                    ? (List<String>) session.getOrDefault("testManual", List.of())
                    : a.testManual;
            String tarea = a.tarea != null ? a.tarea : str(session.get("tarea"));
            Args merged = new Args();
            merged.clase = clase;
            merged.salida = salida;
            merged.contexto = contexto != null ? new ArrayList<>(contexto) : new ArrayList<>();
            merged.testManual = manual != null ? new ArrayList<>(manual) : new ArrayList<>();
            merged.tarea = tarea;
            return desdeArgs(raiz, merged);
        }
    }

    private static final class Proposal {
        String target;
        List<String> existingTestMethods = List.of();
        int existingTestCount;
        boolean salidaExists;
        List<String> scenarios = List.of();
        List<String> publicMethods = List.of();
        String salidaRel;
        String claseRel;
    }

    private static String resolverTarea(Path raiz, String tarea) throws IOException {
        if (tarea == null || tarea.isBlank()) {
            return TAREA_DEFECTO;
        }
        // Solo intentar leer archivo si parece ruta (evita InvalidPathException en Windows
        // cuando el prompt contiene ':' o saltos de linea).
        boolean pareceRuta = !tarea.contains("\n") && !tarea.contains("\r")
                && (tarea.endsWith(".md") || tarea.endsWith(".txt")
                || tarea.contains("/") || tarea.contains("\\"));
        if (pareceRuta) {
            try {
                Path tareaP = raiz.resolve(tarea).normalize();
                if (tareaP.startsWith(raiz) && Files.exists(tareaP) && Files.isRegularFile(tareaP)) {
                    return Files.readString(tareaP, StandardCharsets.UTF_8);
                }
            } catch (java.nio.file.InvalidPathException ignored) {
                // texto literal del desarrollador
            }
        }
        return tarea;
    }

    // ---------------------------------------------------------------- rutas

    private static Path raizRepo() {
        Path cwd = Path.of("").toAbsolutePath();
        Path candidato = cwd;
        for (int i = 0; i < 6 && candidato != null; i++) {
            if (Files.exists(candidato.resolve("AGENTS.md"))) {
                return candidato;
            }
            candidato = candidato.getParent();
        }
        Path porConvencion = cwd.getParent() != null ? cwd.getParent().getParent() : null;
        if (porConvencion != null && Files.exists(porConvencion.resolve("AGENTS.md"))) {
            return porConvencion;
        }
        System.err.println("aviso: no se encontro AGENTS.md; se usa cwd como raiz.");
        return cwd;
    }

    private static Path dentroDeRaiz(Path raiz, String dado, String flag) throws Fallo {
        Path resuelto = raiz.resolve(dado).normalize();
        if (!resuelto.startsWith(raiz)) {
            throw new Fallo(flag + " ('" + dado + "') sale de la raiz del repo.");
        }
        return resuelto;
    }

    private static Path resolver(Path raiz, String dado, String flag) throws Fallo {
        Path resuelto = dentroDeRaiz(raiz, dado, flag);
        if (!Files.exists(resuelto)) {
            throw new Fallo(flag + " no encontrado: " + resuelto);
        }
        return resuelto;
    }

    private static List<Path> resolverTodas(Path raiz, List<String> dados, String flag)
            throws Fallo {
        List<Path> out = new ArrayList<>();
        for (String d : dados) {
            out.add(resolver(raiz, d, flag));
        }
        return out;
    }

    private static Path resolverSalida(Path raiz, String dado) throws Fallo {
        return dentroDeRaiz(raiz, dado, "--salida");
    }

    private static String bloque(String etiqueta, Path ruta) throws IOException {
        String contenido = Files.readString(ruta, StandardCharsets.UTF_8);
        return "### " + etiqueta + ": " + ruta.getFileName()
                + "\n```java\n" + contenido + "\n```";
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

    private static String nombreSinExtension(Path salida) {
        String nombre = salida.getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? nombre : nombre.substring(0, punto);
    }

    private static String rel(Path raiz, Path p) {
        return raiz.relativize(p).toString().replace('\\', '/');
    }

    private static List<String> relAll(Path raiz, List<Path> paths) {
        List<String> out = new ArrayList<>();
        for (Path p : paths) {
            out.add(rel(raiz, p));
        }
        return out;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String ahoraIso() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    // ------------------------------------------------------------- codigo

    private static final Pattern BLOQUE_CODIGO =
            Pattern.compile("```(?:java)?\\r?\\n(.*?)```", Pattern.DOTALL);

    private static final Pattern LINEA_PACKAGE =
            Pattern.compile("^\\s*package\\s+[\\w.]+\\s*;\\s*$", Pattern.MULTILINE);

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
        if (codigo.contains("Mockito.") && !codigo.contains("import org.mockito")) {
            imports.add("import static org.mockito.Mockito.*;");
        }
        if (!imports.isEmpty()) {
            codigo = insertarImports(codigo, imports);
            System.out.println("aviso: se agregaron imports faltantes ("
                    + String.join(", ", imports)
                    + "); leer siempre lo que devuelve el modelo antes de correr mvn test");
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

    // -------------------------------------------------------------- config

    private static final class Config {
        final String proveedor;
        final String baseUrl;
        final String apiKey;
        final String modelo;
        final Duration timeout;

        private Config(String proveedor, String baseUrl, String apiKey, String modelo,
                       Duration timeout) {
            this.proveedor = proveedor;
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.modelo = modelo;
            this.timeout = timeout;
        }

        static Config leer(Path raiz) throws IOException {
            Map<String, String> env = leerDotenv(raiz.resolve(".env"));
            String proveedor = valor(env, "EDUSYNC_AI_PROVIDER", "ollama").toLowerCase(Locale.ROOT);
            if (proveedor.equals("open-webui")) {
                String base = valor(env, "OPEN_WEBUI_BASE_URL", "http://localhost:3000");
                String apiKey = valor(env, "OPEN_WEBUI_API_KEY", "sk-local");
                String modelo = valor(env, "OPEN_WEBUI_MODEL", "llama3.1:latest");
                long tSeg = Long.parseLong(valor(env, "OPEN_WEBUI_TIMEOUT_SECONDS", "120"));
                return new Config(proveedor, quitarSlashFinal(base) + "/api", apiKey, modelo,
                        Duration.ofSeconds(tSeg));
            }
            String base = valor(env, "OLLAMA_BASE_URL", "http://localhost:11434");
            String modelo = valor(env, "OLLAMA_MODEL", "llama3.1:latest");
            long tSeg = Long.parseLong(valor(env, "OLLAMA_TIMEOUT_SECONDS", "120"));
            return new Config(proveedor, quitarSlashFinal(base) + "/v1", "ollama", modelo,
                    Duration.ofSeconds(tSeg));
        }

        String resumen() {
            return "proveedor=" + proveedor + " modelo=" + modelo + " via " + baseUrl;
        }

        private static String quitarSlashFinal(String s) {
            return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
        }

        private static String valor(Map<String, String> env, String clave, String porDefecto) {
            String v = System.getenv(clave);
            if (v != null && !v.isBlank()) {
                return v;
            }
            return env.getOrDefault(clave, porDefecto);
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
                String clave = l.substring(0, eq).strip();
                String val = l.substring(eq + 1).strip();
                if (val.length() >= 2 && ((val.startsWith("\"") && val.endsWith("\""))
                        || (val.startsWith("'") && val.endsWith("'")))) {
                    val = val.substring(1, val.length() - 1);
                }
                out.put(clave, val);
            }
            return out;
        }
    }

    // ------------------------------------------------------------ llamada

    private record LlmRespuesta(String contenido, Long promptTokens, Long completionTokens) {
    }

    private static LlmRespuesta llamarModelo(Config cfg, String systemMsg, String userMsg)
            throws IOException, InterruptedException, Fallo {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", cfg.modelo);
        body.put("temperature", 0);
        List<Object> mensajes = new ArrayList<>();
        mensajes.add(Map.of("role", "system", "content", systemMsg));
        mensajes.add(Map.of("role", "user", "content", userMsg));
        body.put("messages", mensajes);

        String json = Json.escribir(body);

        HttpClient client = HttpClient.newBuilder().connectTimeout(cfg.timeout).build();
        HttpRequest.Builder reqB = HttpRequest.newBuilder()
                .uri(URI.create(cfg.baseUrl + "/chat/completions"))
                .timeout(cfg.timeout)
                .header("Content-Type", "application/json; charset=utf-8");
        if (cfg.apiKey != null && !cfg.apiKey.isBlank() && !cfg.apiKey.equals("ollama")) {
            reqB.header("Authorization", "Bearer " + cfg.apiKey);
        }
        HttpRequest req = reqB.POST(HttpRequest.BodyPublishers.ofString(json,
                StandardCharsets.UTF_8)).build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(
                StandardCharsets.UTF_8));
        if (resp.statusCode() / 100 != 2) {
            throw new Fallo("el modelo respondio HTTP " + resp.statusCode() + ": "
                    + resp.body());
        }

        Object parsed = Json.leer(resp.body());
        Map<?, ?> raiz = (Map<?, ?>) parsed;
        List<?> choices = (List<?>) raiz.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new Fallo("respuesta sin 'choices': " + resp.body());
        }
        Map<?, ?> primera = (Map<?, ?>) choices.get(0);
        Map<?, ?> mensaje = (Map<?, ?>) primera.get("message");
        String contenido = String.valueOf(mensaje.get("content"));

        Long promptTokens = null;
        Long completionTokens = null;
        Object usage = raiz.get("usage");
        if (usage instanceof Map<?, ?> u) {
            promptTokens = numeroLong(u.get("prompt_tokens"));
            completionTokens = numeroLong(u.get("completion_tokens"));
        }
        return new LlmRespuesta(contenido, promptTokens, completionTokens);
    }

    private static Long numeroLong(Object o) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    // --------------------------------------------------------------- args

    private static final class Args {
        String clase;
        List<String> contexto = new ArrayList<>();
        List<String> testManual = new ArrayList<>();
        String salida;
        String tarea;
        String sesion;
        String veredicto;
        String nota;
        boolean proponer;
        boolean generar;
        boolean aprobarAlcance;
        boolean ejecutarTests;
        boolean decidir;
        boolean forzar;

        static Args parse(String[] argv) throws Fallo {
            Args a = new Args();
            for (int i = 0; i < argv.length; i++) {
                String flag = argv[i];
                switch (flag) {
                    case "--proponer" -> {
                        a.proponer = true;
                        continue;
                    }
                    case "--generar" -> {
                        a.generar = true;
                        continue;
                    }
                    case "--aprobar-alcance" -> {
                        a.aprobarAlcance = true;
                        continue;
                    }
                    case "--ejecutar-tests" -> {
                        a.ejecutarTests = true;
                        continue;
                    }
                    case "--decidir" -> {
                        a.decidir = true;
                        continue;
                    }
                    case "--forzar" -> {
                        a.forzar = true;
                        continue;
                    }
                    default -> {
                        // flags con valor
                    }
                }
                String val = (i + 1 < argv.length) ? argv[++i] : null;
                if (val == null) {
                    throw new Fallo("falta el valor de " + flag);
                }
                switch (flag) {
                    case "--clase" -> a.clase = val;
                    case "--contexto" -> a.contexto.add(val);
                    case "--test-manual" -> a.testManual.add(val);
                    case "--salida" -> a.salida = val;
                    case "--tarea" -> a.tarea = val;
                    case "--sesion" -> a.sesion = val;
                    case "--veredicto" -> a.veredicto = val;
                    case "--nota" -> a.nota = val;
                    default -> throw new Fallo("flag desconocido: " + flag
                            + " (usa --proponer|--generar|--ejecutar-tests|--decidir)");
                }
            }
            int modos = (a.generar ? 1 : 0) + (a.ejecutarTests ? 1 : 0) + (a.decidir ? 1 : 0);
            // --proponer es el default si no hay otro modo
            if (modos > 1) {
                throw new Fallo("elige un solo modo: --proponer | --generar | "
                        + "--ejecutar-tests | --decidir");
            }
            return a;
        }
    }

    private static final class Fallo extends Exception {
        Fallo(String msg) {
            super(msg);
        }
    }

    // ---------------------------------------------------- JSON (sin libs)

    private static final class Json {

        static String escribir(Object o) {
            StringBuilder sb = new StringBuilder();
            escribirValor(o, sb, 0, false);
            return sb.toString();
        }

        static String escribirPretty(Object o) {
            StringBuilder sb = new StringBuilder();
            escribirValor(o, sb, 0, true);
            return sb.toString();
        }

        private static void escribirValor(Object o, StringBuilder sb, int indent, boolean pretty) {
            if (o == null) {
                sb.append("null");
            } else if (o instanceof String s) {
                escribirString(s, sb);
            } else if (o instanceof Number || o instanceof Boolean) {
                sb.append(o);
            } else if (o instanceof Map<?, ?> m) {
                sb.append('{');
                if (pretty && !m.isEmpty()) {
                    sb.append('\n');
                }
                boolean primero = true;
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    if (!primero) {
                        sb.append(pretty ? ",\n" : ",");
                    }
                    primero = false;
                    if (pretty) {
                        indent(sb, indent + 1);
                    }
                    escribirString(String.valueOf(e.getKey()), sb);
                    sb.append(pretty ? ": " : ":");
                    escribirValor(e.getValue(), sb, indent + 1, pretty);
                }
                if (pretty && !m.isEmpty()) {
                    sb.append('\n');
                    indent(sb, indent);
                }
                sb.append('}');
            } else if (o instanceof List<?> l) {
                sb.append('[');
                if (pretty && !l.isEmpty()) {
                    sb.append('\n');
                }
                for (int i = 0; i < l.size(); i++) {
                    if (i > 0) {
                        sb.append(pretty ? ",\n" : ",");
                    }
                    if (pretty) {
                        indent(sb, indent + 1);
                    }
                    escribirValor(l.get(i), sb, indent + 1, pretty);
                }
                if (pretty && !l.isEmpty()) {
                    sb.append('\n');
                    indent(sb, indent);
                }
                sb.append(']');
            } else {
                escribirString(String.valueOf(o), sb);
            }
        }

        private static void indent(StringBuilder sb, int n) {
            sb.append("  ".repeat(Math.max(0, n)));
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
                    case '\b' -> sb.append("\\b");
                    case '\f' -> sb.append("\\f");
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
            Parser p = new Parser(texto);
            Object v = p.valor();
            p.saltarEspacios();
            return v;
        }

        private static final class Parser {
            private final String s;
            private int i;

            Parser(String s) {
                this.s = s;
                this.i = 0;
            }

            Object valor() throws Fallo {
                saltarEspacios();
                if (i >= s.length()) {
                    throw new Fallo("JSON invalido: fin inesperado");
                }
                char c = s.charAt(i);
                return switch (c) {
                    case '{' -> objeto();
                    case '[' -> arreglo();
                    case '"' -> string();
                    case 't', 'f' -> booleano();
                    case 'n' -> nulo();
                    default -> numero();
                };
            }

            Map<String, Object> objeto() throws Fallo {
                Map<String, Object> m = new LinkedHashMap<>();
                esperar('{');
                saltarEspacios();
                if (mirar() == '}') {
                    i++;
                    return m;
                }
                while (true) {
                    saltarEspacios();
                    String clave = string();
                    saltarEspacios();
                    esperar(':');
                    Object val = valor();
                    m.put(clave, val);
                    saltarEspacios();
                    char c = mirar();
                    if (c == ',') {
                        i++;
                    } else if (c == '}') {
                        i++;
                        break;
                    } else {
                        throw new Fallo("JSON invalido en objeto, posicion " + i);
                    }
                }
                return m;
            }

            List<Object> arreglo() throws Fallo {
                List<Object> l = new ArrayList<>();
                esperar('[');
                saltarEspacios();
                if (mirar() == ']') {
                    i++;
                    return l;
                }
                while (true) {
                    l.add(valor());
                    saltarEspacios();
                    char c = mirar();
                    if (c == ',') {
                        i++;
                    } else if (c == ']') {
                        i++;
                        break;
                    } else {
                        throw new Fallo("JSON invalido en arreglo, posicion " + i);
                    }
                }
                return l;
            }

            String string() throws Fallo {
                esperar('"');
                StringBuilder sb = new StringBuilder();
                while (true) {
                    if (i >= s.length()) {
                        throw new Fallo("JSON invalido: string sin cerrar");
                    }
                    char c = s.charAt(i++);
                    if (c == '"') {
                        break;
                    }
                    if (c == '\\') {
                        char e = s.charAt(i++);
                        switch (e) {
                            case '"' -> sb.append('"');
                            case '\\' -> sb.append('\\');
                            case '/' -> sb.append('/');
                            case 'n' -> sb.append('\n');
                            case 'r' -> sb.append('\r');
                            case 't' -> sb.append('\t');
                            case 'b' -> sb.append('\b');
                            case 'f' -> sb.append('\f');
                            case 'u' -> {
                                String hex = s.substring(i, i + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            }
                            default -> throw new Fallo("escape JSON invalido: \\" + e);
                        }
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }

            Boolean booleano() throws Fallo {
                if (s.startsWith("true", i)) {
                    i += 4;
                    return Boolean.TRUE;
                }
                if (s.startsWith("false", i)) {
                    i += 5;
                    return Boolean.FALSE;
                }
                throw new Fallo("JSON invalido: se esperaba true/false en posicion " + i);
            }

            Object nulo() throws Fallo {
                if (s.startsWith("null", i)) {
                    i += 4;
                    return null;
                }
                throw new Fallo("JSON invalido: se esperaba null en posicion " + i);
            }

            Number numero() throws Fallo {
                int inicio = i;
                while (i < s.length() && "-+.eE0123456789".indexOf(s.charAt(i)) >= 0) {
                    i++;
                }
                String num = s.substring(inicio, i);
                if (num.isEmpty()) {
                    throw new Fallo("JSON invalido: numero vacio en posicion " + i);
                }
                if (num.contains(".") || num.contains("e") || num.contains("E")) {
                    return Double.parseDouble(num);
                }
                try {
                    return Long.parseLong(num);
                } catch (NumberFormatException ex) {
                    return Double.parseDouble(num);
                }
            }

            void esperar(char c) throws Fallo {
                if (i >= s.length() || s.charAt(i) != c) {
                    throw new Fallo("JSON invalido: se esperaba '" + c + "' en posicion " + i);
                }
                i++;
            }

            char mirar() throws Fallo {
                saltarEspacios();
                if (i >= s.length()) {
                    throw new Fallo("JSON invalido: fin inesperado");
                }
                return s.charAt(i);
            }

            void saltarEspacios() {
                while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                    i++;
                }
            }
        }
    }
}
