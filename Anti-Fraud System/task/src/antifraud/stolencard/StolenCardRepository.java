package antifraud.stolencard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StolenCardRepository extends JpaRepository<StolenCardEntity, Long> {
    Optional<StolenCardEntity> findByNumber(String ip);
}
