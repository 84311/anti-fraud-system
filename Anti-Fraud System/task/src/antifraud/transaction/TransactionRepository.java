package antifraud.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findAllByDateBetweenAndNumber(LocalDateTime startDate, LocalDateTime endDate, String number);
}
