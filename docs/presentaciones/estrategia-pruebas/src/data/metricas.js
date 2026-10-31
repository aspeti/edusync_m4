// Cifras medidas en EduSync. Cada valor indica de dónde sale, para poder actualizarlo
// cuando se repitan las corridas. No hay cifras inventadas: lo no medido figura como "próximo paso".

export const fuente = {
  jacoco: 'backend/target/site/jacoco/jacoco.csv · 28/09/2026',
  pitest: 'docs/qa/README.md · baseline 10/09/2026',
  e2e: 'tests/e2e/AUDITORIA_TABLA.md',
  redteam: 'tools/red-team-agent/evidencia · retest 30/09/2026',
};

export const unit = {
  tests: 276,
  totalTests: 330,
  cobertura: '78,1 %',
  ramas: '55,7 %',
  mutacion: '74 %',
};

export const integracion = {
  tests: 51,
  clases: 12,
  endpoint: 'POST /api/v1/ai/agente',
};

export const e2e = {
  planificados: 26,
  aceptados: 20,
  agenteGenerados: 13,
  agenteAceptados: 12,
};

export const redteam = {
  ataques: 17,
  casos: 18,
  exitosDeterminista: 0,
  hallazgo: 'AI-SEC-007',
  ataqueVivo: 'RT-EXFIL-002',
  antes: '3/3',
  despues: '0/3',
  canary: 'EDUSYNC_REDACTED_CANARY_001',
};
