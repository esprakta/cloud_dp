package netology.cloud.service;

import netology.cloud.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("admin");
        testUser.setPassword("admin123");
    }

    @Test
    void login_shouldReturnToken_whenUserExists() {
        when(userService.findByLogin("admin")).thenReturn(Optional.of(testUser));

        String token = authService.login("admin", "admin123");

        assertNotNull(token);
        assertEquals(36, token.length()); // длина UUID
        verify(userService).findByLogin("admin");
    }

    @Test
    void login_shouldReturnNull_whenUserNotFound() {
        when(userService.findByLogin("unknown")).thenReturn(Optional.empty());

        String token = authService.login("unknown", "password");

        assertNull(token);
    }

    @Test
    void getUserByToken_shouldReturnNull_whenTokenIsInvalid() {
        User user = authService.getUserByToken("invalid-token");
        assertNull(user);
    }

    @Test
    void logout_shouldRemoveToken() {
        when(userService.findByLogin("admin")).thenReturn(Optional.of(testUser));

        String token = authService.login("admin", "admin123");
        authService.logout(token);

        User user = authService.getUserByToken(token);
        assertNull(user);
    }
}