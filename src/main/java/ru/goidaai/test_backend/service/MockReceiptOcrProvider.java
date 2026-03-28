package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.ReceiptItemDTO;

@Component
public class MockReceiptOcrProvider implements ReceiptOcrProvider {

    private final Clock clock;

    public MockReceiptOcrProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public ReceiptExtractionResult extract(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String merchantName = StringUtils.hasText(originalFilename)
            ? originalFilename.replaceFirst("\\.[^.]+$", "").replace('-', ' ').replace('_', ' ')
            : "Unknown merchant";

        return new ReceiptExtractionResult(
            merchantName,
            BigDecimal.ZERO,
            "USD",
            clock.instant(),
            List.of(
                new ReceiptItemDTO(
                    "Unrecognized item",
                    "Unrecognized item",
                    BigDecimal.ONE,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
                )
            ),
            "mock-ocr"
        );
    }
}
