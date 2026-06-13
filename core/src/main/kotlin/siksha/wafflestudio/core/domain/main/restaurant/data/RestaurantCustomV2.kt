package siksha.wafflestudio.core.domain.main.restaurant.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "restaurant_custom_v2")
@Table(name = "restaurant_custom_v2")
data class RestaurantCustomV2(
    @Id
    @Column(name = "user_id", nullable = false)
    val userId: Int,
    @Column(nullable = false, columnDefinition = "json")
    var customs: String = "{}",
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
