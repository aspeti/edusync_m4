package com.edusync.shared.ai.application.security;

import java.util.List;

/**
 * Arma el texto que sí puede ver el modelo.
 * El secreto recibido no se concatena. Las fichas de otro curso tampoco.
 */
public class ContextoAsistenteSeguro {

    public String armar(SesionAgente sesion, String secretoConocido, List<FichaVisible> fichas) {
        StringBuilder texto = new StringBuilder();
        texto.append("Atiendes al usuario autenticado ").append(sesion.usuarioId());
        texto.append(". Rol ").append(sesion.rol()).append(".\n");
        for (FichaVisible ficha : fichas) {
            if (sesion.cursos().contains(ficha.curso())) {
                texto.append(ficha.id()).append(' ')
                        .append(ficha.correo()).append(' ')
                        .append(ficha.marcador()).append('\n');
            }
        }
        return texto.toString().replace(secretoConocido == null ? "\u0000" : secretoConocido, "");
    }
}
