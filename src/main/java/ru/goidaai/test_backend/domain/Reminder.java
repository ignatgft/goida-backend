package ru.goidaai.test_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Доменная модель - Напоминание
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
    private String id;
    private String userId;
    private String title;
    private String description;
    private ReminderType type;
    private LocalDateTime remindAt;
    private Boolean isCompleted;
    private Boolean isRecurring;
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY
    private Instant createdAt;
    private Instant updatedAt;
    
    public enum ReminderType {
        TRANSACTION,
        BILL,
        CUSTOM,
        SYSTEM
    }
}
