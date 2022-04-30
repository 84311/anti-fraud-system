package antifraud.stolencardsandsuspiciousips;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class CardsAndIPsController {

    @Resource
    SuspiciousIPRepository suspiciousIPRepository;

    @Resource
    StolenCardRepository stolenCardRepository;

    @PostMapping("/api/antifraud/suspicious-ip")
    public IPEntity saveIP(@Valid @RequestBody Map<String, String> ipJSON) {
        String ip = ipJSON.get("ip");

        if (isIPWrong(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (suspiciousIPRepository.findByIp(ip).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        IPEntity ipEntity = new IPEntity(ip);
        suspiciousIPRepository.save(ipEntity);

        return ipEntity;
    }

    private boolean isIPWrong(String ip) {
        Pattern pattern = Pattern.compile(
                "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");

        return !(pattern.matcher(ip).matches());
    }

    @DeleteMapping("/api/antifraud/suspicious-ip/{ip}")
    public Map<String, String> deleteIP(@PathVariable String ip) {

        if (isIPWrong(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        IPEntity iPEntity = suspiciousIPRepository.findByIp(ip)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        suspiciousIPRepository.delete(iPEntity);

        return Map.of(
                "status", "IP " + ip + " successfully removed!"
        );
    }

    @GetMapping("/api/antifraud/suspicious-ip")
    public List<IPEntity> getIPList() {
        return suspiciousIPRepository.findAll();
    }

    @PostMapping("/api/antifraud/stolencard")
    public CardEntity saveCard(@Valid @RequestBody Map<String, String> cardJSON) {
        String number = cardJSON.get("number");

        if (isCardNumberNonValid(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (stolenCardRepository.findByNumber(number).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        CardEntity cardEntity = new CardEntity(number);
        stolenCardRepository.save(cardEntity);

        return cardEntity;
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

    @DeleteMapping("/api/antifraud/stolencard/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable String number) {
        if (isCardNumberNonValid(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        CardEntity cardEntity = stolenCardRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        stolenCardRepository.delete(cardEntity);

        return Map.of(
                "status", "Card " + number + " successfully removed!"
        );
    }

    @GetMapping("/api/antifraud/stolencard")
    public List<CardEntity> getStolenCardsList() {
        return stolenCardRepository.findAll();
    }
}
