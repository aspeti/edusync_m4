import { motion } from 'framer-motion';
import { contenedor, item } from '../components/ui.jsx';

// Espectro de capas: de izquierda (resultado exacto) a derecha (resultado probabilístico).
const CAPAS = [
  { nombre: 'Unit', tipo: 'det' },
  { nombre: 'Integración', tipo: 'det' },
  { nombre: 'Contrato', tipo: 'det' },
  { nombre: 'E2E', tipo: 'det' },
  { nombre: 'Red team', tipo: 'prob' },
  { nombre: 'Evals', tipo: 'prob' },
];

export default function Slide01Titulo() {
  return (
    <motion.section
      variants={contenedor}
      initial="oculto"
      animate="visible"
      className="relative flex h-full w-full flex-col justify-between gap-10 px-6 py-10 md:px-16 md:py-14"
    >
      <motion.p variants={item} className="font-mono text-xs uppercase tracking-[0.2em] text-neon">
        EduSync · asistente académico con IA · octubre 2026
      </motion.p>

      <div className="flex flex-col gap-5">
        <motion.h1
          variants={item}
          className="font-display text-5xl font-bold leading-[0.95] tracking-tight text-text md:text-[5.5rem]"
        >
          Estrategia de Pruebas
          <br />
          <span className="text-neon">y Validación</span>
        </motion.h1>
        <motion.p variants={item} className="max-w-[40ch] text-lg text-mute md:text-2xl">
          Del determinismo al análisis probabilístico.
        </motion.p>
      </div>

      <motion.div variants={item} className="flex flex-col gap-3">
        <div className="grid grid-cols-3 gap-2 md:grid-cols-6">
          {CAPAS.map((c) => (
            <div
              key={c.nombre}
              className={`rounded-md border px-3 py-2 text-center font-mono text-xs md:text-sm ${
                c.tipo === 'det'
                  ? 'border-neon/40 bg-neon/10 text-neon'
                  : 'border-emerald/40 bg-emerald/10 text-emerald'
              }`}
            >
              {c.nombre}
            </div>
          ))}
        </div>
        <div className="flex items-center justify-between font-mono text-[11px] uppercase tracking-widest text-mute">
          <span>assertEquals(esperado, real)</span>
          <span className="mx-3 h-px flex-1 bg-gradient-to-r from-neon/60 to-emerald/60" />
          <span>propiedades sobre N corridas</span>
        </div>
      </motion.div>
    </motion.section>
  );
}
