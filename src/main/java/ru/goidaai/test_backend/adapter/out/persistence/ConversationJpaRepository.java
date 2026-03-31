package ru.goidaai.test_backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.ConversationRepositoryPort;
import ru.goidaai.test_backend.domain.Conversation;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для Conversation
 */
@Repository
@RequiredArgsConstructor
public class ConversationJpaRepository implements ConversationRepositoryPort {

    private final ConversationEntityRepository repository;

    @Override
    public Conversation save(Conversation conversation) {
        ConversationEntity entity = ConversationEntity.fromDomain(conversation);
        ConversationEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Conversation> findById(String id) {
        return repository.findById(id).map(ConversationEntity::toDomain);
    }

    @Override
    public Optional<Conversation> findByUser1IdAndUser2Id(String user1Id, String user2Id) {
        return repository.findByUser1IdAndUser2Id(user1Id, user2Id)
            .or(() -> repository.findByUser1IdAndUser2Id(user2Id, user1Id))
            .map(ConversationEntity::toDomain);
    }

    @Override
    public List<Conversation> findByUserId(String userId) {
        return repository.findAllByUser1IdOrUser2Id(userId, userId).stream()
            .map(ConversationEntity::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}

/**
 * Внутренний интерфейс для Spring Data JPA
 */
interface ConversationEntityRepository extends org.springframework.data.jpa.repository.JpaRepository<ConversationEntity, String> {
    Optional<ConversationEntity> findByUser1IdAndUser2Id(String user1Id, String user2Id);
    List<ConversationEntity> findAllByUser1IdOrUser2Id(String user1Id, String user2Id);
}
