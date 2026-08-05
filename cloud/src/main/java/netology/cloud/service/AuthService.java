package netology.cloud.service;

import lombok.RequiredArgsConstructor;
import netology.cloud.entity.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    private final Map<String, User> tokenStorage = new HashMap<>();

    public String login(String login, String password) {
        Optional<User> userOpt = userService.findByLogin(login);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                String token = UUID.randomUUID().toString();
                tokenStorage.put(token, user);
                return token;
            }
        }
        return null;
    }

    public void logout(String token) {
        tokenStorage.remove(token);
    }

    public User getUserByToken(String token) {
        return tokenStorage.get(token);
    }
}