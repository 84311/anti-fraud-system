package antifraud.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long> {
    Optional<CardEntity> findByNumber(String number);

    List<CardEntity> findAllByLockedTrue();

    boolean existsByNumberAndLockedTrue(String number);
}
