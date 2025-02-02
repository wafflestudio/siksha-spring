package siksha.wafflestudio.core.domain.user.data
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import java.time.LocalDateTime

@Entity(name = "user")
@Table(name = "user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L,
    val type: String,
    val identity: String,
    val etc: String? = null,
    val nickname: String,
    val profileUrl: String? = null,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
