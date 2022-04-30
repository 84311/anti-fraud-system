package antifraud.transaction;

import antifraud.stolencardsandsuspiciousips.StolenCardRepository;
import antifraud.stolencardsandsuspiciousips.SuspiciousIPRepository;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    @Resource
    SuspiciousIPRepository suspiciousIPRepository;

    @Resource
    StolenCardRepository stolenCardRepository;

    @PostMapping("/transaction")
    public Map<String, String> settleTransaction(@RequestBody TransactionDTO transaction) {
        long amount = transaction.amount;
        String ip = transaction.ip;
        String cardNumber = transaction.number;

        if (amount <= 0 || ip.isEmpty() || cardNumber.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String result;
        String info = "";
        boolean suspiciousIp = suspiciousIPRepository.findByIp(ip).isPresent();
        boolean stolenCard = stolenCardRepository.findByNumber(cardNumber).isPresent();
        boolean tooMuch = amount > 1500;

        if (suspiciousIp || stolenCard || tooMuch) {
            result = TransactionResult.PROHIBITED.name();
            info += tooMuch ? "amount, " : "";
            info += stolenCard ? "card-number, " : "";
            info += suspiciousIp ? "ip" : "";
        } else if (amount > 200) {
            result = TransactionResult.MANUAL_PROCESSING.name();
            info += "amount";
        } else {
            result = TransactionResult.ALLOWED.name();
            info = "none";
        }

        info = info.trim();
        info = info.endsWith(",") ? info.substring(0, info.length() - 1) : info;
        return Map.of(
                "result", result,
                "info", info
        );
    }

    @Data
    private static class TransactionDTO {
        long amount;
        String ip;
        String number;
    }
}
