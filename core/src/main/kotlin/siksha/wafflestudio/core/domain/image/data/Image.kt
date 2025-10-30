package siksha.wafflestudio.core.domain.image.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "image")
@Table(name = "image")
class Image(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "`key`", length = 255)
    val key: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 10)
    val category: ImageCategory,
    // FIXME: 현재 DB에 외래키 안 걸려 있음
    @Column(nullable = false)
    val userId: Int,
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    val user: User,
    val isDeleted: Boolean = false,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)

// FIXME: POST 외래키 걸기
enum class ImageCategory {
    POST,
    PROFILE,
    REVIEW,
}
