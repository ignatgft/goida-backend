package ru.goidaai.test_backend.adapter.in.rest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.ReceiptDTO;
import ru.goidaai.test_backend.service.ReceiptService;

@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping("/process")
    public ReceiptDTO process(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) {
        return receiptService.process(jwt.getSubject(), file);
    }
}
