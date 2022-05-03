package antifraud.card;

import antifraud.transaction.TransactionValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud/stolencard")
public class StolenCardController {

    @Resource
    private CardRepository cardRepository;

    @PostMapping
    public CardEntity saveCardAsStolenOrMarkExistingAsStolen(@Valid @RequestBody Map<String, String> cardJSON) {
        String number = cardJSON.get("number");

        if (TransactionValidator.isCardNumberNonValid(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        CardEntity card;

        if (cardRepository.findByNumber(number).isPresent()) {
            if (cardRepository.existsByNumberAndLockedTrue(number)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            } else {
                card = cardRepository.findByNumber(number).get();
                card.setLocked(true);
            }
        } else {
            card = new CardEntity(number, true);
        }

        cardRepository.save(card);
        return card;
    }

    @DeleteMapping("/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable String number) {
        if (TransactionValidator.isCardNumberNonValid(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        CardEntity cardEntity = cardRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (cardEntity.isLocked()) cardRepository.delete(cardEntity);

        return Map.of(
                "status", "Card " + number + " successfully removed!"
        );
    }

    @GetMapping
    public List<CardEntity> getStolenCardsList() {
        return cardRepository.findAllByLockedTrue();
    }
}
