package siksha.wafflestudio.core.domain.post.data

import jakarta.persistence.*
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDateTime

@Entity(name = "post_like")
@Table(name = "post_like")
class PostLike(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    val isLiked: Boolean? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
