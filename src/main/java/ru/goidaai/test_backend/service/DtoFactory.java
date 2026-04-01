package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import ru.goidaai.test_backend.dto.AssetDTO;
import ru.goidaai.test_backend.dto.ChatMessageDTO;
import ru.goidaai.test_backend.dto.ReceiptDTO;
import ru.goidaai.test_backend.dto.ReceiptItemDTO;
import ru.goidaai.test_backend.dto.TransactionDTO;
import ru.goidaai.test_backend.dto.UserDTO;
import ru.goidaai.test_backend.model.Asset;
import ru.goidaai.test_backend.model.ChatHistory;
import ru.goidaai.test_backend.model.Receipt;
import ru.goidaai.test_backend.model.ReceiptItem;
import ru.goidaai.test_backend.model.Transaction;
import ru.goidaai.test_backend.model.User;

/**
 * Фабрика для преобразования моделей в DTO
 */
@Component
public class DtoFactory {

    public UserDTO toUserDto(User user) {
        return new UserDTO(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getUsername(),
            user.getAvatarUrl(),
            user.getBaseCurrency(),
            user.getMonthlyBudget(),
            user.getAuthProvider(),
            user.isEmailVerified(),
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLanguage(),
            user.getTheme(),
            user.getEmailNotifications(),
            user.getPushNotifications(),
            user.getTimezone()
        );
    }

    public AssetDTO toAssetDto(Asset asset, BigDecimal currentValue, String baseCurrency) {
        String symbol = asset.getSymbol();
        BigDecimal balance = asset.getBalance();
        
        // currency и symbol - одно и то же
        // amount и balance - одно и то же
        // Оставлены для обратной совместимости
        return new AssetDTO(
            asset.getId(),
            asset.getName(),
            asset.getType().clientValue(),
            symbol,           // currency
            balance,          // amount
            symbol,           // symbol
            balance,          // balance
            currentValue,     // currentValue в базовой валюте
            baseCurrency,
            asset.getNote(),
            asset.getCreatedAt(),
            asset.getUpdatedAt()
        );
    }

    public ReceiptItemDTO toReceiptItemDto(ReceiptItem receiptItem) {
        return new ReceiptItemDTO(
            receiptItem.getName(),
            receiptItem.getName(),
            receiptItem.getQuantity(),
            receiptItem.getUnitPrice(),
            receiptItem.getUnitPrice(),
            receiptItem.getTotalPrice()
        );
    }

    public ReceiptDTO toReceiptDto(Receipt receipt) {
        List<ReceiptItemDTO> items = receipt.getItems().stream()
            .filter(Objects::nonNull)
            .map(this::toReceiptItemDto)
            .toList();

        return new ReceiptDTO(
            receipt.getId(),
            receipt.getId(),
            receipt.getMerchant(),
            receipt.getTotal(),
            receipt.getCurrency(),
            receipt.getPurchasedAt(),
            items,
            receipt.getImageUrl(),
            receipt.getCreatedAt()
        );
    }

    public TransactionDTO toTransactionDto(Transaction transaction, ReceiptDTO receiptDTO) {
        Instant effectiveOccurredAt = transaction.getOccurredAt() != null
            ? transaction.getOccurredAt()
            : transaction.getCreatedAt();

        return new TransactionDTO(
            transaction.getId(),
            transaction.getTitle(),
            transaction.getCategory(),
            transaction.getKind().clientValue(),
            transaction.getKind(),
            transaction.getSourceType(),
            transaction.getAmount(),
            transaction.getCurrency(),
            effectiveOccurredAt,
            effectiveOccurredAt,
            transaction.getSourceAsset() != null ? transaction.getSourceAsset().getId() : null,
            transaction.getSourceAssetName(),
            transaction.getNote(),
            receiptDTO
        );
    }

    public ChatMessageDTO toChatMessageDto(ChatHistory chatHistory) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setContent(chatHistory.getMessage());
        dto.setType("TEXT");
        dto.setSenderId(chatHistory.getUser().getId());
        dto.setSenderName(chatHistory.getUser().getFullName());
        dto.setReceiverId(chatHistory.getUser().getId());
        dto.setReceiverName(chatHistory.getUser().getFullName());
        dto.setIsRead(true);
        dto.setSentAt(chatHistory.getCreatedAt());
        return dto;
    }
}
