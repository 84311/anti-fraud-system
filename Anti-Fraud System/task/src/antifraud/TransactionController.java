package antifraud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class TransactionController {

    @PostMapping("/api/antifraud/transaction")
    public ResponseEntity<Map<String, String>> settleTransaction(@RequestBody Map<String, Long> amountJSON) {
        long amount = amountJSON.get("amount") == null ? -1 : amountJSON.get("amount");
        String result;
        int status = 200;

        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (amount <= 200) {
            result = TransactionResult.ALLOWED.name();
        } else if (amount <= 1500) {
            result = TransactionResult.MANUAL_PROCESSING.name();
        } else {
            result = TransactionResult.PROHIBITED.name();
        }

        return ResponseEntity
                .status(status)
                .body(Map.of("result", result));
    }
}
