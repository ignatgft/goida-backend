package ru.goidaai.test_backend.service.transaction;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * Сервис для кодирования/декодирования курсоров пагинации
 */
@Component
public class CursorEncoder {

    private static final String SEPARATOR = "|";

    /**
     * Закодировать курсор
     */
    public String encode(String id, String occurredAt) {
        String raw = id + SEPARATOR + occurredAt;
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Декодировать курсор
     * @return массив [id, occurredAt] или null если не удалось декодировать
     */
    public String[] decode(String cursor) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8));
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = decoded.split(SEPARATOR, 2);
            return parts.length == 2 ? parts : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Проверить валидность курсора
     */
    public boolean isValid(String cursor) {
        return decode(cursor) != null;
    }
}
