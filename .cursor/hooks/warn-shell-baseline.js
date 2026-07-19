#!/usr/bin/env node
// beforeShellExecution hook — pide confirmacion cuando un comando de shell
// parece escribir, mover o borrar algo dentro de docs/baseline/** (congelado).
// Complementa a protect-baseline.js (que cubre Write/StrReplace/EditNotebook/
// Delete), ya que las mismas operaciones tambien podrian intentarse via
// terminal (git mv, Remove-Item, Set-Content, etc.).

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

  const command = String(input.command || '');
  const touchesBaseline = /docs[\\/]+baseline/i.test(command);
  const looksLikeWrite =
    /(Remove-Item|Move-Item|del\s|rm\s|mv\s|git\s+mv|git\s+rm|Set-Content|Out-File|Add-Content|>>?[^=]|cp\s+-f|Copy-Item[^\n]*-Force)/i.test(
      command
    );

  if (touchesBaseline && looksLikeWrite) {
    process.stdout.write(
      JSON.stringify({
        permission: 'ask',
        user_message:
          'Este comando de shell parece escribir, mover o borrar algo dentro de docs/baseline/** (congelado, tag release/2.0.0). Revisalo antes de aprobar.',
        agent_message:
          'El comando referencia docs/baseline/** junto con un verbo de escritura/borrado/movimiento. docs/baseline/** esta protegido (AGENTS.md §8.2, .cursor/rules/baseline-congelado.mdc). Si el objetivo es reflejar un cambio real, redirige el cambio a docs/product/ en su lugar.',
      })
    );
    return;
  }

  process.stdout.write(JSON.stringify({ permission: 'allow' }));
});
