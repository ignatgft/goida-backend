package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.goidaai.test_backend.application.service.WalletConnectService;
import ru.goidaai.test_backend.domain.WalletConnect;

import java.util.List;

/**
 * REST контроллер для управления WalletConnect
 */
@RestController
@RequestMapping("/api/wallet-connect")
@RequiredArgsConstructor
public class WalletConnectController {

    private final WalletConnectService walletConnectService;

    @PostMapping("/connect")
    public ResponseEntity<WalletConnect> connectWallet(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam String walletAddress,
        @RequestParam(defaultValue = "METAMASK") String walletType
    ) {
        WalletConnect wallet = walletConnectService.connectWallet(
            jwt.getSubject(), 
            walletAddress, 
            walletType
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping
    public ResponseEntity<List<WalletConnect>> getUserWallets(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(walletConnectService.getUserWallets(jwt.getSubject()));
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> disconnectWallet(@PathVariable String walletId) {
        walletConnectService.disconnectWallet(walletId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{walletId}/sync")
    public ResponseEntity<Void> syncWallet(@PathVariable String walletId) {
        walletConnectService.syncWalletBalances(walletId);
        return ResponseEntity.ok().build();
    }
}
