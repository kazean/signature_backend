package org.delivery.db.user;

import org.delivery.db.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/*

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findFirstByIdAndStatusOrderByIdDesc(Long userId, UserStatus status);
    Optional<UserEntity> findFirstByEmailAndPasswordAndStatusOrderByIdDesc(String userEmail, String password, UserStatus status);
}
*/
