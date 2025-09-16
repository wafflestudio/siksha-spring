package siksha.wafflestudio.core.domain.main.review.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.user.data.User
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "review")
@Table(name = "review")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    val menu: Menu,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    var score: Int,
    @Column(nullable = true)
    var comment: String?,
    var etc: String?,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
