package siksha.wafflestudio.core.domain.restaurant.data

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.sql.Timestamp

@Entity(name = "restaurant")
@Table(name = "restaurant")
class Restaurant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val code: String,
    val nameKr: String?,
    val nameEn: String?,
    val addr: String?,
    val lat: Double?,
    val lng: Double?,
    val etc: String?,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
)
