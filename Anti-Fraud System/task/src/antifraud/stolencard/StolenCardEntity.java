package antifraud.stolencard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "stolen_card")
@Getter
@Setter
@NoArgsConstructor
public class StolenCardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String number;

    public StolenCardEntity(String number) {
        this.number = number;
    }
}
