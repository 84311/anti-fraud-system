package antifraud.stolencard;

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
    StolenCardRepository stolenCardRepository;

    @PostMapping
    public StolenCardEntity saveCard(@Valid @RequestBody Map<String, String> cardJSON) {
        String number = cardJSON.get("number");

        if (isCardNumberNonValid(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (stolenCardRepository.findByNumber(number).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        StolenCardEntity stolenCardEntity = new StolenCardEntity(number);
        stolenCardRepository.save(stolenCardEntity);

        return stolenCardEntity;
    }

    @DeleteMapping("/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable String number) {
        if (isCardNumberNonValid(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        StolenCardEntity stolenCardEntity = stolenCardRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        stolenCardRepository.delete(stolenCardEntity);

        return Map.of(
                "status", "Card " + number + " successfully removed!"
        );
    }

    @GetMapping
    public List<StolenCardEntity> getStolenCardsList() {
        return stolenCardRepository.findAll();
    }

    private boolean isCardNumberNonValid(String number) {
        int nDigits = number.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--) {
            int d = number.charAt(i) - '0';

            if (isSecond) {
                d = d * 2;
            }

            nSum += d / 10;
            nSum += d % 10;
            isSecond = !isSecond;
        }
        return (nSum % 10 != 0);
    }
}
