package antifraud.transaction;

import antifraud.stolencard.StolenCardRepository;
import antifraud.suspiciousip.SuspiciousIPRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud/transaction")
public class TransactionController {

    @Resource
    SuspiciousIPRepository suspiciousIPRepository;

    @Resource
    StolenCardRepository stolenCardRepository;

    @Resource
    TransactionRepository transactionRepository;

    @PostMapping
    public Map<String, String> settleTransaction(@RequestBody TransactionEntity transaction) {
        if (transaction.amount <= 0 || transaction.ip.isEmpty() || transaction.number.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        transactionRepository.save(transaction);

        TransactionValidator.verifyTransaction(transaction, this);

        return Map.of(
                "result", transaction.result.name(),
                "info", transaction.info
        );
    }
}
