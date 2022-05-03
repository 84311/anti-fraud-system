package antifraud.suspiciousip;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity(name = "suspicious_ip")
@Table(name = "suspicious_ip")
@Getter
@Setter
@NoArgsConstructor
public class SuspiciousIPEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty
    private String ip;

    SuspiciousIPEntity(String ip) {
        this.ip = ip;
    }
}
