import { motion } from 'framer-motion';
import { SlideFrame, Dato, item } from '../components/ui.jsx';
import { unit, e2e, redteam, fuente } from '../data/metricas.js';

export default function Slide05Resultados() {
  return (
    <SlideFrame seccion="05 · Resultados" titulo="Lo que medimos">
      <div className="grid flex-1 grid-cols-1 content-center gap-x-10 gap-y-8 md:grid-cols-4">
        <Dato
          valor={unit.cobertura.replace(' %', '')}
          unidad="%"
          acento="neon"
          etiqueta="Cobertura de líneas"
          detalle={`domain + application, JaCoCo, ${unit.totalTests} tests. Meta del gate: 80 %.`}
        />
        <Dato
          valor={`${redteam.exitosDeterminista}/${redteam.ataques}`}
          etiqueta="Ataques con éxito"
          detalle="Gate determinista con modelo hostil, en cada mvn test, tras la mitigación."
        />
        <Dato
          valor={`${redteam.antes.split('/')[0]}→${redteam.despues.split('/')[0]}`}
          etiqueta="Fuga del directorio en vivo"
          detalle={`${redteam.ataqueVivo} con Ollama, 3 corridas: ${redteam.antes} antes, ${redteam.despues} después del control.`}
        />
        <Dato
          valor={`${e2e.aceptados}/${e2e.planificados}`}
          acento="neon"
          etiqueta="Casos E2E aceptados"
          detalle="Planificados con agente, auditados uno por uno con checklist de 5 preguntas."
        />
      </div>
      <motion.p variants={item} className="font-mono text-[11px] text-mute">
        Fuentes: {fuente.jacoco} · {fuente.redteam} · {fuente.e2e}
      </motion.p>
    </SlideFrame>
  );
}
