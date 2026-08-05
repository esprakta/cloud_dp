package netology.cloud.service;

import netology.cloud.entity.CloudFile;
import netology.cloud.entity.User;
import netology.cloud.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    @Test
    void testFileServiceNotNull() {
        assertNotNull(fileService);
    }

    @Test
    void getFile_shouldReturnFile_whenExists() {
        User user = new User();
        CloudFile file = new CloudFile();
        file.setFilename("test.txt");
        file.setSize(100L);

        when(fileRepository.findByUserAndFilename(user, "test.txt")).thenReturn(file);

        CloudFile result = fileService.getFile(user, "test.txt");

        assertNotNull(result);
        assertEquals("test.txt", result.getFilename());
        verify(fileRepository).findByUserAndFilename(user, "test.txt");
    }

    @Test
    void getFile_shouldReturnNull_whenNotExists() {
        User user = new User();
        when(fileRepository.findByUserAndFilename(user, "missing.txt")).thenReturn(null);

        CloudFile result = fileService.getFile(user, "missing.txt");

        assertNull(result);
    }
}