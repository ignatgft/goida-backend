package ru.goidaai.test_backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.UserAvatar;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, String> {

    List<UserAvatar> findByUserId(String userId);

    Optional<UserAvatar> findByUserIdAndIsActive(String userId, Boolean isActive);

    List<UserAvatar> findByUserIdOrderByUploadedAtDesc(String userId);

    @Modifying
    @Query("UPDATE UserAvatar a SET a.isActive = false WHERE a.user = :user")
    void deactivateAllByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE UserAvatar a SET a.isActive = true WHERE a.id = :id AND a.user = :user")
    void activateByIdAndUser(@Param("id") String id, @Param("user") User user);
}
