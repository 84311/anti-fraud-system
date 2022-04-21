package antifraud.authentication;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthorizationController {
    private final PasswordEncoder encoder;

    @Resource
    UserRepository userRepository;

    @Autowired
    public AuthorizationController(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @PostMapping("/api/auth/user")
    public ResponseEntity<User> register(@RequestBody UserDTO userDTO) {
        if (userDTO.name == null || userDTO.username == null || userDTO.password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByUsernameIgnoreCase(userDTO.username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        User newUser = new User(
                userDTO.name,
                userDTO.username,
                encoder.encode(userDTO.password),
                "user"
        );

        userRepository.save(newUser);

        return ResponseEntity
                .status(201)
                .body(newUser);
    }

    @GetMapping("/api/auth/list")
    public List<User> register() {
        return userRepository.findAll();
    }

    @DeleteMapping("/api/auth/user/{username}")
    public Map<String, String> deleteUser(@PathVariable String username) {
        Optional<User> userToDelete = userRepository.findByUsernameIgnoreCase(username);

        if (userToDelete.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        userRepository.delete(userToDelete.get());

        return Map.of(
                "status", "Deleted successfully!",
                "username", username
        );
    }
}

@Setter
class UserDTO {
    String name;
    String username;
    String password;
}
