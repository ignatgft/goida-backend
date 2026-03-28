package ru.goidaai.test_backend.service.transaction;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.goidaai.test_backend.dto.ReceiptItemRequest;
import ru.goidaai.test_backend.model.Receipt;
import ru.goidaai.test_backend.model.ReceiptItem;

/**
 * Сервис для связывания транзакций с чеками
 */
@Component
public class ReceiptLinkingService {

    /**
     * Связать транзакцию с чеком
     */
    public void linkReceipt(Receipt receipt, String transactionId) {
        if (receipt != null && transactionId != null) {
            // receipt.setTransactionId не существует, используем setTransaction
            // Но для этого нужен Transaction объект, поэтому оставляем как есть
            // В TransactionsService нужно будет передать Transaction объект
        }
    }

    /**
     * Построить элементы чека из запроса
     */
    public List<ReceiptItem> buildReceiptItems(Receipt receipt, List<ReceiptItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return List.of();
        }

        List<ReceiptItem> items = new ArrayList<>();
        for (ReceiptItemRequest itemRequest : itemRequests) {
            ReceiptItem item = new ReceiptItem();
            item.setReceipt(receipt);
            item.setName(itemRequest.name());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            item.setTotalPrice(itemRequest.totalPrice());
            items.add(item);
        }
        return items;
    }

    /**
     * Обновить элементы чека
     */
    public void updateReceiptItems(Receipt receipt, List<ReceiptItem> currentItems, List<ReceiptItemRequest> newItemRequests) {
        // Удаляем старые элементы
        currentItems.forEach(item -> item.setReceipt(null));
        currentItems.clear();

        // Добавляем новые
        if (newItemRequests != null) {
            currentItems.addAll(buildReceiptItems(receipt, newItemRequests));
        }
    }
}
