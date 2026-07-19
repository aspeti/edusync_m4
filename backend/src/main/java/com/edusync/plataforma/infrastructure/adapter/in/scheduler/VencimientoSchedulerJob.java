package com.edusync.plataforma.infrastructure.adapter.in.scheduler;

import com.edusync.plataforma.application.service.VencimientoSchedulerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job diario que marca {@code VENCIDO} a los Tenants sin renovacion ({@code FSD-UC-011},
 * paso 5). {@code @Scheduled} interno de Spring, suficiente para 1 instancia del
 * monolito ({@code DD-UC-003} &sect;2, alternativa A); si en el futuro se despliegan 2+
 * replicas, este job necesitara un bloqueo distribuido (p. ej. ShedLock).
 */
@Component
@RequiredArgsConstructor
public class VencimientoSchedulerJob {

  private static final Logger LOG = LoggerFactory.getLogger(VencimientoSchedulerJob.class);

  private final VencimientoSchedulerService vencimientoSchedulerService;

  @Scheduled(cron = "${edusync.plataforma.scheduler.vencimiento-cron:0 0 3 * * *}")
  public void ejecutar() {
    int marcados = vencimientoSchedulerService.marcarVencidos();
    if (marcados > 0) {
      LOG.info("Tenants marcados VENCIDO por el scheduler: {}", marcados);
    }
  }
}
