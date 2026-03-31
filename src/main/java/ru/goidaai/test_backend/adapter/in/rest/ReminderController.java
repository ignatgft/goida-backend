package ru.goidaai.test_backend.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.goidaai.test_backend.application.service.ReminderService;
import ru.goidaai.test_backend.domain.Reminder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST контроллер для напоминаний
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<Reminder> createReminder(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam String title,
        @RequestParam(required = false) String description,
        @RequestParam(defaultValue = "CUSTOM") String type,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime remindAt,
        @RequestParam(defaultValue = "false") boolean isRecurring,
        @RequestParam(required = false) String recurrencePattern
    ) {
        Reminder reminder = reminderService.createReminder(
            jwt.getSubject(),
            title,
            description,
            Reminder.ReminderType.valueOf(type.toUpperCase()),
            remindAt,
            isRecurring,
            recurrencePattern
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }

    @GetMapping
    public ResponseEntity<List<Reminder>> getUserReminders(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(reminderService.getUserReminders(jwt.getSubject()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Reminder>> getUpcomingReminders(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(reminderService.getUpcomingReminders(jwt.getSubject()));
    }

    @PostMapping("/{reminderId}/complete")
    public ResponseEntity<Void> completeReminder(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String reminderId
    ) {
        reminderService.completeReminder(jwt.getSubject(), reminderId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String reminderId
    ) {
        reminderService.deleteReminder(jwt.getSubject(), reminderId);
        return ResponseEntity.noContent().build();
    }
}
