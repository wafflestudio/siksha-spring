package siksha.wafflestudio.core.domain.post.data

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.user.data.User
import java.sql.Timestamp
import java.time.LocalDateTime

@Entity(name = "post")
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    var available: Boolean,
    val anonymous: Boolean,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val etc: String? = null,
)
