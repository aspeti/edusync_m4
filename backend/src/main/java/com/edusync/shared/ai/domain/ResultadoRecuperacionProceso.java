package com.edusync.shared.ai.domain;

import java.util.List;

/**
 * Fragmentos recuperados del corpus de procesos EduSync (DD-UC-028).
 * {@code fuente} es el nombre de archivo, nunca PII.
 */
public record ResultadoRecuperacionProceso(List<FragmentoProceso> fragmentos, int puntuacionMaxima) {

    public ResultadoRecuperacionProceso {
        fragmentos = fragmentos == null ? List.of() : List.copyOf(fragmentos);
    }

    public boolean hayMatch() {
        return !fragmentos.isEmpty() && puntuacionMaxima >= RecuperacionUmbral.MINIMO;
    }

    public record FragmentoProceso(String fuente, String texto, int puntuacion) {
    }

    public static final class RecuperacionUmbral {
        public static final int MINIMO = 1;

        private RecuperacionUmbral() {
        }
    }
}
