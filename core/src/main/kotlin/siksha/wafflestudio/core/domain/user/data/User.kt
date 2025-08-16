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
@Table(
    name = "user",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["type", "identity"]),
        UniqueConstraint(columnNames = ["nickname"])
    ]
)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 10)
    val type: String,

    @Column(nullable = false, length = 200)
    val identity: String,

    @Column(columnDefinition = "TEXT")
    val etc: String? = null,

    @Column(nullable = false, length = 30)
    var nickname: String,

    @Column(length = 100)
    var profileUrl: String? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
