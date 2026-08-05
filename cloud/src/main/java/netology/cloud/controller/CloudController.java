package netology.cloud.controller;

import lombok.RequiredArgsConstructor;
import netology.cloud.dto.*;
import netology.cloud.entity.CloudFile;
import netology.cloud.entity.User;
import netology.cloud.exception.UnauthorizedException;
import netology.cloud.service.AuthService;
import netology.cloud.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CloudController {
    private final AuthService authService;
    private final FileService fileService;

    private User getUserFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException("Token is missing");
        }
        User user = authService.getUserByToken(token);
        if (user == null) {
            throw new UnauthorizedException("Invalid token");
        }
        return user;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getLogin(), request.getPassword());

        if (token != null) {
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Bad credentials");
            error.put("id", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "auth-token", required = false) String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/file")
    public ResponseEntity<Void> uploadFile(
            @RequestHeader(value = "auth-token", required = false) String token,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) {

        try {
            User user = getUserFromToken(token);
            fileService.uploadFile(user, file, filename);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(
            @RequestHeader(value = "auth-token", required = false) String token,
            @RequestParam("filename") String filename) {

        try {
            User user = getUserFromToken(token);
            fileService.deleteFile(user, filename);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> downloadFile(
            @RequestHeader(value = "auth-token", required = false) String token,
            @RequestParam("filename") String filename) {

        try {
            User user = getUserFromToken(token);
            CloudFile cloudFile = fileService.getFile(user, filename);

            if (cloudFile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Path filePath = Paths.get(cloudFile.getFilePath());
            byte[] fileContent = Files.readAllBytes(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cloudFile.getFilename() + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/file")
    public ResponseEntity<Void> renameFile(
            @RequestHeader(value = "auth-token", required = false) String token,
            @RequestParam("filename") String oldFilename,
            @RequestBody RenameFileRequest request) {

        try {
            User user = getUserFromToken(token);
            fileService.renameFile(user, oldFilename, request.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> getFileList(
            @RequestHeader(value = "auth-token", required = false) String token,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        User user = getUserFromToken(token);
        List<CloudFile> files = fileService.findAllByUser(user, limit);

        List<FileInfo> fileInfoList = files.stream()
                .map(f -> new FileInfo(f.getFilename(), f.getSize()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(fileInfoList);
    }
}