package org.skarvex.auth.core.security;

public interface PasswordHasher {

    String hash(String password);

    boolean verify(String password, String hash);
}
