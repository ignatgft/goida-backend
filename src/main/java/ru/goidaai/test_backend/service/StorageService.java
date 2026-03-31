package ru.goidaai.test_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.exception.BadRequestException;

@Service
public class StorageService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path uploadRoot;
    private final String publicBaseUrl;

    public StorageService(AppProperties appProperties) {
        this.uploadRoot = Path.of(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
        this.publicBaseUrl = appProperties.getStorage().getPublicBaseUrl();
    }

    public String storeImage(MultipartFile multipartFile, String folderName) {
        validateImage(multipartFile);
        String originalFilename = StringUtils.hasText(multipartFile.getOriginalFilename())
            ? multipartFile.getOriginalFilename()
            : "upload";
        String extension = extensionOf(originalFilename);
        String filename = UUID.randomUUID() + "." + extension;

        try {
            Path folder = uploadRoot.resolve(folderName).normalize();
            Files.createDirectories(folder);
            Files.copy(multipartFile.getInputStream(), folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store uploaded file", exception);
        }

        return buildPublicUrl(folderName, filename);
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        try {
            // Извлекаем имя файла из URL
            String filename = filePath.substring(filePath.lastIndexOf('/') + 1);
            Path file = uploadRoot.resolve("avatars").resolve(filename).normalize();
            if (Files.exists(file)) {
                Files.delete(file);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to delete file", exception);
        }
    }

    private void validateImage(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        String contentType = multipartFile.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("Only image uploads are supported");
        }
        String extension = extensionOf(multipartFile.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Supported image formats: jpg, jpeg, png, webp");
        }
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String buildPublicUrl(String folderName, String filename) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl.replaceAll("/+$", "") + "/uploads/" + folderName + "/" + filename;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/uploads/")
            .path(folderName)
            .path("/")
            .path(filename)
            .toUriString();
    }
}
