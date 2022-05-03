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
@RequestMapping("/api/auth")
public class AuthorizationController {
    private final PasswordEncoder encoder;

    @Resource
    private UserRepository userRepository;

    @Autowired
    public AuthorizationController(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @PostMapping("/user")
    public ResponseEntity<User> register(@RequestBody UserDTO userDTO) {
        if (userDTO.name == null || userDTO.username == null || userDTO.password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByUsernameIgnoreCase(userDTO.username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        String role = userRepository.findAll().isEmpty()
                ? Role.ADMINISTRATOR.asStringWithRolePrefix : Role.MERCHANT.asStringWithRolePrefix;

        boolean isAccountNonLocked = userRepository.findAll().isEmpty();

        User newUser = new User(
                userDTO.name,
                userDTO.username,
                encoder.encode(userDTO.password),
                role,
                isAccountNonLocked
        );

        userRepository.save(newUser);

        return ResponseEntity
                .status(201)
                .body(newUser);
    }

    @GetMapping("/list")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/user/{username}")
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

    @PutMapping("/role")
    public User updateRole(@RequestBody Map<String, String> usernameAndRole) {
        String username = usernameAndRole.get("username");
        String role = usernameAndRole.get("role");

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!Role.valuesAsStrings().contains(role) || role.equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (user.getRoleWithoutPrefix().equals(role)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        user.setRole("ROLE_" + role);
        userRepository.save(user);
        return user;
    }

    @PutMapping("/access")
    public Map<String, String> toggleUserLock(@RequestBody Map<String, String> usernameAndLockValue) {
        String username = usernameAndLockValue.get("username");
        String operation = usernameAndLockValue.get("operation");

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getRole().equals("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (operation.equals("LOCK")) {
            user.setAccountNonLocked(false);
            userRepository.save(user);
            return Map.of("status", "User " + user.getUsername() + " locked!");
        } else if (operation.equals("UNLOCK")) {
            user.setAccountNonLocked(true);
            userRepository.save(user);
            return Map.of("status", "User " + user.getUsername() + " unlocked!");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}

@Setter
class UserDTO {
    String name;
    String username;
    String password;
}
