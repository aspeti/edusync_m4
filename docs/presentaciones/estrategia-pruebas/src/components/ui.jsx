import { motion } from 'framer-motion';

// Entrada escalonada de los hijos de un slide. Arranca casi visible para que una captura
// a mitad de animación siga mostrando el contenido.
export const contenedor = {
  oculto: {},
  visible: { transition: { staggerChildren: 0.07, delayChildren: 0.05 } },
};
export const item = {
  oculto: { opacity: 0.15, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.45, ease: [0.22, 1, 0.36, 1] } },
};

export function SlideFrame({ seccion, titulo, idea, children, className = '' }) {
  return (
    <motion.section
      variants={contenedor}
      initial="oculto"
      animate="visible"
      className={`relative flex h-full w-full flex-col gap-6 px-6 py-8 md:gap-8 md:px-16 md:py-12 ${className}`}
    >
      <motion.header variants={item} className="flex flex-col gap-3">
        {seccion && (
          <p className="font-mono text-xs uppercase tracking-[0.18em] text-neon">{seccion}</p>
        )}
        {titulo && (
          <h2 className="font-display text-3xl font-semibold leading-tight text-text md:text-[2.75rem]">
            {titulo}
          </h2>
        )}
        {idea && <p className="max-w-[62ch] text-base leading-relaxed text-mute md:text-lg">{idea}</p>}
      </motion.header>
      {children}
    </motion.section>
  );
}

const ESTADOS = {
  hecho: { texto: 'Hecho', cls: 'border-emerald/40 bg-emerald/10 text-emerald' },
  aplicado: { texto: 'Aplicado', cls: 'border-emerald/40 bg-emerald/10 text-emerald' },
  curso: { texto: 'En curso', cls: 'border-amber/40 bg-amber/10 text-amber' },
  proximo: { texto: 'Próximo paso', cls: 'border-amber/40 bg-amber/10 text-amber' },
  fuera: { texto: 'Fuera de alcance', cls: 'border-rose/40 bg-rose/10 text-rose' },
  nocubierto: { texto: 'Sin cubrir aún', cls: 'border-amber/40 bg-amber/10 text-amber' },
};

export function Estado({ tipo, texto }) {
  const e = ESTADOS[tipo];
  return (
    <span className={`inline-flex shrink-0 items-center rounded-full border px-2.5 py-0.5 font-mono text-[11px] uppercase tracking-wider ${e.cls}`}>
      {texto ?? e.texto}
    </span>
  );
}

export function Tarjeta({ icono: Icono, titulo, estado, children, acento = 'neon', className = '' }) {
  const color = {
    neon: 'text-neon bg-neon/10',
    emerald: 'text-emerald bg-emerald/10',
    amber: 'text-amber bg-amber/10',
    rose: 'text-rose bg-rose/10',
  }[acento];
  return (
    <motion.article
      variants={item}
      className={`flex min-w-0 flex-col gap-3 rounded-xl border border-line bg-panel/80 p-5 ${className}`}
    >
      <div className="flex items-center justify-between gap-3">
        {Icono && (
          <span className={`grid h-9 w-9 shrink-0 place-items-center rounded-lg ${color}`}>
            <Icono size={18} strokeWidth={2} aria-hidden="true" />
          </span>
        )}
        {estado}
      </div>
      <h3 className="font-display text-xl font-semibold leading-snug text-text">{titulo}</h3>
      <div className="flex flex-col gap-2 text-[15px] leading-relaxed text-mute">{children}</div>
    </motion.article>
  );
}

export function Codigo({ children }) {
  return <code className="rounded bg-ink/80 px-1.5 py-0.5 font-mono text-[0.85em] text-text">{children}</code>;
}

export function Dato({ valor, unidad, etiqueta, detalle, acento = 'emerald' }) {
  const color = { emerald: 'text-emerald', neon: 'text-neon', amber: 'text-amber' }[acento];
  return (
    <motion.div variants={item} className="flex min-w-0 flex-col gap-2 border-t border-line pt-5">
      <p className={`font-display text-6xl font-semibold leading-none tnum md:text-7xl ${color}`}>
        {valor}
        {unidad && <span className="ml-1 text-3xl md:text-4xl">{unidad}</span>}
      </p>
      <p className="text-base font-medium text-text md:text-lg">{etiqueta}</p>
      {detalle && <p className="text-sm leading-relaxed text-mute">{detalle}</p>}
    </motion.div>
  );
}
