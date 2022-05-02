package antifraud.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class TransactionEntity {
    long amount;
    String ip;
    String number;
    @Enumerated(EnumType.STRING)
    Region region;
    LocalDateTime date;
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    TransactionResult result;
    @JsonIgnore
    @Transient
    String info;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;
}
