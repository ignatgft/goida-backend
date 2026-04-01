package ru.goidaai.test_backend.adapter.in.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.AvatarInfoDTO;
import ru.goidaai.test_backend.dto.AvatarUploadResponse;
import ru.goidaai.test_backend.dto.ResizeAvatarRequest;
import ru.goidaai.test_backend.service.AvatarService;

@RestController
@RequestMapping("/api/avatars")
public class AvatarController {

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        AvatarUploadResponse response = avatarService.uploadAvatar(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resize")
    public ResponseEntity<AvatarUploadResponse> resizeAvatar(
            @PathVariable String id,
            @RequestBody ResizeAvatarRequest request) {
        AvatarUploadResponse response = avatarService.resizeAvatar(
                id,
                request.getWidth(),
                request.getHeight(),
                request.getMaintainAspectRatio() != null ? request.getMaintainAspectRatio() : true
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AvatarInfoDTO>> getUserAvatars(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<AvatarInfoDTO> avatars = avatarService.getUserAvatars(userId);
        return ResponseEntity.ok(avatars);
    }

    @GetMapping("/active")
    public ResponseEntity<AvatarInfoDTO> getActiveAvatar(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return avatarService.getActiveAvatar(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> setActiveAvatar(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        avatarService.setActiveAvatar(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvatar(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        avatarService.deleteAvatar(id);
        return ResponseEntity.ok().build();
    }
}
