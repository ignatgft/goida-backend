package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.service.ProfileService;
import ru.goidaai.test_backend.dto.UserDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для настроек пользователя
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(profileService.getProfile(jwt.getSubject()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String baseCurrency,
        @RequestParam(required = false) Double monthlyBudget,
        @RequestParam(required = false) String language,
        @RequestParam(required = false) String theme
    ) {
        UserDTO updated = profileService.updateProfile(
            jwt.getSubject(),
            name,
            baseCurrency,
            monthlyBudget,
            language,
            theme
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam MultipartFile file
    ) {
        String avatarUrl = profileService.uploadAvatar(jwt.getSubject(), file).avatarUrl();
        Map<String, String> response = new HashMap<>();
        response.put("avatarUrl", avatarUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar(@AuthenticationPrincipal Jwt jwt) {
        profileService.deleteAvatar(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getPreferences(@AuthenticationPrincipal Jwt jwt) {
        UserDTO user = profileService.getProfile(jwt.getSubject());
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("language", user.language());
        preferences.put("theme", user.theme());
        preferences.put("baseCurrency", user.baseCurrency());
        preferences.put("monthlyBudget", user.monthlyBudget());
        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) String language,
        @RequestParam(required = false) String theme,
        @RequestParam(required = false) String baseCurrency,
        @RequestParam(required = false) Double monthlyBudget
    ) {
        profileService.updateProfile(
            jwt.getSubject(),
            null,
            baseCurrency,
            monthlyBudget,
            language,
            theme
        );
        return ResponseEntity.ok().build();
    }
}
