package com.edusync.plataforma.application.port.in;

import com.edusync.plataforma.domain.Tenant;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;

/**
 * Puerto de entrada: listado paginado y filtrable de Tenants para la consola SysAdmin
 * (DD-UC-004 §2, filtros/paginacion DD-UC-007).
 */
public interface ListarTenantsUseCase {

  PageResult<Tenant> listar(TenantFiltro filtro, PageQuery pageQuery);
}
