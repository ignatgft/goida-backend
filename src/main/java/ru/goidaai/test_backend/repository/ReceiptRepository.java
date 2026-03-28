package ru.goidaai.test_backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.goidaai.test_backend.model.Receipt;

public interface ReceiptRepository extends JpaRepository<Receipt, String> {

    Optional<Receipt> findByIdAndUser_Id(String receiptId, String userId);

    Optional<Receipt> findByTransaction_Id(String transactionId);

    List<Receipt> findByTransaction_IdIn(List<String> transactionIds);
}
