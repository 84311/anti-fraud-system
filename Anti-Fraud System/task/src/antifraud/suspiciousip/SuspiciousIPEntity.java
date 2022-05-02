package antifraud.suspiciousip;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
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

    public SuspiciousIPEntity(String ip) {
        this.ip = ip;
    }
}
