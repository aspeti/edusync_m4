package com.edusync.identidad.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/** Claims extraidos y validados de un JWT (userId, tenantId, roles). */
public record ClaimsToken(UUID userId, UUID tenantId, Set<String> roles) {
}
