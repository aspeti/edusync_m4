package com.edusync.shared.ai.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.shared.ai.application.port.in.ExtraerConsultaUsuarioUseCase;
import com.edusync.shared.ai.application.port.out.BuscarUsuarioPorNombrePort;
import com.edusync.shared.ai.domain.ConsultaUsuarioDTO;
import com.edusync.shared.ai.domain.UsuarioResumen;
import com.edusync.shared.tenant.TenantContextProvider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarUsuarioServiceTest {

  @Mock private ExtraerConsultaUsuarioUseCase extraerConsultaUsuarioUseCase;
  @Mock private BuscarUsuarioPorNombrePort buscarUsuarioPorNombrePort;
  @Mock private TenantContextProvider tenantContextProvider;

  @Test
  void extraeElNombreYBuscaSoloEnElTenantDelActor() {
    UUID tenantId = UUID.randomUUID();
    ConsultarUsuarioService service =
        new ConsultarUsuarioService(extraerConsultaUsuarioUseCase, buscarUsuarioPorNombrePort, tenantContextProvider);

    when(extraerConsultaUsuarioUseCase.extraer("no recuerdo el correo de Roberto"))
        .thenReturn(new ConsultaUsuarioDTO("Roberto"));
    when(tenantContextProvider.tenantActual()).thenReturn(Optional.of(tenantId));
    when(buscarUsuarioPorNombrePort.buscarPorNombre(tenantId, "Roberto"))
        .thenReturn(List.of(new UsuarioResumen("Roberto Fernandez", "roberto@colegio.edu.bo", Set.of("PROFESOR"), true)));

    List<UsuarioResumen> resultado = service.consultar("no recuerdo el correo de Roberto");

    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).nombreCompleto()).isEqualTo("Roberto Fernandez");
    verify(buscarUsuarioPorNombrePort).buscarPorNombre(eq(tenantId), eq("Roberto"));
  }

  @Test
  void devuelveListaVaciaCuandoNoHayCoincidencias() {
    UUID tenantId = UUID.randomUUID();
    ConsultarUsuarioService service =
        new ConsultarUsuarioService(extraerConsultaUsuarioUseCase, buscarUsuarioPorNombrePort, tenantContextProvider);

    when(extraerConsultaUsuarioUseCase.extraer("busco a alguien que no existe"))
        .thenReturn(new ConsultaUsuarioDTO("Nadie"));
    when(tenantContextProvider.tenantActual()).thenReturn(Optional.of(tenantId));
    when(buscarUsuarioPorNombrePort.buscarPorNombre(tenantId, "Nadie")).thenReturn(List.of());

    List<UsuarioResumen> resultado = service.consultar("busco a alguien que no existe");

    assertThat(resultado).isEmpty();
  }
}
