package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.EstadoInscripcion;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.InscripcionId;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InscripcionRepositoryAdapter implements InscripcionRepositoryPort {

  private final InscripcionJpaRepository jpaRepository;

  @Override
  public Inscripcion guardar(Inscripcion inscripcion) {
    InscripcionJpaEntity entity =
        new InscripcionJpaEntity(
            inscripcion.getId().valor(),
            inscripcion.getTenantId(),
            inscripcion.getEstudianteId().valor(),
            inscripcion.getGestionEscolarId().valor(),
            inscripcion.getCursoId().valor(),
            inscripcion.getParaleloId().valor(),
            inscripcion.getFechaInscripcion(),
            inscripcion.getEstado().name());
    return aDominio(jpaRepository.save(entity));
  }

  @Override
  public List<Inscripcion> listarPorEstudianteYTenant(EstudianteId estudianteId, UUID tenantId) {
    return jpaRepository.findByEstudianteIdAndTenantId(estudianteId.valor(), tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public boolean existePorEstudianteGestionYTenant(
      EstudianteId estudianteId, GestionEscolarId gestionEscolarId, UUID tenantId) {
    return jpaRepository.existsByEstudianteIdAndGestionEscolarIdAndTenantId(
        estudianteId.valor(), gestionEscolarId.valor(), tenantId);
  }

  private Inscripcion aDominio(InscripcionJpaEntity entity) {
    return Inscripcion.reconstruir(
        InscripcionId.de(entity.getId()),
        entity.getTenantId(),
        EstudianteId.de(entity.getEstudianteId()),
        GestionEscolarId.de(entity.getGestionEscolarId()),
        CursoId.de(entity.getCursoId()),
        ParaleloId.de(entity.getParaleloId()),
        entity.getFechaInscripcion(),
        EstadoInscripcion.valueOf(entity.getEstado()));
  }
}
