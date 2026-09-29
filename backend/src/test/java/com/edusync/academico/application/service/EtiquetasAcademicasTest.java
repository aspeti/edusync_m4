package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EtiquetasAcademicasTest {

  private CursoRepositoryPort cursoRepositoryPort;
  private ParaleloRepositoryPort paraleloRepositoryPort;
  private GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private EtiquetasAcademicas etiquetas;

  @BeforeEach
  void setUp() {
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    paraleloRepositoryPort = mock(ParaleloRepositoryPort.class);
    gestionEscolarRepositoryPort = mock(GestionEscolarRepositoryPort.class);
    etiquetas =
        new EtiquetasAcademicas(cursoRepositoryPort, paraleloRepositoryPort, gestionEscolarRepositoryPort);
  }

  @Test
  void resuelveNombresDelTenant() {
    UUID tenantId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    UUID paraleloId = UUID.randomUUID();
    UUID gestionId = UUID.randomUUID();
    when(cursoRepositoryPort.buscarPorIdYTenant(CursoId.de(cursoId), tenantId))
        .thenReturn(Optional.of(Curso.reconstruir(CursoId.de(cursoId), tenantId, "Primero de Primaria")));
    when(paraleloRepositoryPort.buscarPorIdYTenant(ParaleloId.de(paraleloId), tenantId))
        .thenReturn(
            Optional.of(Paralelo.reconstruir(ParaleloId.de(paraleloId), tenantId, CursoId.de(cursoId), "A")));
    when(gestionEscolarRepositoryPort.buscarPorIdYTenant(GestionEscolarId.de(gestionId), tenantId))
        .thenReturn(
            Optional.of(
                GestionEscolar.reconstruir(
                    GestionEscolarId.de(gestionId),
                    tenantId,
                    "2026",
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 11, 30),
                    EstadoGestionEscolar.ACTIVA)));

    assertThat(etiquetas.nombreCurso(tenantId, cursoId)).isEqualTo("Primero de Primaria");
    assertThat(etiquetas.nombreParalelo(tenantId, paraleloId)).isEqualTo("A");
    assertThat(etiquetas.nombreGestion(tenantId, gestionId)).isEqualTo("2026");
  }

  @Test
  void referenciaHuerfanaDevuelveNull() {
    UUID tenantId = UUID.randomUUID();
    UUID cursoId = UUID.randomUUID();
    when(cursoRepositoryPort.buscarPorIdYTenant(CursoId.de(cursoId), tenantId)).thenReturn(Optional.empty());

    assertThat(etiquetas.nombreCurso(tenantId, cursoId)).isNull();
  }
}
