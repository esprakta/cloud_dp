package netology.cloud.repository;

import netology.cloud.entity.CloudFile;
import netology.cloud.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<CloudFile, Long> {
    List<CloudFile> findByUser(User user, Pageable pageable);

    boolean existsByUserAndFilename(User user, String filename);

    CloudFile findByUserAndFilename(User user, String filename);
}