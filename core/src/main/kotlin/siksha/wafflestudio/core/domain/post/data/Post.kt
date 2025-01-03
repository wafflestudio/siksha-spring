package siksha.wafflestudio.core.domain.post.data

import jakarta.persistence.*
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDateTime

@Entity(name = "post")
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    val board: Board,

    @Column(name = "title", length = 200)
    val title: String,

    val content: String,
    val available: Boolean,
    val anonymous: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val etc: String? = null,
)
