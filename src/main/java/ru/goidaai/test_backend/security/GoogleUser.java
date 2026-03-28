package ru.goidaai.test_backend.security;

public record GoogleUser(
    String subject,
    String email,
    String fullName,
    String pictureUrl,
    boolean emailVerified
) {
}
