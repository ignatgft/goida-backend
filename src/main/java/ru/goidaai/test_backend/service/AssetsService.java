package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.dto.AssetDTO;
import ru.goidaai.test_backend.dto.UpsertAssetRequest;
import ru.goidaai.test_backend.exception.ResourceNotFoundException;
import ru.goidaai.test_backend.model.Asset;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.AssetRepository;

@Service
public class AssetsService {

    private final AssetRepository assetRepository;
    private final CurrentUserService currentUserService;
    private final RatesService ratesService;
    private final DtoFactory dtoFactory;

    public AssetsService(
        AssetRepository assetRepository,
        CurrentUserService currentUserService,
        RatesService ratesService,
        DtoFactory dtoFactory
    ) {
        this.assetRepository = assetRepository;
        this.currentUserService = currentUserService;
        this.ratesService = ratesService;
        this.dtoFactory = dtoFactory;
    }

    @Transactional
    public AssetDTO create(String userId, UpsertAssetRequest request) {
        User user = currentUserService.require(userId);
        Asset asset = new Asset();
        apply(asset, request, user);
        Asset savedAsset = assetRepository.save(asset);
        return toDto(savedAsset, user.getBaseCurrency());
    }

    @Transactional
    public AssetDTO update(String userId, String assetId, UpsertAssetRequest request) {
        User user = currentUserService.require(userId);
        Asset asset = requireOwnedAsset(assetId, userId);
        apply(asset, request, user);
        Asset savedAsset = assetRepository.save(asset);
        return toDto(savedAsset, user.getBaseCurrency());
    }

    @Transactional
    public void delete(String userId, String assetId) {
        Asset asset = requireOwnedAsset(assetId, userId);
        assetRepository.delete(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> listForUser(User user) {
        return assetRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
            .map(asset -> toDto(asset, user.getBaseCurrency()))
            .toList();
    }

    @Transactional(readOnly = true)
    public Asset requireOwnedAsset(String assetId, String userId) {
        return assetRepository.findByIdAndUser_Id(assetId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    private void apply(Asset asset, UpsertAssetRequest request, User user) {
        asset.setUser(user);
        asset.setName(request.name().trim());
        asset.setType(request.type());
        asset.setSymbol(request.symbol().trim().toUpperCase());
        asset.setBalance(request.balance());
        asset.setNote(request.note());
    }

    @Transactional
    public void updateBalance(String assetId, BigDecimal amountDelta) {
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        BigDecimal newBalance = asset.getBalance().add(amountDelta);
        asset.setBalance(newBalance);
        assetRepository.save(asset);
    }

    private AssetDTO toDto(Asset asset, String baseCurrency) {
        BigDecimal currentValue = ratesService.calculateAssetCurrentValue(asset, baseCurrency);
        return dtoFactory.toAssetDto(asset, currentValue, baseCurrency);
    }
}
