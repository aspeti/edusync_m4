package com.edusync.shared.ai.application.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Solo entran al contexto los archivos cuyo SHA-256 coincide con el manifiesto aprobado.
 */
public class ProcedenciaBaseConocimiento {

    public List<String> contenidosAprobados(Map<String, String> hashesAprobados, Map<String, String> contenidos) {
        List<String> aprobados = new ArrayList<>();
        for (Map.Entry<String, String> entrada : hashesAprobados.entrySet()) {
            String contenido = contenidos.get(entrada.getKey());
            if (contenido == null) {
                continue;
            }
            if (sha256(contenido).equals(entrada.getValue())) {
                aprobados.add(contenido);
            }
        }
        return List.copyOf(aprobados);
    }

    public static String sha256(String texto) {
        String normalizado = texto.replace("\r\n", "\n");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalizado.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
