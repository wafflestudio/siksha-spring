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
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "restaurant_v2")
@Table(
    name = "restaurant_v2",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_restaurant_v2_name", columnNames = ["name"]),
        UniqueConstraint(name = "uk_restaurant_v2_building_order", columnNames = ["building_id", "default_order"]),
        UniqueConstraint(name = "uk_restaurant_v2_id_building", columnNames = ["id", "building_id"]),
    ],
)
class RestaurantV2(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    val building: BuildingV2,
    @Column(nullable = false, length = 100)
    val name: String,
    @Column(columnDefinition = "json")
    val operatingHours: String? = null,
    @Column(name = "owner_id")
    val ownerId: Int? = null,
    @Column(name = "default_order", nullable = false)
    val defaultOrder: Int = 0,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
