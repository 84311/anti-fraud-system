package antifraud.transaction;

import antifraud.card.CardEntity;
import antifraud.card.CardRepository;
import antifraud.suspiciousip.SuspiciousIPRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class TransactionValidator {
    private TransactionEntity transaction;
    @Resource
    private SuspiciousIPRepository suspiciousIPRepository;
    @Resource
    private CardRepository cardRepository;
    @Resource
    private TransactionRepository transactionRepository;

    private Set<String> info;

    public static boolean isCardNumberNonValid(String number) {
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

    public static boolean isFeedbackFormatWrong(String feedback) {
        return Arrays.stream(TransactionResult.values())
                .noneMatch(r -> r.name().equals(feedback));
    }

    private void checkIfSuspiciousIP() {
        if (suspiciousIPRepository.findByIp(transaction.ip).isPresent()) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("ip");
        }
    }

    public void verifyTransaction(TransactionEntity transaction) {
        this.transaction = transaction;
        this.info = new TreeSet<>();
        transaction.result = TransactionResult.ALLOWED;

        checkIfStolenCard();
        checkIfSuspiciousIP();
        checkIfCorrelation();
        checkIfTooMuchMoney();

        transaction.info = formatInfo();
    }

    private void checkIfStolenCard() {
        if (cardRepository.existsByNumberAndLockedTrue(transaction.number)) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("card-number");
        }
    }

    private String formatInfo() {
        if (transaction.result == TransactionResult.ALLOWED) {
            info.add("none");
        }

        return String.join(", ", info);
    }

    private void checkIfCorrelation() {
        List<TransactionEntity> oneHourBeforeTransaction =
                transactionRepository.findAllByDateBetweenAndNumber(
                        transaction.date.minusHours(1), transaction.date, transaction.number);

        long regionCount = oneHourBeforeTransaction.stream()
                .map(TransactionEntity::getRegion)
                .filter(r -> r != transaction.region)
                .distinct().count();

        long ipCount = oneHourBeforeTransaction.stream()
                .map(TransactionEntity::getIp)
                .filter(ip -> !Objects.equals(ip, transaction.ip))
                .distinct().count();

        if (regionCount == 2 && transaction.result != TransactionResult.PROHIBITED) {
            transaction.result = TransactionResult.MANUAL_PROCESSING;
            info.add("region-correlation");
        }

        if (ipCount == 2 && transaction.result != TransactionResult.PROHIBITED) {
            transaction.result = TransactionResult.MANUAL_PROCESSING;
            info.add("ip-correlation");
        }

        if (regionCount > 2) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("region-correlation");
        }

        if (ipCount > 2) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("ip-correlation");
        }
    }

    private void checkIfTooMuchMoney() {
        CardEntity card = cardRepository.findByNumber(transaction.number)
                .orElseThrow(AssertionError::new);

        int allowedLimit = card.getAllowedLimit();
        int manualLimit = card.getManualLimit();

        if (transaction.amount > allowedLimit && transaction.amount <= manualLimit
                && transaction.result != TransactionResult.PROHIBITED) {

            transaction.result = TransactionResult.MANUAL_PROCESSING;
            info.add("amount");
        }

        if (transaction.amount > manualLimit) {
            if (transaction.result != TransactionResult.PROHIBITED) {
                info.clear();
            }
            info.add("amount");
            transaction.result = TransactionResult.PROHIBITED;
        }
    }
}
