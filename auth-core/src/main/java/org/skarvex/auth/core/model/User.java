package org.skarvex.auth.core.model;

import java.util.UUID;

public record User(UUID id, String name, String passwordHash) {
}
