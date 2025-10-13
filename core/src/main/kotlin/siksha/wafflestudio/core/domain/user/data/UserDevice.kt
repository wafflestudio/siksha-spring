package siksha.wafflestudio.core.domain.user.data
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "user_device")
@Table(
    name = "user_device",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "fcm_token"]),
    ],
)
data class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    val fcmToken: String,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
