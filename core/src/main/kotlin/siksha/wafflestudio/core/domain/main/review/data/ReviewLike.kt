package siksha.wafflestudio.core.domain.main.review.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.user.data.User
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "review_like")
@Table(name = "review_like",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "review_id"]),
    ],
)
data class ReviewLike(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val review: Review,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
