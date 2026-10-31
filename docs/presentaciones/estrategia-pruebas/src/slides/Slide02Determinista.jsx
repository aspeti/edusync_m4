import { motion } from 'framer-motion';
import { Boxes, Network, MousePointerClick } from 'lucide-react';
import { SlideFrame, Tarjeta, Estado, Codigo, item } from '../components/ui.jsx';
import { unit, integracion, e2e } from '../data/metricas.js';

// Pirámide: ancho proporcional a la cantidad de tests de cada capa.
const PIRAMIDE = [
  { capa: 'E2E · Playwright', n: e2e.aceptados, ancho: '38%' },
  { capa: 'Integración · Testcontainers', n: integracion.tests, ancho: '62%' },
  { capa: 'Unit · JUnit 5 + Mockito', n: unit.tests, ancho: '100%' },
];

export default function Slide02Determinista() {
  return (
    <SlideFrame
      seccion="02 · La base determinista"
      titulo="Primero, que el código haga lo que dice"
      idea="Antes de evaluar al modelo aseguramos el software que lo rodea. Ninguna de estas pruebas llama al modelo ni a la red: el mismo commit da siempre el mismo resultado."
    >
      <div className="grid min-h-0 flex-1 grid-cols-1 gap-5 md:grid-cols-[1fr_1fr_1fr_0.9fr]">
        <Tarjeta icono={Boxes} titulo="Unit tests" estado={<Estado tipo="hecho" />}>
          <p>Componentes y funciones aisladas, con dobles solo en la frontera: modelo, disco y red.</p>
          <p className="font-mono text-sm text-text tnum">
            {unit.tests} tests · {unit.cobertura} líneas
          </p>
          <p className="text-sm">Mutación (Pitest) en dominio crítico: {unit.mutacion} de mutantes eliminados.</p>
        </Tarjeta>
        <Tarjeta icono={Network} titulo="Integración y contrato" estado={<Estado tipo="curso" texto="Contrato en curso" />}>
          <p>
            {integracion.tests} tests contra PostgreSQL real (Testcontainers): seguridad 401/403 y aislamiento entre tenants.
          </p>
          <p className="text-sm">
            Contrato: JSON Schema de <Codigo>{integracion.endpoint}</Codigo> con campos, catálogos y códigos HTTP.
          </p>
        </Tarjeta>
        <Tarjeta icono={MousePointerClick} titulo="End-to-End" estado={<Estado tipo="hecho" />}>
          <p>Flujos completos de usuario en el navegador con Playwright.</p>
          <p className="font-mono text-sm text-text tnum">
            {e2e.aceptados} de {e2e.planificados} casos aceptados
          </p>
          <p className="text-sm">El agente generó {e2e.agenteGenerados}; se aceptaron {e2e.agenteAceptados} tras auditoría humana.</p>
        </Tarjeta>

        <motion.figure variants={item} className="flex min-w-0 flex-col justify-end gap-2" aria-label="Pirámide de pruebas">
          {PIRAMIDE.map((p) => (
            <div key={p.capa} className="flex justify-center">
              <div
                style={{ width: p.ancho }}
                className="rounded-md border border-neon/40 bg-neon/10 px-2 py-2 text-center"
              >
                <p className="font-display text-2xl font-semibold leading-none text-neon tnum">{p.n}</p>
              </div>
            </div>
          ))}
          <figcaption className="pt-2 text-center font-mono text-[11px] leading-relaxed text-mute">
            E2E · Integración · Unit
            <br />
            tests activos por capa
          </figcaption>
        </motion.figure>
      </div>
    </SlideFrame>
  );
}
