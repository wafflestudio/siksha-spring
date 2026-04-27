package siksha.wafflestudio.core.domain.main.restaurant.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "restaurant_v2")
@Table(name = "restaurant_v2")
class RestaurantV2(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true, length = 100)
    val name: String,
    @Column(length = 50)
    val building: String? = null,
    @Column(length = 200)
    val address: String? = null,
    @Column(precision = 10, scale = 7)
    val latitude: BigDecimal? = null,
    @Column(precision = 10, scale = 7)
    val longitude: BigDecimal? = null,
    @Column(columnDefinition = "json")
    val operatingHours: String? = null,
    @Column(name = "owner_id")
    val ownerId: Int? = null,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
