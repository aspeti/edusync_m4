#!/usr/bin/env node
// stop hook — al terminar cada turno del agente, revisa si hay cambios sin
// commitear en backend/, frontend/, docs/design/ o docs/prompts/impl/PR-IMPL-*.md
// sin una actualizacion correspondiente en docs/product/DTP.md, y si es asi
// recuerda ejecutar el skill @dtp-sync (ver .cursor/skills/dtp-sync/SKILL.md).
// loop_limit: 1 en hooks.json evita que el recordatorio se repita en cada
// turno de la misma conversacion.

const { execSync } = require('child_process');

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

  if (input.status !== 'completed') {
    process.stdout.write('{}');
    return;
  }

  const cwd = process.env.CURSOR_PROJECT_DIR || process.cwd();
  const runGit = (args) =>
    execSync(`git ${args}`, {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
      cwd,
    })
      .split(/\r?\n/)
      .filter(Boolean);

  let changed = [];
  try {
    // "diff --name-only HEAD" solo ve archivos ya trackeados (modificados/staged).
    // Se combina con "ls-files --others" para no perder archivos nuevos sin
    // trackear todavia — el caso esperado al empezar a poblar src/ desde cero.
    const tracked = runGit('diff --name-only HEAD');
    const untracked = runGit('ls-files --others --exclude-standard');
    changed = [...tracked, ...untracked];
  } catch {
    process.stdout.write('{}');
    return;
  }

  const normalized = changed.map((p) => p.replace(/\\/g, '/'));
  const touchesImpl = normalized.some(
    (p) =>
      p.startsWith('backend/') ||
      p.startsWith('frontend/') ||
      p.startsWith('src/') ||
      p.startsWith('docs/design/') ||
      /^docs\/prompts\/impl\/PR-IMPL-/.test(p)
  );
  const touchesDtp = normalized.some((p) => p === 'docs/product/DTP.md');

  if (touchesImpl && !touchesDtp) {
    process.stdout.write(
      JSON.stringify({
        followup_message:
          'Detecte cambios sin commitear en backend/, frontend/, docs/design/ o docs/prompts/impl/PR-IMPL-*.md sin una actualizacion correspondiente en docs/product/DTP.md. Ejecuta el skill @dtp-sync para sincronizar docs/product/DTP.md (y PRD.md/FSD.md en docs/product/ si aplica) antes de continuar, o confirma explicitamente que este cambio no requiere sincronizacion documental.',
      })
    );
    return;
  }

  process.stdout.write('{}');
});
