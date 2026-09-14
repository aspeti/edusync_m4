package com.edusync.shared.ai.application.port.out;

import com.edusync.shared.ai.domain.HerramientaLlm;

import java.util.List;

/**
 * Descubre el catalogo de herramientas de solo lectura expuestas por
 * el propio backend. Debe aplicar EXACTAMENTE el filtro de seguridad
 * de ADR-0018 seccion 3 (identico al de DescubridorTools en
 * edusync-agente-llm): prefijo /api/v1/**, excluye /api/v1/auth/**,
 * /api/v1/plataforma/**, /api/v1/ai/**; solo GET, o POST cuyo path
 * contenga una palabra de consulta explicita (consultar, buscar,
 * obtener, listar, search, query).
 */
public interface DescubridorHerramientasPort {
    List<HerramientaLlm> descubrir();
}
