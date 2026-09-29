package com.edusync.academico.application.service;

import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resuelve nombres de curso, paralelo y gestion para DTOs de lectura ({@code DD-UC-012}/
 * {@code DD-UC-013}). No hidrata grafos de agregados: solo etiquetas; {@code null} si la
 * referencia estuviera huerfana (mismo criterio que {@code AsignacionProfesorVista}).
 */
@Component
@RequiredArgsConstructor
public class EtiquetasAcademicas {

  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;
  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;

  @Transactional(readOnly = true)
  public String nombreCurso(UUID tenantId, UUID cursoId) {
    return cursoRepositoryPort
        .buscarPorIdYTenant(CursoId.de(cursoId), tenantId)
        .map(Curso::getNombre)
        .orElse(null);
  }

  @Transactional(readOnly = true)
  public String nombreParalelo(UUID tenantId, UUID paraleloId) {
    return paraleloRepositoryPort
        .buscarPorIdYTenant(ParaleloId.de(paraleloId), tenantId)
        .map(Paralelo::getNombre)
        .orElse(null);
  }

  @Transactional(readOnly = true)
  public String nombreGestion(UUID tenantId, UUID gestionEscolarId) {
    return gestionEscolarRepositoryPort
        .buscarPorIdYTenant(GestionEscolarId.de(gestionEscolarId), tenantId)
        .map(GestionEscolar::getNombre)
        .orElse(null);
  }
}
