package antifraud.transaction;

import antifraud.stolencard.StolenCardRepository;
import antifraud.suspiciousip.SuspiciousIPRepository;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TransactionValidator {
    private final TransactionEntity transaction;
    private final SuspiciousIPRepository suspiciousIPRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;

    private final Set<String> info = new TreeSet<>();

    private TransactionValidator(TransactionEntity transaction,
                                 SuspiciousIPRepository suspiciousIPRepository,
                                 StolenCardRepository stolenCardRepository,
                                 TransactionRepository transactionRepository) {
        this.transaction = transaction;
        this.suspiciousIPRepository = suspiciousIPRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionRepository = transactionRepository;
    }

    public static void verifyTransaction(TransactionEntity transaction, TransactionController controller) {

        TransactionValidator transactionValidator = new TransactionValidator(
                transaction,
                controller.suspiciousIPRepository,
                controller.stolenCardRepository,
                controller.transactionRepository);

        transactionValidator.verifyTransaction();
    }

    private void verifyTransaction() {
        transaction.result = TransactionResult.ALLOWED;

        checkIfStolenCard();
        checkIfSuspiciousIP();
        checkIfCorrelation();
        checkIfTooMuchMoney();

        transaction.info = formatInfo();
    }

    private void checkIfStolenCard() {
        if (stolenCardRepository.findByNumber(transaction.number).isPresent()) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("card-number");
        }
    }

    private void checkIfSuspiciousIP() {
        if (suspiciousIPRepository.findByIp(transaction.ip).isPresent()) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("ip");
        }
    }

    private void checkIfCorrelation() {
        List<TransactionEntity> oneHourBeforeTransaction =
                transactionRepository.findAllByDateBetweenAndNumber(
                        transaction.date.minusHours(1), transaction.date, transaction.number);

        long regionCount = oneHourBeforeTransaction.stream().map(TransactionEntity::getRegion).distinct().count();
        long ipCount = oneHourBeforeTransaction.stream().map(TransactionEntity::getIp).distinct().count();

        if (regionCount == 3 && transaction.result != TransactionResult.PROHIBITED) {
            transaction.result = TransactionResult.MANUAL_PROCESSING;
            info.add("region-correlation");
        }

        if (ipCount == 3 && transaction.result != TransactionResult.PROHIBITED) {
            transaction.result = TransactionResult.MANUAL_PROCESSING;
            info.add("ip-correlation");
        }

        if (regionCount > 3) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("region-correlation");
        }

        if (ipCount > 3) {
            transaction.result = TransactionResult.PROHIBITED;
            info.add("ip-correlation");
        }
    }

    private void checkIfTooMuchMoney() {
        if (transaction.amount > 200 && transaction.amount <= 1500 && transaction.result != TransactionResult.PROHIBITED) {
            transaction.result = TransactionResult.MANUAL_PROCESSING;
            info.add("amount");
        }

        if (transaction.amount > 1500) {
            if (transaction.result != TransactionResult.PROHIBITED) {
                info.clear();
            }
            info.add("amount");
            transaction.result = TransactionResult.PROHIBITED;
        }
    }

    private String formatInfo() {
        if (transaction.result == TransactionResult.ALLOWED) {
            info.add("none");
        }

        return String.join(", ", info);
    }
}
