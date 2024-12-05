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

@Entity(name = "comment")
@Table(name = "comment")
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    val user: User,
    val postId: Long,
    val content: String,
    val available: Boolean,
    val anonymous: Boolean,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
)
