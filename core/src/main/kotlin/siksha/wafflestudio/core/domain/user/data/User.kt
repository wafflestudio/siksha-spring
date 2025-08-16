package siksha.wafflestudio.core.domain.user.data
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "user")
@Table(name = "user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val type: String,
    val identity: String,
    val etc: String? = null,
    var nickname: String,
    var profileUrl: String? = null,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
