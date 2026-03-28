package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.ReceiptItemDTO;

public interface ReceiptOcrProvider {

    ReceiptExtractionResult extract(MultipartFile multipartFile);

    record ReceiptExtractionResult(
        String merchant,
        BigDecimal total,
        String currency,
        Instant purchasedAt,
        List<ReceiptItemDTO> items,
        String providerName
    ) {
    }
}
