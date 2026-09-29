# M7 · generador de tests unitarios (forma lab)

Espejo de `plantillas/Labs/LabX_M7_unit/agente_generador.py` para EduSync.
El modelo **propone**; una persona **audita**. Esta herramienta **nunca borra** tests a mano:
escribe un sidecar `*AgenteTest.java`.

El fusionador Java (`tools/ai-test-generator-edusync/GenerarTest.java`) sigue existiendo
si quieres agregar metodos dentro de un `*Test.java` ya escrito.

## Preparar

```powershell
cd tools\m7-unit
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
copy .env.example .env
# o reutilizar el .env de la raiz del repo (OLLAMA_* / OPEN_WEBUI_*)
```

Ollama en `http://localhost:11434`. Este CLI **ignora** `EDUSYNC_AI_PROVIDER=open-webui` del `.env` de EduSync (ese timeout de 120 s corta la generación en CPU). Para WebUI: `$env:M7_PROVIDER='open-webui'`.

## Generar (HITL paso 1)

```powershell
python agente_generador.py ejercicio_1_analizador
python agente_generador.py ejercicio_2_guardrail
```

Salida esperada (como el lab):

```text
agente -> modelo=llama3.1:latest via http://localhost:11434/v1
escrito: backend\src\test\java\...\AnalizadorIntencionConsultaAcademicaAgenteTest.java
tokens: entrada=... salida=...  tiempo=...s
```

## Correr (HITL paso 2)

```powershell
cd ..\..\backend
mvn -Dtest=AnalizadorIntencionConsultaAcademicaAgenteTest test
mvn -Dtest=GuardrailEntradaAgenteAgenteTest test
```

Verde ≠ aprobado. Audita con las 3 preguntas del lab:

1. ¿El test verifica comportamiento, o solo que “no lanzo”?
2. ¿El nombre dice el escenario, o es `test1`?
3. ¿Cubre un caso que el test a mano no tenia, o es un duplicado?

Si pasa: cambia `@Tag("agente")` por `@Tag("auditado")` a mano.
Si no: `@Disabled("motivo")` — no borres el archivo.

## Ejercicios

| Clave | Clase | Sidecar |
|-------|--------|---------|
| `ejercicio_1_analizador` | `AnalizadorIntencionConsultaAcademica` | `AnalizadorIntencionConsultaAcademicaAgenteTest.java` |
| `ejercicio_2_guardrail` | `GuardrailEntradaAgente` | `GuardrailEntradaAgenteAgenteTest.java` |

Para otro ejercicio: copia una carpeta `ejercicio_N_*`, edita `PROMPT_AGENTE.md` y registra rutas en `CONTEXTO` / `SALIDA` de `agente_generador.py`.
