package ru.goidaai.test_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GoogleAuthRequest(
    String idToken,
    String accessToken,
    @JsonAlias({"googleId", "googleSubject", "subject"})
    String googleSubject,
    String email,
    @JsonAlias({"displayName", "name", "fullName"})
    String displayName,
    @JsonAlias({"photoUrl", "avatarUrl", "pictureUrl"})
    String photoUrl
) {
}
