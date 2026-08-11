package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.in.ConsultarUsuarioUseCase;
import com.edusync.shared.ai.application.port.in.ExtraerConsultaUsuarioUseCase;
import com.edusync.shared.ai.application.port.out.BuscarUsuarioPorNombrePort;
import com.edusync.shared.ai.domain.ConsultaUsuarioDTO;
import com.edusync.shared.ai.domain.UsuarioResumen;
import com.edusync.shared.tenant.TenantContextProvider;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Extrae el nombre buscado del texto libre y busca coincidencias en el tenant del actor
 * autenticado ({@link TenantContextProvider}) — nunca en otro tenant. Cualquier usuario
 * autenticado del tenant puede invocarlo (decision explicita del usuario, 04/08/2026): no
 * esta restringido a roles administrativos.
 */
@Service
@RequiredArgsConstructor
public class ConsultarUsuarioService implements ConsultarUsuarioUseCase {

  private final ExtraerConsultaUsuarioUseCase extraerConsultaUsuarioUseCase;
  private final BuscarUsuarioPorNombrePort buscarUsuarioPorNombrePort;
  private final TenantContextProvider tenantContextProvider;

  @Override
  public List<UsuarioResumen> consultar(String texto) {
    ConsultaUsuarioDTO consulta = extraerConsultaUsuarioUseCase.extraer(texto);
    UUID tenantId = tenantContextProvider.tenantActual().orElseThrow();
    return buscarUsuarioPorNombrePort.buscarPorNombre(tenantId, consulta.nombreBuscado());
  }
}
