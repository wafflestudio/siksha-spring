package siksha.wafflestudio.core.domain.comment.data

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDateTime

@Entity(name = "comment_report")
@Table(name = "comment_report")
class CommentReport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: Comment,
    @Column(name = "reason", length = 200, nullable = false)
    val reason: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_uid", nullable = true)
    val reportingUser: User?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_uid", nullable = false)
    val reportedUser: User,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
