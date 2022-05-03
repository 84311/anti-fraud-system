package antifraud.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "card")
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String number;

    @JsonIgnore
    private boolean locked;

    @JsonIgnore
    private int allowedLimit = 200;

    @JsonIgnore
    private int manualLimit = 1500;

    public CardEntity(String number, boolean locked) {
        this.number = number;
        this.locked = locked;
    }
}
