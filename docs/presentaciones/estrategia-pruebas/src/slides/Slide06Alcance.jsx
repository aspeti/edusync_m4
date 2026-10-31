import { motion } from 'framer-motion';
import { CircleCheck, Clock3, CircleX } from 'lucide-react';
import { SlideFrame, item } from '../components/ui.jsx';

const HICIMOS = [
  ['CI con gates de calidad', 'mvn verify con JaCoCo, mutación con Pitest y verificación del catálogo de red team en cada push.'],
  ['Red teaming automatizado', '18 casos en cada mvn test con modelo hostil, más corridas en vivo con Ollama contra localhost.'],
  ['Tests generados con agente y auditados', 'Unit y E2E propuestos por el agente; cada test pasa por un checklist humano antes de entrar.'],
  ['Control con test de regresión', 'PoliticaAlcanceAgente (ADR-0022). Sin él, el test falla en 3 ataques.'],
];

const NO_HICIMOS = [
  ['Evals online / shadow testing', 'fuera'],
  ['Monitoreo de toxicidad en producción', 'fuera'],
  ['Fine-tuning del modelo base', 'fuera'],
  ['LLM-as-a-Judge y métricas RAG', 'proximo'],
  ['Casos de toxicidad y sesgo', 'proximo'],
];

export default function Slide06Alcance() {
  return (
    <SlideFrame seccion="06 · Alcance" titulo="Qué hicimos y qué no">
      <div className="grid flex-1 grid-cols-1 gap-6 md:grid-cols-[1.35fr_1fr]">
        <motion.div variants={item} className="flex min-w-0 flex-col gap-4 rounded-xl border border-emerald/30 bg-emerald/5 p-6">
          <p className="font-mono text-xs uppercase tracking-[0.18em] text-emerald">Hecho</p>
          <ul className="flex flex-col gap-4">
            {HICIMOS.map(([t, d]) => (
              <li key={t} className="flex gap-3">
                <CircleCheck size={22} className="mt-0.5 shrink-0 text-emerald" aria-hidden="true" />
                <div className="min-w-0">
                  <p className="font-medium text-text">{t}</p>
                  <p className="text-sm leading-relaxed text-mute">{d}</p>
                </div>
              </li>
            ))}
          </ul>
        </motion.div>

        <motion.div variants={item} className="flex min-w-0 flex-col gap-4 rounded-xl border border-line bg-panel/60 p-6">
          <p className="font-mono text-xs uppercase tracking-[0.18em] text-mute">Fuera del alcance inicial</p>
          <ul className="flex flex-col gap-4">
            {NO_HICIMOS.map(([t, tipo]) => (
              <li key={t} className="flex items-center gap-3">
                {tipo === 'fuera' ? (
                  <CircleX size={22} className="shrink-0 text-rose" aria-hidden="true" />
                ) : (
                  <Clock3 size={22} className="shrink-0 text-amber" aria-hidden="true" />
                )}
                <p className="text-text">{t}</p>
              </li>
            ))}
          </ul>
          <div className="mt-auto flex flex-wrap gap-4 border-t border-line pt-4 font-mono text-[11px] text-mute">
            <span className="flex items-center gap-1.5"><CircleX size={14} className="text-rose" aria-hidden="true" /> fuera de alcance</span>
            <span className="flex items-center gap-1.5"><Clock3 size={14} className="text-amber" aria-hidden="true" /> próximo paso</span>
          </div>
        </motion.div>
      </div>
    </SlideFrame>
  );
}
