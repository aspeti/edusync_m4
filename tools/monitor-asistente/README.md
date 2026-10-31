# Monitor en línea del asistente EduSync

El red team (`tools/red-team-agent`) es la compuerta **offline**: dataset dorado conocido, antes del deploy. Este monitor es la compuerta **online**: tráfico de después del deploy. Un caso que una persona acepta vuelve a `dataset/golden-online.json`.

El motor está en Java (`com.edusync.shared.ai.application.monitoring`). No llama al modelo. Los umbrales viven en `backend/src/main/resources/ai/umbrales_monitoreo.json`.

## Comandos

Desde `backend/` (hace falta haber compilado):

```powershell
mvn -q -DskipTests compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.edusync.shared.ai.application.monitoring.MonitorAsistenteCli" "-Dexec.args=alertas ../tools/monitor-asistente/datos/trafico_dia1.jsonl"
```

Equivalentes: `resumen`, `alertas`, `latencia`, `casos-nuevos`. Alias en inglés: `summary`, `alerts`, `latency`, `new-cases`.

| Código | Significado |
|---|---|
| 0 | No hay alertas `CRITICAL` ni `HIGH` |
| 1 | Hay al menos una. `MEDIUM` se imprime y no cambia el código |

No hay rollback automático.

## Demo

```powershell
.\tools\monitor-asistente\demo.ps1
```

Día 1 (`v1`): fidelidad en rango, cero frases prohibidas, cero ráfagas. Código 0.

Día 2: a las 10:00 entra `v2` (cifra `64` que no está en el contexto, y «te apruebo el trimestre»); a las 14:00 se repite el clima de Cochabamba; a las 16:10 `U-SYN-07` encadena cuatro bloqueos. Código 1. El informe queda en `tools/monitor-asistente/resultados/`.

## De producción al dataset dorado

1. `casos-nuevos` escribe candidatos. No toca ningún dataset.
2. Una persona crea una revisión con `"estado": "ACEPTADO"` (hay un ejemplo en `revisiones/ONLINE-001.json`). `RECHAZADO` no se copia.
3. `promover <revision.json> <destino.json>` agrega el caso a `dataset/golden-online.json` si todavía no está.

`tools/red-team-agent/catalog/golden.json` no se modifica. Cuando el caso deba entrar en la compuerta offline del red team, una persona lo traslada a ese catálogo en un cambio aparte.

## Registro en el proceso

Apagado por defecto (`EDUSYNC_AI_MONITOR_REGISTRO=false`). Encendido escribe JSONL sin JWT. El modo `minimizado` guarda hash de la pregunta y de la respuesta, no el texto. `laboratorio` guarda el texto ya redactado (RUDE, CI, tarjeta, correo). Esas líneas no llevan tokens: el puerto del modelo no los informa.

## CI

`mvn verify` del job Backend ejecuta `MonitorAsistenteTest` (día 1 en 0, día 2 en 1). El día 2 no es el log de cada push: es el fixture de la demo.
