package ru.goidaai.test_backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.AvatarUploadResponse;
import ru.goidaai.test_backend.dto.UserDTO;
import ru.goidaai.test_backend.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserDTO getProfile(@AuthenticationPrincipal Jwt jwt) {
        return profileService.getProfile(jwt.getSubject());
    }

    @PostMapping("/avatar")
    public AvatarUploadResponse uploadAvatar(
        @AuthenticationPrincipal Jwt jwt,
        @RequestPart("file") MultipartFile file
    ) {
        return profileService.uploadAvatar(jwt.getSubject(), file);
    }
}
