package siksha.wafflestudio.core.domain.post.data

import jakarta.persistence.*
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDateTime

@Entity(name = "post_report")
@Table(name = "post_report")
class PostReport(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @Column(name = "reason", length = 200, nullable = false)
    val reason: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_uid", nullable = true)
    val reportingUser: User?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_uid", nullable = false)
    val reportedUser: User,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
