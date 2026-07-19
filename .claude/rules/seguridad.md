---
paths:
  - "**/*.{java,kt,yml,yaml,properties,xml}"
---

# Seguridad: secretos y registros sensibles (OWASP ASVS L2)

Espejo de `.cursor/rules/seguridad.mdc`. Aplica al editar código Spring/Java y configuración.

## Prohibido en código y configuración

- Claves API, tokens OAuth/OpenID, `client_secret`, refresh tokens, JWT firmados o claves privadas embebidas.
- Contraseñas de BD, connection strings con credenciales en texto plano.
- Valores reales de producción en `application.yml` / `.properties`; usar variables de entorno o un almacén de secretos.
- “Secretos de ejemplo” que parezcan reales; usar placeholders (`${VAR}`, `<REDACTED>`).

## Prohibido en logs

- Contraseñas, OTP, tokens, cookies, cabeceras `Authorization` / `Cookie`.
- Cuerpos con PII o calificaciones individuales; el campo `rude` del estudiante.
- Stack traces que incluyan query strings con secretos.

## Checklist antes de completar un cambio

1. ¿Hay literales que parezcan claves, tokens o contraseñas?
2. ¿Algún `log` imprime autenticación, PII o payloads completos?
3. ¿La configuración usa placeholders y no valores reales?

Si cualquier respuesta es “sí”, corregir antes de dar el código por terminado. Ver también `AGENTS.md` §7.
