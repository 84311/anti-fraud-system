package antifraud.stolencardsandsuspiciousips;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuspiciousIPRepository extends JpaRepository<IPEntity, Long> {
    Optional<IPEntity> findByIp(String ip);
}
