package netology.cloud.service;

import lombok.RequiredArgsConstructor;
import netology.cloud.entity.CloudFile;
import netology.cloud.entity.User;
import netology.cloud.repository.FileRepository;
import netology.cloud.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    private final String uploadDir = "upload/";

    public List<CloudFile> findAllByUser(User user, int limit) {
        return fileRepository.findByUser(user, PageRequest.of(0, limit));
    }

    @Transactional
    public CloudFile uploadFile(User user, MultipartFile file, String filename) throws IOException {
        User attachedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        CloudFile cloudFile = new CloudFile();
        cloudFile.setFilename(filename);
        cloudFile.setSize(file.getSize());
        cloudFile.setFilePath(filePath.toString());
        cloudFile.setUser(attachedUser);

        return fileRepository.save(cloudFile);
    }

    public void deleteFile(User user, String filename) {
        CloudFile cloudFile = fileRepository.findByUserAndFilename(user, filename);
        if (cloudFile != null) {
            try {
                Files.deleteIfExists(Paths.get(cloudFile.getFilePath()));
            } catch (IOException e) {
                throw new RuntimeException("Error deleting file", e);
            }
            fileRepository.delete(cloudFile);
        }
    }

    public CloudFile getFile(User user, String filename) {
        return fileRepository.findByUserAndFilename(user, filename);
    }

    public void renameFile(User user, String oldFilename, String newFilename) {
        CloudFile cloudFile = fileRepository.findByUserAndFilename(user, oldFilename);
        if (cloudFile != null) {
            Path oldPath = Paths.get(cloudFile.getFilePath());
            Path newPath = oldPath.getParent().resolve(newFilename);

            try {
                Files.move(oldPath, newPath);
                cloudFile.setFilename(newFilename);
                cloudFile.setFilePath(newPath.toString());
                fileRepository.save(cloudFile);
            } catch (IOException e) {
                throw new RuntimeException("Error renaming file", e);
            }
        }
    }
}