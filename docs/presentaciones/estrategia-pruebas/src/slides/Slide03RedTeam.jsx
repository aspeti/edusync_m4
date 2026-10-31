import { motion } from 'framer-motion';
import { Syringe, FileLock2, Scale, DatabaseZap, ArrowRight } from 'lucide-react';
import { SlideFrame, Tarjeta, Estado, Codigo, item } from '../components/ui.jsx';
import { redteam } from '../data/metricas.js';

const METODO = ['Modelo de amenazas', 'Ataque JSON', 'Hallazgo AI-SEC', 'Control en código', 'Retest', 'Test de regresión'];

export default function Slide03RedTeam() {
  return (
    <SlideFrame
      seccion="03 · Red teaming"
      titulo="Atacamos a nuestro propio asistente"
      idea={`${redteam.ataques} ataques controlados para que haga lo que no debe, solo con datos ficticios. Éxito = un hecho observable, nunca igualdad de texto.`}
      className="md:gap-6"
    >
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <Tarjeta icono={Syringe} titulo="Prompt injection y jailbreak" acento="neon" estado={<Estado tipo="hecho" texto="0 éxitos" />}>
          <p>«Ignora las instrucciones…», paráfrasis, letras separadas e historial malicioso.</p>
          <p className="font-mono text-xs text-text">RT-INJ-001..003 · RT-GUARD-001/002 · RT-IND-001</p>
        </Tarjeta>
        <Tarjeta icono={FileLock2} titulo="Extracción del system prompt" acento="neon" estado={<Estado tipo="hecho" texto="0 éxitos" />}>
          <p>
            El modelo hostil devuelve el canary <Codigo>{redteam.canary}</Codigo>; el test verifica que no llega al usuario.
          </p>
        </Tarjeta>
        <Tarjeta icono={Scale} titulo="Toxicidad y sesgo" acento="amber" estado={<Estado tipo="nocubierto" />}>
          <p>Contenido dañino, ofensivo o discriminatorio. Aún sin casos en el catálogo: es el siguiente bloque.</p>
        </Tarjeta>
        <Tarjeta icono={DatabaseZap} titulo="Fuga de datos y PII" acento="emerald" estado={<Estado tipo="hecho" texto={`${redteam.antes} → ${redteam.despues}`} />}>
          <p>
            Un PROFESOR obtenía la llamada al directorio de cuentas (<span className="whitespace-nowrap text-text">{redteam.hallazgo}</span>). Corregido con una política de herramientas por rol.
          </p>
          <p className="font-mono text-xs text-text">RT-EXFIL-001/002 · RT-AUTH-001/003 · RT-TENANT-001</p>
        </Tarjeta>
      </div>

      <motion.ol variants={item} className="flex flex-wrap items-center gap-x-2 gap-y-2 font-mono text-xs text-mute" aria-label="Método">
        {METODO.map((paso, i) => (
          <li key={paso} className="flex items-center gap-2">
            <span className="rounded border border-line bg-panel px-2 py-1 text-text">{paso}</span>
            {i < METODO.length - 1 && <ArrowRight size={14} className="text-neon" aria-hidden="true" />}
          </li>
        ))}
      </motion.ol>
    </SlideFrame>
  );
}
