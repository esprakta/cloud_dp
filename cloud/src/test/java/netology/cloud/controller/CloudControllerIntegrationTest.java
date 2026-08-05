package netology.cloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import netology.cloud.dto.LoginRequest;
import netology.cloud.dto.LoginResponse;
import netology.cloud.entity.User;
import netology.cloud.repository.FileRepository;
import netology.cloud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CloudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @BeforeEach
    void setUp() throws Exception {
        fileRepository.deleteAll();

        java.nio.file.Path uploadPath = java.nio.file.Paths.get("upload");
        if (java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.walk(uploadPath)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            java.nio.file.Files.deleteIfExists(p);
                        } catch (Exception e) {
                        }
                    });
        }

        if (userRepository.findByLogin("admin").isEmpty()) {
            User user = new User();
            user.setLogin("admin");
            user.setPassword("admin123");
            userRepository.save(user);
        }
    }

    @Test
    void login_shouldReturnToken_whenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("admin");
        request.setPassword("admin123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth-token").exists());
    }

    @Test
    void login_shouldReturn400_whenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("admin");
        request.setPassword("wrong");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.id").value(400));
    }

    @Test
    void uploadFile_shouldReturn200_whenAuthorized() throws Exception {
        String token = getToken();

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello".getBytes());

        mockMvc.perform(multipart("/file")
                        .file(file)
                        .param("filename", "test.txt")
                        .header("auth-token", token))
                .andExpect(status().isOk());
    }

    @Test
    void uploadFile_shouldReturn401_whenNoToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello".getBytes());

        mockMvc.perform(multipart("/file")
                        .file(file)
                        .param("filename", "test.txt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_shouldReturn200_whenAuthorized() throws Exception {
        String token = getToken();

        mockMvc.perform(get("/list")
                        .header("auth-token", token)
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void logout_shouldInvalidateToken() throws Exception {
        String token = getToken();

        mockMvc.perform(post("/logout")
                        .header("auth-token", token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/list")
                        .header("auth-token", token))
                .andExpect(status().isUnauthorized());
    }

    private String getToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("admin");
        request.setPassword("admin123");

        String response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, LoginResponse.class).getAuthToken();
    }
}