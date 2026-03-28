package ru.goidaai.test_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.CryptoRatesResponse;
import ru.goidaai.test_backend.dto.FiatRatesResponse;
import ru.goidaai.test_backend.service.RatesService;

/**
 * Контроллер для получения курсов валют
 * Фиат: ExchangeRate-API
 * Крипта: CoinGecko
 */
@RestController
@RequestMapping("/api/rates")
public class RatesController {

    private final RatesService ratesService;

    public RatesController(RatesService ratesService) {
        this.ratesService = ratesService;
    }

    @GetMapping("/fiat")
    public FiatRatesResponse fiat(
        @RequestParam(defaultValue = "USD") String base
    ) {
        return ratesService.getFiatRates(base);
    }

    @GetMapping("/crypto")
    public CryptoRatesResponse crypto(
        @RequestParam(defaultValue = "USD") String fiat
    ) {
        return ratesService.getCryptoRates(fiat);
    }
}
