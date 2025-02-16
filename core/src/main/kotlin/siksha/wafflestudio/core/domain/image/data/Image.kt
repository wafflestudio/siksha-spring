package siksha.wafflestudio.core.domain.image.data

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import java.time.LocalDateTime

@Entity(name = "image")
@Table(name = "image")
class Image(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "`key`", length = 60)
    val key: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 10)
    val category: ImageCategory,

    // FIXME: 현재 DB에 외래키 안 걸려 있음
    @Column(nullable = false)
    val userId: Long,

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    val user: User,

    val isDeleted: Boolean = false,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

// FIXME: POST 외래키 걸기
enum class ImageCategory {
    POST,
    PROFILE,
    REVIEW
}
