package ru.goidaai.test_backend.adapter.in.rest;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.UpdateSettingsRequest;
import ru.goidaai.test_backend.dto.UserDTO;
import ru.goidaai.test_backend.dto.UserSettingsDTO;
import ru.goidaai.test_backend.service.ProfileService;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ProfileService profileService;

    public SettingsController(ProfileService profileService) {
        this.profileService = profileService;
    }

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
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) Boolean emailNotifications,
            @RequestParam(required = false) Boolean pushNotifications,
            @RequestParam(required = false) String timezone) {
        
        UserDTO updated = profileService.updateProfile(
                jwt.getSubject(),
                name,
                baseCurrency,
                monthlyBudget,
                language,
                theme,
                emailNotifications,
                pushNotifications,
                timezone
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam MultipartFile file) {
        
        AvatarUploadResponse response = profileService.uploadAvatar(jwt.getSubject(), file);
        Map<String, String> result = new HashMap<>();
        result.put("avatarUrl", response.getUrl());
        return ResponseEntity.ok(result);
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
        preferences.put("emailNotifications", user.emailNotifications());
        preferences.put("pushNotifications", user.pushNotifications());
        preferences.put("timezone", user.timezone());
        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String baseCurrency,
            @RequestParam(required = false) Double monthlyBudget,
            @RequestParam(required = false) Boolean emailNotifications,
            @RequestParam(required = false) Boolean pushNotifications,
            @RequestParam(required = false) String timezone) {
        
        profileService.updateProfile(
                jwt.getSubject(),
                null,
                baseCurrency,
                monthlyBudget,
                language,
                theme,
                emailNotifications,
                pushNotifications,
                timezone
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/all")
    public ResponseEntity<UserDTO> updateAllSettings(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateSettingsRequest request) {
        
        UserDTO updated = profileService.updateProfile(
                jwt.getSubject(),
                request.getFullName(),
                request.getBaseCurrency(),
                request.getMonthlyBudget(),
                request.getLanguage(),
                request.getTheme(),
                request.getEmailNotifications(),
                request.getPushNotifications(),
                request.getTimezone()
        );
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/full")
    public ResponseEntity<UserSettingsDTO> getFullSettings(@AuthenticationPrincipal Jwt jwt) {
        UserDTO user = profileService.getProfile(jwt.getSubject());
        UserSettingsDTO settings = new UserSettingsDTO();
        settings.setId(user.id());
        settings.setEmail(user.email());
        settings.setFullName(user.fullName());
        settings.setAvatarUrl(user.avatarUrl());
        settings.setBaseCurrency(user.baseCurrency());
        settings.setMonthlyBudget(user.monthlyBudget().doubleValue());
        settings.setLanguage(user.language());
        settings.setTheme(user.theme());
        settings.setEmailNotifications(user.emailNotifications());
        settings.setPushNotifications(user.pushNotifications());
        settings.setTimezone(user.timezone());
        return ResponseEntity.ok(settings);
    }
}
