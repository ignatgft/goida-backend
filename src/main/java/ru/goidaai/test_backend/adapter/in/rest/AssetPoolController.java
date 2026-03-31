package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.goidaai.test_backend.application.service.AssetPoolService;
import ru.goidaai.test_backend.domain.AssetPool;
import ru.goidaai.test_backend.domain.PoolItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST контроллер для управления пулами активов
 */
@RestController
@RequestMapping("/api/asset-pools")
@RequiredArgsConstructor
public class AssetPoolController {

    private final AssetPoolService assetPoolService;

    @PostMapping
    public ResponseEntity<AssetPool> createPool(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam String name,
        @RequestParam String type,
        @RequestParam(defaultValue = "USD") String baseCurrency
    ) {
        AssetPool pool = assetPoolService.createPool(jwt.getSubject(), name, type, baseCurrency);
        return ResponseEntity.status(HttpStatus.CREATED).body(pool);
    }

    @GetMapping("/{poolId}")
    public ResponseEntity<AssetPool> getPool(@PathVariable String poolId) {
        return ResponseEntity.ok(assetPoolService.getPool(poolId));
    }

    @GetMapping
    public ResponseEntity<List<AssetPool>> getUserPools(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(assetPoolService.getUserPools(jwt.getSubject()));
    }

    @PostMapping("/{poolId}/currencies")
    public ResponseEntity<PoolItem> addCurrencyToPool(
        @PathVariable String poolId,
        @RequestParam String currency,
        @RequestParam BigDecimal balance
    ) {
        PoolItem item = assetPoolService.addCurrencyToPool(poolId, currency, balance);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{poolId}/currencies/{currency}")
    public ResponseEntity<Void> updateCurrencyBalance(
        @PathVariable String poolId,
        @PathVariable String currency,
        @RequestParam BigDecimal balance
    ) {
        assetPoolService.updateCurrencyBalance(poolId, currency, balance);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{poolId}")
    public ResponseEntity<Void> deletePool(@PathVariable String poolId) {
        assetPoolService.deletePool(poolId);
        return ResponseEntity.noContent().build();
    }
}
