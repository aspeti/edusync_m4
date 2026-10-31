import { motion } from 'framer-motion';
import { Wrench, MessageSquareText } from 'lucide-react';
import { contenedor, item } from '../components/ui.jsx';

const PASOS = [
  'Subir la cobertura de 78,1 % al gate de 80 %',
  'Tests de contrato contra el JSON Schema del asistente',
  'LLM-as-a-Judge y métricas RAG sobre el dataset dorado',
  'Casos de toxicidad y sesgo en el catálogo de red team',
];

export default function Slide07Conclusiones() {
  return (
    <motion.section
      variants={contenedor}
      initial="oculto"
      animate="visible"
      className="relative flex h-full w-full flex-col justify-between gap-8 px-6 py-10 md:px-16 md:py-12"
    >
      <motion.p variants={item} className="font-mono text-xs uppercase tracking-[0.18em] text-neon">
        07 · Conclusiones
      </motion.p>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <motion.div variants={item} className="flex flex-col gap-3 rounded-xl border border-neon/30 bg-neon/5 p-6">
          <div className="flex items-center gap-3 text-neon">
            <Wrench size={20} aria-hidden="true" />
            <span className="font-mono text-xs uppercase tracking-[0.18em]">Pruebas deterministas</span>
          </div>
          <p className="font-display text-3xl font-semibold leading-tight text-text md:text-4xl">
            aseguran <span className="text-neon">cómo</span> funciona
          </p>
        </motion.div>
        <motion.div variants={item} className="flex flex-col gap-3 rounded-xl border border-emerald/30 bg-emerald/5 p-6">
          <div className="flex items-center gap-3 text-emerald">
            <MessageSquareText size={20} aria-hidden="true" />
            <span className="font-mono text-xs uppercase tracking-[0.18em]">Evals y red teaming</span>
          </div>
          <p className="font-display text-3xl font-semibold leading-tight text-text md:text-4xl">
            aseguran <span className="text-emerald">qué</span> genera
          </p>
        </motion.div>
      </div>

      <div className="grid grid-cols-1 items-end gap-8 md:grid-cols-[1.4fr_1fr]">
        <motion.div variants={item} className="flex flex-col gap-3">
          <p className="font-mono text-xs uppercase tracking-[0.18em] text-amber">Próximos pasos</p>
          <ol className="grid grid-cols-1 gap-2 md:grid-cols-2">
            {PASOS.map((p, i) => (
              <li key={p} className="flex gap-3 text-[15px] leading-snug text-text">
                <span className="font-mono text-sm text-amber tnum">{i + 1}</span>
                {p}
              </li>
            ))}
          </ol>
        </motion.div>
        <motion.div variants={item} className="md:text-right">
          <p className="font-display text-6xl font-bold leading-none text-text md:text-7xl">¿Preguntas?</p>
          <p className="mt-2 font-mono text-xs text-mute">Q&amp;A</p>
        </motion.div>
      </div>
    </motion.section>
  );
}
