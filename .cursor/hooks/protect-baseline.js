#!/usr/bin/env node
// preToolUse hook — bloquea escrituras/eliminaciones dentro de docs/baseline/**
// (registro historico congelado de M4, tag release/2.0.0). Ver AGENTS.md §8.2
// y .cursor/rules/baseline-congelado.mdc.
//
// No conoce el esquema exacto de tool_input de cada herramienta, asi que
// recolecta recursivamente todos los strings del payload y busca la ruta
// protegida en cualquiera de ellos (path, file_path, contenido, etc.).

let raw = '';
process.stdin.on('data', (chunk) => {
  raw += chunk;
});

process.stdin.on('end', () => {
  let input = {};
  try {
    input = JSON.parse(raw || '{}');
  } catch {
    input = {};
  }

  const strings = [];
  const collect = (value, depth) => {
    if (depth > 6 || value == null) return;
    if (typeof value === 'string') {
      strings.push(value);
    } else if (Array.isArray(value)) {
      value.forEach((v) => collect(v, depth + 1));
    } else if (typeof value === 'object') {
      Object.values(value).forEach((v) => collect(v, depth + 1));
    }
  };
  collect(input.tool_input, 0);

  const normalized = strings.map((s) => s.replace(/\\/g, '/'));
  const hitsBaseline = normalized.some((s) => /(^|[/:])docs\/baseline\//i.test(s));

  if (hitsBaseline) {
    process.stdout.write(
      JSON.stringify({
        permission: 'deny',
        user_message:
          'Bloqueado: docs/baseline/** es el registro historico congelado de M4 (tag release/2.0.0) y ningun agente puede editarlo ni eliminarlo. Si necesitas reflejar un cambio real, hazlo en docs/product/ (+ un ADR en docs/adr/ si aplica) y ejecuta el skill @dtp-sync.',
        agent_message:
          'La ruta objetivo pertenece a docs/baseline/** (congelado, tag release/2.0.0; ver AGENTS.md §8.2 y .cursor/rules/baseline-congelado.mdc). Esta accion fue bloqueada por el hook protect-baseline. MUST NOT editar ni eliminar el baseline bajo ninguna circunstancia: redirige el cambio a docs/product/ (BRD.md, PRD.md, FSD.md, DTP.md) y, si corresponde, crea un ADR en docs/adr/. Luego invoca el skill @dtp-sync para propagar el cambio y reportar el bloqueo al usuario.',
      })
    );
    return;
  }

  process.stdout.write(JSON.stringify({ permission: 'allow' }));
});
