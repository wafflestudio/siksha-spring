package siksha.wafflestudio.core.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.user.data.User

@Repository
interface UserRepository: JpaRepository<User, Int> {
    fun existsByNickname(nickname: String): Boolean
}
