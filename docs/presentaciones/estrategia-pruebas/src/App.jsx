import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { AnimatePresence, MotionConfig, motion } from 'framer-motion';
import { ChevronLeft, ChevronRight, Maximize2 } from 'lucide-react';

import Slide01Titulo from './slides/Slide01Titulo.jsx';
import Slide02Determinista from './slides/Slide02Determinista.jsx';
import Slide03RedTeam from './slides/Slide03RedTeam.jsx';
import Slide04Evals from './slides/Slide04Evals.jsx';
import Slide05Resultados from './slides/Slide05Resultados.jsx';
import Slide06Alcance from './slides/Slide06Alcance.jsx';
import Slide07Conclusiones from './slides/Slide07Conclusiones.jsx';

const SLIDES = [
  { titulo: 'Título', C: Slide01Titulo },
  { titulo: 'Base determinista', C: Slide02Determinista },
  { titulo: 'Red teaming', C: Slide03RedTeam },
  { titulo: 'Evals offline', C: Slide04Evals },
  { titulo: 'Resultados', C: Slide05Resultados },
  { titulo: 'Alcance', C: Slide06Alcance },
  { titulo: 'Conclusiones', C: Slide07Conclusiones },
];

const ANCHO = 1280;
const ALTO = 720;

function useEsAncho() {
  const q = '(min-width: 768px)';
  const [ancho, setAncho] = useState(() => window.matchMedia(q).matches);
  useEffect(() => {
    const m = window.matchMedia(q);
    const f = () => setAncho(m.matches);
    m.addEventListener('change', f);
    return () => m.removeEventListener('change', f);
  }, []);
  return ancho;
}

function indiceDesdeHash() {
  const n = parseInt(window.location.hash.replace('#', ''), 10);
  return Number.isFinite(n) && n >= 1 && n <= SLIDES.length ? n - 1 : 0;
}

// Escenario 16:9 de 1280×720 escalado al espacio disponible.
function Escenario({ indice, direccion }) {
  const marco = useRef(null);
  const [escala, setEscala] = useState(1);

  useLayoutEffect(() => {
    const el = marco.current;
    const medir = () => {
      const { width, height } = el.getBoundingClientRect();
      setEscala(Math.min(width / ANCHO, height / ALTO));
    };
    medir();
    const ro = new ResizeObserver(medir);
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  const { C } = SLIDES[indice];
  return (
    <div ref={marco} className="relative grid min-h-0 flex-1 place-items-center overflow-hidden">
      <div
        style={{ width: ANCHO * escala, height: ALTO * escala }}
        className="relative overflow-hidden rounded-2xl border border-line shadow-[0_30px_80px_-20px_rgb(0_0_0/0.7)]"
      >
        <div
          style={{ width: ANCHO, height: ALTO, transform: `scale(${escala})`, transformOrigin: 'top left' }}
          className="grid-bg absolute left-0 top-0 bg-ink"
        >
          <AnimatePresence mode="wait" custom={direccion}>
            <motion.div
              key={indice}
              custom={direccion}
              initial={{ opacity: 0, x: direccion * 40 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: direccion * -40 }}
              transition={{ duration: 0.28, ease: 'easeOut' }}
              className="h-full w-full"
            >
              <C />
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
}

export default function App() {
  const esAncho = useEsAncho();
  const [indice, setIndice] = useState(indiceDesdeHash);
  const [direccion, setDireccion] = useState(1);

  const ir = useCallback((n) => {
    setIndice((actual) => {
      const destino = Math.max(0, Math.min(SLIDES.length - 1, n));
      setDireccion(destino >= actual ? 1 : -1);
      return destino;
    });
  }, []);

  useEffect(() => {
    try {
      history.replaceState(null, '', `#${indice + 1}`);
    } catch {
      /* el visor puede bloquear history: no es crítico */
    }
  }, [indice]);

  useEffect(() => {
    if (!esAncho) return undefined;
    const tecla = (e) => {
      if (['ArrowRight', 'PageDown', ' '].includes(e.key)) { e.preventDefault(); ir(indice + 1); }
      if (['ArrowLeft', 'PageUp'].includes(e.key)) { e.preventDefault(); ir(indice - 1); }
      if (e.key === 'Home') ir(0);
      if (e.key === 'End') ir(SLIDES.length - 1);
      if (e.key === 'f' || e.key === 'F') pantallaCompleta();
    };
    window.addEventListener('keydown', tecla);
    return () => window.removeEventListener('keydown', tecla);
  }, [esAncho, indice, ir]);

  const pantallaCompleta = () => {
    const el = document.documentElement;
    if (document.fullscreenElement) document.exitFullscreen?.().catch(() => {});
    else el.requestFullscreen?.().catch(() => {});
  };

  // En pantallas angostas, los slides se apilan en una lectura vertical sin escalar.
  if (!esAncho) {
    return (
      <MotionConfig reducedMotion="user">
        <main className="grid-bg flex flex-col gap-4 bg-ink px-4 py-4">
          {SLIDES.map(({ titulo, C }) => (
            <div key={titulo} className="overflow-hidden rounded-2xl border border-line bg-ink">
              <C />
            </div>
          ))}
        </main>
      </MotionConfig>
    );
  }

  return (
    <MotionConfig reducedMotion="user">
      <main className="flex h-full flex-col gap-4 bg-ink px-6 py-5">
        <Escenario indice={indice} direccion={direccion} />

        <nav className="flex items-center justify-between gap-4" aria-label="Navegación de slides">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => ir(indice - 1)}
              disabled={indice === 0}
              className="grid h-9 w-9 place-items-center rounded-lg border border-line text-text transition hover:border-neon hover:text-neon disabled:opacity-30"
              aria-label="Slide anterior"
            >
              <ChevronLeft size={18} />
            </button>
            <button
              type="button"
              onClick={() => ir(indice + 1)}
              disabled={indice === SLIDES.length - 1}
              className="grid h-9 w-9 place-items-center rounded-lg border border-line text-text transition hover:border-neon hover:text-neon disabled:opacity-30"
              aria-label="Slide siguiente"
            >
              <ChevronRight size={18} />
            </button>
          </div>

          <ol className="flex items-center gap-1.5">
            {SLIDES.map((s, i) => (
              <li key={s.titulo}>
                <button
                  type="button"
                  onClick={() => ir(i)}
                  aria-label={`Ir a ${s.titulo}`}
                  aria-current={i === indice ? 'step' : undefined}
                  className={`h-1.5 rounded-full transition-all ${i === indice ? 'w-8 bg-neon' : 'w-3 bg-line hover:bg-mute'}`}
                />
              </li>
            ))}
          </ol>

          <div className="flex items-center gap-3">
            <span className="font-mono text-xs text-mute tnum">
              {String(indice + 1).padStart(2, '0')} / {String(SLIDES.length).padStart(2, '0')} · {SLIDES[indice].titulo}
            </span>
            <button
              type="button"
              onClick={pantallaCompleta}
              className="grid h-9 w-9 place-items-center rounded-lg border border-line text-text transition hover:border-neon hover:text-neon"
              aria-label="Pantalla completa (F)"
              title="Pantalla completa (F)"
            >
              <Maximize2 size={16} />
            </button>
          </div>
        </nav>
      </main>
    </MotionConfig>
  );
}
