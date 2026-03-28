package ru.goidaai.test_backend.dto;

public record AuthResponse(
    String tokenType,
    String accessToken,
    String sessionToken,
    String token,
    long expiresIn,
    UserDTO user
) {

    public AuthResponse(String tokenType, String accessToken, long expiresIn, UserDTO user) {
        this(tokenType, accessToken, accessToken, accessToken, expiresIn, user);
    }
}
