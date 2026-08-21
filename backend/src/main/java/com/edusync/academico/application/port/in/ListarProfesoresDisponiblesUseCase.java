package com.edusync.academico.application.port.in;

import com.edusync.academico.ProfesorResumen;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: catalogo minimo de profesores activos del tenant para el
 * {@code <select>} de la UI ({@code DD-UC-012}). No es {@code FSD-UC-019}.
 */
public interface ListarProfesoresDisponiblesUseCase {

  List<ProfesorResumen> listar(UUID tenantId);
}
