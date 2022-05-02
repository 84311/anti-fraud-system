package antifraud.suspiciousip;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/antifraud/suspicious-ip")
public class SuspiciousIPController {

    @Resource
    SuspiciousIPRepository suspiciousIPRepository;

    @PostMapping
    public SuspiciousIPEntity saveIP(@Valid @RequestBody Map<String, String> ipJSON) {
        String ip = ipJSON.get("ip");

        if (isIPWrong(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (suspiciousIPRepository.findByIp(ip).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        SuspiciousIPEntity suspiciousIpEntity = new SuspiciousIPEntity(ip);
        suspiciousIPRepository.save(suspiciousIpEntity);

        return suspiciousIpEntity;
    }

    private boolean isIPWrong(String ip) {
        Pattern pattern = Pattern.compile(
                "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");

        return !(pattern.matcher(ip).matches());
    }

    @DeleteMapping("/{ip}")
    public Map<String, String> deleteIP(@PathVariable String ip) {

        if (isIPWrong(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        SuspiciousIPEntity iPEntitySuspicious = suspiciousIPRepository.findByIp(ip)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        suspiciousIPRepository.delete(iPEntitySuspicious);

        return Map.of(
                "status", "IP " + ip + " successfully removed!"
        );
    }

    @GetMapping
    public List<SuspiciousIPEntity> getIPList() {
        return suspiciousIPRepository.findAll();
    }
}
