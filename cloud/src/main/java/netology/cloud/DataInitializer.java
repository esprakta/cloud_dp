package netology.cloud;

import lombok.RequiredArgsConstructor;
import netology.cloud.entity.User;
import netology.cloud.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        userRepository.findByLogin("admin").ifPresentOrElse(
                user -> System.out.println("Пользователь 'admin' уже существует в базе данных"),
                () -> {
                    User user = new User();
                    user.setLogin("admin");
                    user.setPassword("admin123");
                    userRepository.save(user);
                    System.out.println("Пользователь 'admin' успешно создан в базе данных!");
                }
        );
    }
}