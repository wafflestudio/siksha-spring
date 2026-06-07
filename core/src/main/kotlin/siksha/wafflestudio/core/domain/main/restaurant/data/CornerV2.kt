package siksha.wafflestudio.core.domain.main.restaurant.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "corner_v2")
@Table(name = "corner_v2")
class CornerV2(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: RestaurantV2,
    @Column(length = 100)
    val name: String? = null,
    @Column(nullable = false)
    val isDefault: Boolean = false,
    @Column(nullable = false)
    val active: Boolean = true,
    @Column(nullable = false)
    val displayOrder: Int = 0,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
