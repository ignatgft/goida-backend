package ru.goidaai.test_backend.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.goidaai.test_backend.model.Transaction;

public interface TransactionRepository
    extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUser_Id(String id, String userId);

    List<Transaction> findByUser_IdOrderByOccurredAtDesc(String userId, Pageable pageable);
}
