package ru.goidaai.test_backend.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.ReceiptDTO;
import ru.goidaai.test_backend.dto.ReceiptItemDTO;
import ru.goidaai.test_backend.dto.ReceiptMetadataRequest;
import ru.goidaai.test_backend.model.Receipt;
import ru.goidaai.test_backend.model.ReceiptItem;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.ReceiptRepository;

@Service
public class ReceiptService {

    private final CurrentUserService currentUserService;
    private final StorageService storageService;
    private final ReceiptOcrProvider receiptOcrProvider;
    private final ReceiptRepository receiptRepository;
    private final DtoFactory dtoFactory;
    private final Clock clock;

    public ReceiptService(
        CurrentUserService currentUserService,
        StorageService storageService,
        ReceiptOcrProvider receiptOcrProvider,
        ReceiptRepository receiptRepository,
        DtoFactory dtoFactory,
        Clock clock
    ) {
        this.currentUserService = currentUserService;
        this.storageService = storageService;
        this.receiptOcrProvider = receiptOcrProvider;
        this.receiptRepository = receiptRepository;
        this.dtoFactory = dtoFactory;
        this.clock = clock;
    }

    @Transactional
    public ReceiptDTO process(String userId, MultipartFile file) {
        User user = currentUserService.require(userId);
        String imageUrl = storageService.storeImage(file, "receipts");
        ReceiptOcrProvider.ReceiptExtractionResult extractionResult = receiptOcrProvider.extract(file);

        Receipt receipt = new Receipt();
        receipt.setUser(user);
        receipt.setMerchant(extractionResult.merchant());
        receipt.setTotal(extractionResult.total());
        receipt.setCurrency(extractionResult.currency());
        receipt.setPurchasedAt(extractionResult.purchasedAt() != null ? extractionResult.purchasedAt() : clock.instant());
        receipt.setImageUrl(imageUrl);
        receipt.setProviderName(extractionResult.providerName());
        receipt.setItems(buildReceiptItems(receipt, extractionResult.items()));

        return dtoFactory.toReceiptDto(receiptRepository.save(receipt));
    }

    /**
     * Создать чек из метаданных (для уже отсканированных чеков)
     */
    @Transactional
    public Receipt createFromScan(String userId, ReceiptMetadataRequest request) {
        User user = currentUserService.require(userId);

        Receipt receipt = new Receipt();
        receipt.setUser(user);
        receipt.setMerchant(request.merchant());
        receipt.setTotal(request.total());
        receipt.setCurrency(request.currency());
        receipt.setPurchasedAt(request.purchasedAt() != null ? request.purchasedAt() : clock.instant());
        receipt.setProviderName("manual");
        receipt.setItems(buildReceiptItemsFromRequest(receipt, request.items()));

        return receiptRepository.save(receipt);
    }

    private List<ReceiptItem> buildReceiptItemsFromRequest(Receipt receipt, List<ru.goidaai.test_backend.dto.ReceiptItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
            .map(item -> {
                ReceiptItem receiptItem = new ReceiptItem();
                receiptItem.setReceipt(receipt);
                receiptItem.setName(item.name());
                receiptItem.setQuantity(item.quantity());
                receiptItem.setUnitPrice(item.unitPrice());
                receiptItem.setTotalPrice(item.totalPrice());
                return receiptItem;
            })
            .toList();
    }

    private List<ReceiptItem> buildReceiptItems(Receipt receipt, List<ReceiptItemDTO> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
            .filter(Objects::nonNull)
            .map(item -> {
                ReceiptItem receiptItem = new ReceiptItem();
                receiptItem.setReceipt(receipt);
                receiptItem.setName(item.name());
                receiptItem.setQuantity(item.quantity());
                receiptItem.setUnitPrice(item.unitPrice());
                receiptItem.setTotalPrice(item.totalPrice());
                return receiptItem;
            })
            .toList();
    }
}
