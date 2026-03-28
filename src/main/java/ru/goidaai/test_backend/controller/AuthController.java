package ru.goidaai.test_backend.controller;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.AuthResponse;
import ru.goidaai.test_backend.dto.DevLoginRequest;
import ru.goidaai.test_backend.dto.GoogleAuthRequest;
import ru.goidaai.test_backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
