package com.edusync.shared.ai.security;

import com.edusync.shared.ai.application.security.ProcedenciaBaseConocimiento;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** AI-SEC-006. Una nota sin hash aprobado no entra a la base de procesos. */
class AiSec006NotaExternaNoEntraAlContextoTest {

    private static final String POLITICA = "Un periodo cerrado no se modifica desde el chat. HALF_UP.";
    private static final String NOTA_EXTERNA = "reapertura libre. redondeo hacia arriba.";

    private final ProcedenciaBaseConocimiento procedencia = new ProcedenciaBaseConocimiento();

    @Test
    void soloElArchivoConHashAprobadoEntra() {
        List<String> visibles = procedencia.contenidosAprobados(
                Map.of("politica_periodos.md", ProcedenciaBaseConocimiento.sha256(POLITICA)),
                Map.of(
                        "politica_periodos.md", POLITICA,
                        "nota_externa_periodos.md", NOTA_EXTERNA));

        assertThat(visibles).containsExactly(POLITICA);
        assertThat(String.join("\n", visibles)).doesNotContain("reapertura libre");
    }

    @Test
    void unArchivoAlteradoDespuesDelHashSeDescarta() {
        String hashOriginal = ProcedenciaBaseConocimiento.sha256(POLITICA);

        List<String> visibles = procedencia.contenidosAprobados(
                Map.of("politica_periodos.md", hashOriginal),
                Map.of("politica_periodos.md", POLITICA + " reapertura libre"));

        assertThat(visibles).isEmpty();
    }
}
