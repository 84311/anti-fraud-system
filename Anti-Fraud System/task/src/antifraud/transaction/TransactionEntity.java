package antifraud.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "transaction")
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class TransactionEntity {

    @JsonIgnore
    TransactionResult feedback;
    @JsonIgnore
    String info;

    long amount;
    String ip;
    String number;

    @Enumerated(EnumType.STRING)
    Region region;

    LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    TransactionResult result;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @JsonProperty("transactionId")
    Long getId() {
        return id;
    }

    @JsonProperty("result")
    String getResult() {
        return result.name();
    }

    @JsonProperty("feedback")
    String getFeedback() {
        return feedback == null ? "" : feedback.name();
    }
}
