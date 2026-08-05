package netology.cloud.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RenameFileRequest {
    private String name;
}