package ru.goidaai.test_backend.security;

public interface GoogleTokenVerifier {

    GoogleUser verify(String idToken, String accessToken);
}
