import { motion } from 'framer-motion';
import { Database, Gavel, BookOpenCheck, Equal } from 'lucide-react';
import { SlideFrame, Tarjeta, Estado, Codigo, item } from '../components/ui.jsx';

// Cómo decidimos si una respuesta es correcta cuando el texto cambia entre corridas.
const COMPARACION = [
  { modo: 'Exact match', nota: 'Frágil: falla si el modelo cambia una coma', estado: 'fuera', etiqueta: 'No se usa' },
  { modo: 'Propiedades', nota: 'texto_contiene · herramienta_ejecutada · argumento ausente', estado: 'aplicado', etiqueta: 'En uso' },
  { modo: 'Similaridad semántica', nota: 'Embeddings o juez sobre la respuesta completa', estado: 'proximo', etiqueta: 'Próximo' },
];

export default function Slide04Evals() {
  return (
    <SlideFrame
      seccion="04 · Evaluaciones offline"
      titulo="Medir la calidad de lo que genera"
      idea="Datasets fijos que se corren contra el asistente para medir sus respuestas. Hoy evaluamos propiedades de seguridad; las métricas de calidad del contenido son el siguiente paso."
    >
      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <Tarjeta icono={Database} titulo="Golden dataset" estado={<Estado tipo="aplicado" texto="En uso" />}>
          <p>
            <Codigo>golden.json</Codigo>: cada caso declara propiedades esperadas (secreto ausente, tenant correcto, herramienta autorizada), no un párrafo ideal.
          </p>
        </Tarjeta>
        <Tarjeta icono={Gavel} titulo="LLM-as-a-Judge" acento="amber" estado={<Estado tipo="proximo" />}>
          <p>Un modelo más capaz califica las respuestas del modelo local (llama3.1:8b) con una rúbrica fija.</p>
        </Tarjeta>
        <Tarjeta icono={BookOpenCheck} titulo="Métricas RAG" acento="amber" estado={<Estado tipo="proximo" />}>
          <p>
            <span className="text-text">Faithfulness:</span> ¿la respuesta se apoya en el contexto recuperado?
          </p>
          <p>
            <span className="text-text">Answer relevance:</span> ¿contesta la pregunta del usuario?
          </p>
        </Tarjeta>
      </div>

      <motion.div variants={item} className="flex flex-col gap-3 rounded-xl border border-line bg-panel/60 p-5">
        <div className="flex items-center gap-3">
          <Equal size={18} className="text-neon" aria-hidden="true" />
          <h3 className="font-display text-xl font-semibold text-text">Exact match vs. similaridad semántica</h3>
        </div>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
          {COMPARACION.map((c) => (
            <div key={c.modo} className="flex min-w-0 flex-col gap-2 border-l-2 border-line pl-4">
              <div className="flex flex-wrap items-center gap-2">
                <span className="font-medium text-text">{c.modo}</span>
                <Estado tipo={c.estado} texto={c.etiqueta} />
              </div>
              <p className="font-mono text-xs leading-relaxed text-mute">{c.nota}</p>
            </div>
          ))}
        </div>
      </motion.div>
    </SlideFrame>
  );
}
