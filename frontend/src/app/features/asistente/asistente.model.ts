export interface AgenteRequest {
  pregunta: string;
  confirmed?: boolean;
  history?: TurnoHistorialAgente[];
  contexto?: ContextoConsultaAgente | null;
}

export interface TurnoHistorialAgente {
  role: 'user' | 'assistant' | string;
  content: string;
}

export interface ContextoConsultaAgente {
  estudianteId?: string | null;
  estudianteEtiqueta?: string | null;
  cursoId?: string | null;
  paraleloId?: string | null;
  paraleloEtiqueta?: string | null;
  materiaId?: string | null;
  materiaEtiqueta?: string | null;
  periodoId?: string | null;
  periodoEtiqueta?: string | null;
  ultimaOperacion?: string | null;
}

export interface PasoTrazaAgente {
  paso: number;
  toolId: string;
  tablasFuente: string[];
  exito: boolean;
}

export interface AgenteResponse {
  respuesta: string;
  herramientasUsadas: string[];
  turnos: number;
  camino: string;
  fuente: string;
  agente?: string;
  steps?: PasoTrazaAgente[];
  confirmacionRequerida?: boolean;
  contexto?: ContextoConsultaAgente | null;
}

export interface MensajeAsistente {
  role: 'user' | 'assistant';
  content: string;
  camino?: string;
}

export const STORAGE_HISTORIAL = 'edusync.asistente.historial';
export const STORAGE_CONTEXTO = 'edusync.asistente.contexto';
export const MAX_TURNOS_HISTORIAL = 12;

/** Frases del catálogo KEYWORD (DD-UC-025) y ejemplos CONSULTA (DD-UC-026). */
export const FRASES_KEYWORD: readonly string[] = [
  '¿Cuántos cursos hay?',
  '¿Cuál es la gestión activa?',
  'Lista los periodos',
  'Lista las secciones',
  'Lista los estudiantes',
  'Lista las materias',
  'Mis materias',
  'Lista los profesores',
  'Lista las gestiones',
  'Lista los usuarios',
  'Crea el curso 1ro A',
  'Crea la materia Matemática',
  'Notas de Juan del primer trimestre',
  'Notas de Juan en Matemática',
  '¿Cuál es el promedio de 1ro A durante el segundo trimestre?',
  '¿Qué alumnos reprobaron Matemática?',
  '¿Qué alumnos están en 1ro A?',
  '¿Qué materias tiene Juan?',
];
