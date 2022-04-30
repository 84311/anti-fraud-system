package antifraud.stolencardsandsuspiciousips;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StolenCardRepository extends JpaRepository<CardEntity, Long> {
    Optional<CardEntity> findByNumber(String ip);
}
