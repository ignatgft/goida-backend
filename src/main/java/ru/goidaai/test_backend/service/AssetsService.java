package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.dto.AssetBalanceSummaryDTO;
import ru.goidaai.test_backend.dto.AssetDTO;
import ru.goidaai.test_backend.dto.UpsertAssetRequest;
import ru.goidaai.test_backend.exception.ResourceNotFoundException;
import ru.goidaai.test_backend.model.Asset;
import ru.goidaai.test_backend.model.Transaction;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.enums.PeriodFilter;
import ru.goidaai.test_backend.model.enums.TransactionKind;
import ru.goidaai.test_backend.repository.AssetRepository;
import ru.goidaai.test_backend.repository.TransactionRepository;

@Service
public class AssetsService {

    private final AssetRepository assetRepository;
    private final CurrentUserService currentUserService;
    private final RatesService ratesService;
    private final DtoFactory dtoFactory;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public AssetsService(
        AssetRepository assetRepository,
        CurrentUserService currentUserService,
        RatesService ratesService,
        DtoFactory dtoFactory,
        TransactionRepository transactionRepository,
        Clock clock
    ) {
        this.assetRepository = assetRepository;
        this.currentUserService = currentUserService;
        this.ratesService = ratesService;
        this.dtoFactory = dtoFactory;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
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
    @CacheEvict(value = {"assets", "dashboard"}, key = "#userId")
    public void delete(String userId, String assetId) {
        Asset asset = requireOwnedAsset(assetId, userId);
        assetRepository.delete(asset);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "assets", key = "#user.id", unless = "#result == null || #result.isEmpty()")
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

    /**
     * Получить сводку по активам: общий баланс и потраченный баланс
     */
    @Transactional(readOnly = true)
    public AssetBalanceSummaryDTO getBalanceSummary(String userId, String rawPeriod) {
        User user = currentUserService.require(userId);
        PeriodFilter period = PeriodFilter.from(rawPeriod);

        // Общий баланс всех активов в базовой валюте
        List<AssetDTO> assets = listForUser(user);
        BigDecimal totalAssets = assets.stream()
            .map(AssetDTO::currentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        // Расходы за период
        List<Transaction> expenses = transactionRepository.findAll(expensesSpec(userId, period));
        BigDecimal spentBalance = expenses.stream()
            .map(t -> ratesService.convertAmount(t.getAmount(), t.getCurrency(), user.getBaseCurrency()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        return new AssetBalanceSummaryDTO(
            totalAssets,
            spentBalance,
            user.getBaseCurrency(),
            period.label()
        );
    }

    /**
     * Спецификация для фильтрации расходов
     */
    private Specification<Transaction> expensesSpec(String userId, PeriodFilter period) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.equal(root.get("user").get("id"), userId),
            criteriaBuilder.equal(root.get("kind"), TransactionKind.EXPENSE),
            criteriaBuilder.greaterThanOrEqualTo(root.get("occurredAt"), period.startsAt(clock))
        );
    }
}
