package ru.goidaai.test_backend.adapter.in.rest;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.AuthResponse;
import ru.goidaai.test_backend.dto.DevLoginRequest;
import ru.goidaai.test_backend.dto.GoogleAuthRequest;
import ru.goidaai.test_backend.dto.TokenRefreshRequest;
import ru.goidaai.test_backend.service.AuthService;
import ru.goidaai.test_backend.service.TokenRefreshService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenRefreshService tokenRefreshService;

    public AuthController(AuthService authService, TokenRefreshService tokenRefreshService) {
        this.authService = authService;
        this.tokenRefreshService = tokenRefreshService;
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleAuthRequest request) {
        return authService.googleLogin(request);
    }

    @PostMapping("/dev")
    @Profile("!prod")
    public AuthResponse devLogin(@Valid @RequestBody DevLoginRequest request) {
        return authService.devLogin(request);
    }

    /**
     * Обновить access токен
     * Принимает старый токен (может быть истекшим) и возвращает новый
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
        @Valid @RequestBody TokenRefreshRequest request
    ) {
        AuthResponse response = tokenRefreshService.refreshToken(request.getToken());
        return ResponseEntity.ok(response);
    }
}
