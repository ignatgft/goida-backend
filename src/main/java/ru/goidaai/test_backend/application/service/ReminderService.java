package ru.goidaai.test_backend.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.application.port.out.ReminderRepositoryPort;
import ru.goidaai.test_backend.domain.Reminder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис напоминаний
 */
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepositoryPort reminderRepository;
    private final NotificationService notificationService;

    @Transactional
    public Reminder createReminder(String userId, String title, String description,
                                    Reminder.ReminderType type, LocalDateTime remindAt,
                                    boolean isRecurring, String recurrencePattern) {
        Reminder reminder = Reminder.builder()
            .userId(userId)
            .title(title)
            .description(description)
            .type(type)
            .remindAt(remindAt)
            .isCompleted(false)
            .isRecurring(isRecurring)
            .recurrencePattern(recurrencePattern)
            .build();
        return reminderRepository.save(reminder);
    }

    @Transactional(readOnly = true)
    public List<Reminder> getUserReminders(String userId) {
        return reminderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Reminder> getUpcomingReminders(String userId) {
        return reminderRepository.findUpcomingReminders(userId);
    }

    @Transactional
    public void completeReminder(String userId, String reminderId) {
        Reminder reminder = reminderRepository.findById(userId, reminderId);
        if (reminder != null) {
            reminder.setIsCompleted(true);
            reminderRepository.save(reminder);
        }
    }

    @Transactional
    public void deleteReminder(String userId, String reminderId) {
        reminderRepository.deleteById(userId, reminderId);
    }

    /**
     * Проверка напоминаний каждые 5 минут
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkReminders() {
        // Получаем все активные напоминания
        // Для каждого пользователя проверяем наступило ли время
        // Отправляем уведомление если время пришло
        
        // Эта логика будет реализована когда будет полноценная БД
        // Сейчас это заглушка для демонстрации
    }
}
