package ru.goidaai.test_backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.analytics.DashboardOverviewDTO;
import ru.goidaai.test_backend.service.DashboardService;

/**
 * Контроллер для дашборда с расширенной аналитикой
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Получить расширенную сводку дашборда с аналитикой
     */
    @GetMapping("/overview")
    public DashboardOverviewDTO overview(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "month") String period
    ) {
        return dashboardService.getOverview(jwt.getSubject(), period);
    }
}
