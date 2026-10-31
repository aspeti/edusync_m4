package com.edusync.shared.ai.application.monitoring;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Minimiza lo que puede ir a la bitácora. Nunca devuelve el JWT.
 */
public final class RedactorBitacora {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern TARJETA = Pattern.compile("\\b(?:\\d[ -]?){13,19}\\b");
    private static final Pattern RUDE = Pattern.compile("\\b\\d{12}\\b");
    private static final Pattern CI = Pattern.compile("\\b\\d{6,8}\\s?[A-Z]{2}\\b");
    private static final Pattern CORREO = Pattern.compile("\\b[\\w.+-]+@[\\w-]+\\.[\\w.]+\\b");

    private RedactorBitacora() {
    }

    public static String sujeto(String jwt) {
        String sub = extraerSub(jwt);
        if (sub == null || sub.isBlank()) {
            return "anonimo";
        }
        return "U-" + sha256(sub).substring(0, 12);
    }

    public static String texto(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String limpio = CORREO.matcher(valor).replaceAll("[CORREO]");
        limpio = TARJETA.matcher(limpio).replaceAll("[TARJETA]");
        limpio = CI.matcher(limpio).replaceAll("[CI]");
        limpio = RUDE.matcher(limpio).replaceAll("[RUDE]");
        return limpio;
    }

    public static String sha256(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String extraerSub(String jwt) {
        if (jwt == null || jwt.isBlank()) {
            return null;
        }
        String token = jwt.startsWith("Bearer ") ? jwt.substring("Bearer ".length()) : jwt;
        String[] partes = token.split("\\.");
        if (partes.length < 2) {
            return null;
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(partes[1]);
            JsonNode nodo = MAPPER.readTree(json);
            return nodo.path("sub").asString(null);
        } catch (IllegalArgumentException | tools.jackson.core.JacksonException ex) {
            return null;
        }
    }
}
