package org.delivery.db.user

import org.delivery.db.user.enums.UserStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findFirstByIdAndStatusOrderByIdDesc(userId: Long, userStatus: UserStatus): UserEntity?
    fun findFirstByEmailAndPasswordAndStatusOrderByIdDesc(
        userEmail: String?,
        password: String?,
        status: UserStatus?
    ): UserEntity?

}