package antifraud.suspiciousip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuspiciousIPRepository extends JpaRepository<SuspiciousIPEntity, Long> {
    Optional<SuspiciousIPEntity> findByIp(String ip);
}
