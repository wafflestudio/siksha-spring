package siksha.wafflestudio.core.domain.user.data
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.sql.Timestamp

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
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
)
