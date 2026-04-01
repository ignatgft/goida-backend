package ru.goidaai.test_backend.adapter.in.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.AssetBalanceSummaryDTO;
import ru.goidaai.test_backend.dto.AssetDTO;
import ru.goidaai.test_backend.dto.UpsertAssetRequest;
import ru.goidaai.test_backend.service.AssetsService;

@RestController
@RequestMapping("/api/assets")
public class AssetsController {

    private final AssetsService assetsService;

    public AssetsController(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    @PostMapping
    public ResponseEntity<AssetDTO> create(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpsertAssetRequest request
    ) {
        AssetDTO asset = assetsService.create(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(asset);
    }

    @PutMapping("/{assetId}")
    public AssetDTO update(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String assetId,
        @Valid @RequestBody UpsertAssetRequest request
    ) {
        return assetsService.update(jwt.getSubject(), assetId, request);
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable String assetId) {
        assetsService.delete(jwt.getSubject(), assetId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить сводку по активам: общий баланс и потраченный баланс
     */
    @GetMapping("/balance-summary")
    public AssetBalanceSummaryDTO balanceSummary(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "month") String period
    ) {
        return assetsService.getBalanceSummary(jwt.getSubject(), period);
    }
}
