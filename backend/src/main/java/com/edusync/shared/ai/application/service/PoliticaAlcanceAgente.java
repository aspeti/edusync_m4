package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.LlamadaHerramienta;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * El modelo no elige tenant, colegio ni el directorio de cuentas (ADR-0022).
 * El rol sale de la autenticacion de la peticion, no del texto.
 */
public class PoliticaAlcanceAgente {

    static final Set<String> CLAVES_DE_ALCANCE = Set.of(
            "tenantId", "tenant_id", "schoolId", "school_id");

    private static final String LISTAR_USUARIOS = "list_usuarios";

    private static final Set<String> ROLES_DIRECTORIO = Set.of(
            "ROLE_ADMIN", "ROLE_SECRETARIA", "ROLE_SYSADMIN");

    public LlamadaHerramienta sanear(LlamadaHerramienta llamada) {
        Map<String, Object> limpios = new LinkedHashMap<>();
        llamada.argumentos().forEach((clave, valor) -> {
            if (!CLAVES_DE_ALCANCE.contains(clave)) {
                limpios.put(clave, valor);
            }
        });
        if (limpios.size() == llamada.argumentos().size()) {
            return llamada;
        }
        return new LlamadaHerramienta(llamada.id(), llamada.nombreHerramienta(), limpios);
    }

    public boolean puedeEjecutar(String toolId) {
        if (!LISTAR_USUARIOS.equals(toolId)) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> ROLES_DIRECTORIO.contains(a.getAuthority()));
    }
}
