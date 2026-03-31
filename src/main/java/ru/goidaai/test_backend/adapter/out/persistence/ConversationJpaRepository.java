package ru.goidaai.test_backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.application.port.out.ConversationRepositoryPort;
import ru.goidaai.test_backend.domain.Conversation;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер репозитория для Conversation
 */
@Repository
public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, String>, ConversationRepositoryPort {

    Optional<ConversationEntity> findByUser1IdAndUser2Id(String user1Id, String user2Id);

    @Override
    default Conversation save(Conversation conversation) {
        ConversationEntity entity = ConversationEntity.fromDomain(conversation);
        ConversationEntity saved = super.save(entity);
        return saved.toDomain();
    }

    @Override
    default Optional<Conversation> findById(String id) {
        return super.findById(id).map(ConversationEntity::toDomain);
    }

    @Override
    default Optional<Conversation> findByUser1IdAndUser2Id(String user1Id, String user2Id) {
        return findByUser1IdAndUser2Id(user1Id, user2Id).map(ConversationEntity::toDomain);
    }

    @Override
    default List<Conversation> findByUserId(String userId) {
        return findAllByUser1IdOrUser2Id(userId, userId).stream()
            .map(ConversationEntity::toDomain)
            .toList();
    }

    @Override
    default void deleteById(String id) {
        super.deleteById(id);
    }

    List<ConversationEntity> findAllByUser1IdOrUser2Id(String user1Id, String user2Id);
}
