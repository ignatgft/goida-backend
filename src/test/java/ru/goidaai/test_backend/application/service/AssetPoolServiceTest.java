package ru.goidaai.test_backend.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.goidaai.test_backend.application.port.out.AssetPoolRepositoryPort;
import ru.goidaai.test_backend.application.port.out.PoolItemRepositoryPort;
import ru.goidaai.test_backend.domain.AssetPool;
import ru.goidaai.test_backend.domain.AssetPoolType;
import ru.goidaai.test_backend.domain.PoolItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetPoolServiceTest {

    @Mock
    private AssetPoolRepositoryPort assetPoolRepository;

    @Mock
    private PoolItemRepositoryPort poolItemRepository;

    private AssetPoolService assetPoolService;

    @BeforeEach
    void setUp() {
        assetPoolService = new AssetPoolService(assetPoolRepository, poolItemRepository);
    }

    @Test
    void createPool_shouldCreatePoolSuccessfully() {
        // Arrange
        String userId = "user-123";
        String name = "Test Pool";
        String type = "CRYPTO";
        String baseCurrency = "USD";

        AssetPool savedPool = AssetPool.builder()
            .id("pool-123")
            .userId(userId)
            .name(name)
            .type(AssetPoolType.CRYPTO)
            .baseCurrency(baseCurrency)
            .totalBalance(BigDecimal.ZERO)
            .totalValueInBaseCurrency(BigDecimal.ZERO)
            .build();

        when(assetPoolRepository.save(any(AssetPool.class))).thenReturn(savedPool);

        // Act
        AssetPool result = assetPoolService.createPool(userId, name, type, baseCurrency);

        // Assert
        assertNotNull(result);
        assertEquals("pool-123", result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(name, result.getName());
        assertEquals(AssetPoolType.CRYPTO, result.getType());
        verify(assetPoolRepository, times(1)).save(any(AssetPool.class));
        verify(poolItemRepository, times(1)).save(any(PoolItem.class));
    }

    @Test
    void getPool_shouldReturnPoolWhenExists() {
        // Arrange
        String poolId = "pool-123";
        AssetPool pool = AssetPool.builder()
            .id(poolId)
            .userId("user-123")
            .name("Test Pool")
            .type(AssetPoolType.CRYPTO)
            .baseCurrency("USD")
            .totalBalance(BigDecimal.valueOf(1000))
            .build();

        when(assetPoolRepository.findById(poolId)).thenReturn(Optional.of(pool));

        // Act
        AssetPool result = assetPoolService.getPool(poolId);

        // Assert
        assertNotNull(result);
        assertEquals(poolId, result.getId());
        assertEquals("Test Pool", result.getName());
    }

    @Test
    void getPool_shouldThrowExceptionWhenNotExists() {
        // Arrange
        String poolId = "pool-999";
        when(assetPoolRepository.findById(poolId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> assetPoolService.getPool(poolId));
    }

    @Test
    void getUserPools_shouldReturnListOfPools() {
        // Arrange
        String userId = "user-123";
        List<AssetPool> pools = List.of(
            AssetPool.builder().id("pool-1").userId(userId).name("Pool 1").type(AssetPoolType.CASH).baseCurrency("USD").totalBalance(BigDecimal.valueOf(100)).build(),
            AssetPool.builder().id("pool-2").userId(userId).name("Pool 2").type(AssetPoolType.CRYPTO).baseCurrency("USD").totalBalance(BigDecimal.valueOf(200)).build()
        );

        when(assetPoolRepository.findByUserId(userId)).thenReturn(pools);

        // Act
        List<AssetPool> result = assetPoolService.getUserPools(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(assetPoolRepository, times(1)).findByUserId(userId);
    }

    @Test
    void deletePool_shouldDeletePoolAndItems() {
        // Arrange
        String poolId = "pool-123";

        // Act
        assetPoolService.deletePool(poolId);

        // Assert
        verify(poolItemRepository, times(1)).deleteByPoolId(poolId);
        verify(assetPoolRepository, times(1)).deleteById(poolId);
    }
}
