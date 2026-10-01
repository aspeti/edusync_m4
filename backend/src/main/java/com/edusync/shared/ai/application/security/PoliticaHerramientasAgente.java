package com.edusync.shared.ai.application.security;

import java.util.Map;
import java.util.Set;

/**
 * El modelo propone una herramienta; esta clase decide si se ejecuta.
 * Allow-list, confirmación de escrituras y tope de umbral viven aquí, no en el prompt.
 */
public class PoliticaHerramientasAgente {

    public static final int LIMITE_UMBRAL = 100;

    private final Set<String> permitidas;

    public PoliticaHerramientasAgente(Set<String> permitidas) {
        this.permitidas = Set.copyOf(permitidas);
    }

    public static PoliticaHerramientasAgente paraProfesor() {
        return new PoliticaHerramientasAgente(Set.of("consultar_academico", "list_mis_materias"));
    }

    public DecisionHerramienta evaluar(String nombre, Map<String, Object> argumentos, SesionAgente sesion) {
        if ("create_curso".equals(nombre) && !sesion.confirmaciones().contains("create_curso")) {
            return DecisionHerramienta.bloquear("escritura sin confirmación humana");
        }
        if (!permitidas.contains(nombre)) {
            return DecisionHerramienta.bloquear("herramienta '" + nombre + "' no está en la allow-list");
        }
        if ("consultar_academico".equals(nombre)) {
            Object umbral = argumentos.get("umbral");
            if (umbral instanceof Boolean || (umbral != null && !(umbral instanceof Number))) {
                return DecisionHerramienta.bloquear("umbral inválido");
            }
            if (umbral instanceof Number numero && numero.doubleValue() > LIMITE_UMBRAL) {
                return DecisionHerramienta.bloquear(
                        "umbral " + numero.intValue() + " supera el límite automático (" + LIMITE_UMBRAL + ")");
            }
        }
        return DecisionHerramienta.permitir();
    }
}
