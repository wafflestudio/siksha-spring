package siksha.wafflestudio.core.domain.main.menu.data

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
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "menu_v2")
@Table(
    name = "menu_v2",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["restaurant_id", "name"]),
    ],
)
class MenuV2(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: RestaurantV2,
    @Column(nullable = false, length = 200)
    val name: String,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
