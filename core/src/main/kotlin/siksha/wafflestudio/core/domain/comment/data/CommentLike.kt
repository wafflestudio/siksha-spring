package siksha.wafflestudio.core.domain.comment.data

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import siksha.wafflestudio.core.domain.user.data.User
import java.sql.Timestamp
import java.time.LocalDateTime

@Entity(name = "comment_like")
@Table(name = "comment_like")
class CommentLike(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: Comment,

    val isLiked: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
