package antifraud.transaction;

import antifraud.card.CardEntity;
import antifraud.card.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    @Resource
    private CardRepository cardRepository;

    @Resource
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionValidator trValidator;

    @PostMapping("/transaction")
    public Map<String, String> settleTransaction(@RequestBody TransactionEntity transaction) {
        if (transaction.amount <= 0 || transaction.ip.isEmpty() || transaction.number.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        saveCardIfNotExist(transaction.number);

        trValidator.verifyTransaction(transaction);
        transactionRepository.save(transaction);

        return Map.of(
                "result", transaction.result.name(),
                "info", transaction.info
        );
    }

    private void saveCardIfNotExist(String number) {
        if (cardRepository.findByNumber(number).isEmpty()) {
            cardRepository.save(new CardEntity(number, false));
        }
    }

    @PutMapping("/transaction")
    public TransactionEntity addFeedback(@RequestBody Map<String, String> trIDAndFeedback) {
        long transactionId = Long.parseLong(trIDAndFeedback.get("transactionId"));
        String feedback = trIDAndFeedback.get("feedback");

        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (TransactionValidator.isFeedbackFormatWrong(feedback))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        else if (transaction.feedback != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        else if (transaction.result.name().equals(feedback))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);

        changeLimit(transaction, feedback);

        transaction.feedback = TransactionResult.valueOf(feedback);
        transactionRepository.save(transaction);

        return transaction;
    }

    private void changeLimit(TransactionEntity transaction, String feedback) {
        String trResult = transaction.result.name();
        CardEntity card = cardRepository.findByNumber(transaction.number)
                .orElseThrow(AssertionError::new);

        int increasedAllowed = (int) Math.ceil(0.8 * card.getAllowedLimit() + 0.2 * transaction.amount);
        int decreasedAllowed = (int) Math.ceil(0.8 * card.getAllowedLimit() - 0.2 * transaction.amount);
        int increasedManual = (int) Math.ceil(0.8 * card.getManualLimit() + 0.2 * transaction.amount);
        int decreasedManual = (int) Math.ceil(0.8 * card.getManualLimit() - 0.2 * transaction.amount);

        if (feedback.equals("MANUAL_PROCESSING") && trResult.equals("ALLOWED")) {
            card.setAllowedLimit(decreasedAllowed);
        } else if (feedback.equals("PROHIBITED") && trResult.equals("ALLOWED")) {
            card.setAllowedLimit(decreasedAllowed);
            card.setManualLimit(decreasedManual);
        } else if (feedback.equals("ALLOWED") && trResult.equals("MANUAL_PROCESSING")) {
            card.setAllowedLimit(increasedAllowed);
        } else if (feedback.equals("PROHIBITED") && trResult.equals("MANUAL_PROCESSING")) {
            card.setManualLimit(decreasedManual);
        } else if (feedback.equals("ALLOWED") && trResult.equals("PROHIBITED")) {
            card.setAllowedLimit(increasedAllowed);
            card.setManualLimit(increasedManual);
        } else if (feedback.equals("MANUAL_PROCESSING") && trResult.equals("PROHIBITED")) {
            card.setManualLimit(increasedManual);
        }

        cardRepository.save(card);
    }

    @GetMapping("/history")
    public List<TransactionEntity> getHistory() {
        return transactionRepository.findAll();
    }

    @GetMapping("/history/{number}")
    public List<TransactionEntity> getHistoryForCardNumber(@PathVariable String number) {
        if (TransactionValidator.isCardNumberNonValid(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<TransactionEntity> transactions = transactionRepository.findAllByNumber(number);

        if (transactions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return transactions;
    }
}
