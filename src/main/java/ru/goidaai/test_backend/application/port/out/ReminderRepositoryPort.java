package ru.goidaai.test_backend.application.port.out;

import ru.goidaai.test_backend.domain.Reminder;
import java.util.List;

/**
 * Порт репозитория для напоминаний
 */
public interface ReminderRepositoryPort {
    Reminder save(Reminder reminder);
    List<Reminder> findByUserId(String userId);
    List<Reminder> findUpcomingReminders(String userId);
    void deleteById(String userId, String reminderId);
    Reminder findById(String userId, String reminderId);
}
