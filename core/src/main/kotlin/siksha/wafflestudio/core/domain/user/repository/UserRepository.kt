<<<<<<< HEAD
package siksha.wafflestudio.core.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.user.data.User

@Repository
interface UserRepository: JpaRepository<User, Long> {
}
=======
package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.user.data.User

interface UserRepository : JpaRepository<User, Long>
>>>>>>> f6f24d3 (댓글 api들 추가)
