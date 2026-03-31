package ru.goidaai.test_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис уведомлений через Kafka
 * Отправляет уведомления в топик Kafka
 * По умолчанию отключен (требуется явное включение через application.properties)
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    public NotificationService(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Отправить уведомление о создании транзакции
     */
    public void sendTransactionCreated(String userId, String transactionId, Map<String, Object> data) {
        if (!kafkaEnabled) {
            log.debug("Kafka отключен, уведомление о создании транзакции не отправлено");
            return;
        }
        sendNotification("transaction.created", userId, transactionId, data);
    }

    /**
     * Отправить уведомление об обновлении транзакции
     */
    public void sendTransactionUpdated(String userId, String transactionId, Map<String, Object> data) {
        if (!kafkaEnabled) {
            log.debug("Kafka отключен, уведомление об обновлении транзакции не отправлено");
            return;
        }
        sendNotification("transaction.updated", userId, transactionId, data);
    }

    /**
     * Отправить уведомление об обновлении актива
     */
    public void sendAssetUpdated(String userId, String assetId, Map<String, Object> data) {
        if (!kafkaEnabled) {
            log.debug("Kafka отключен, уведомление об обновлении актива не отправлено");
            return;
        }
        sendNotification("asset.updated", userId, assetId, data);
    }

    /**
     * Отправить пользовательское уведомление
     */
    public void sendUserNotification(String userId, String type, String title, String message) {
        if (!kafkaEnabled) {
            log.debug("Kafka отключен, пользовательское уведомление не отправлено");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("title", title);
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());

        sendNotification("user.notification", userId, userId, data);
    }

    private void sendNotification(String topic, String userId, String key, Map<String, Object> data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Уведомление отправлено в топик {}: offset={}, partition={}",
                        topic,
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
                } else {
                    log.error("Ошибка отправки уведомления в топик {}: {}", topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления: {}", e.getMessage(), e);
        }
    }
}
