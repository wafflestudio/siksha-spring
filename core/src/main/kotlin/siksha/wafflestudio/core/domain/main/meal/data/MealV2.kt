package siksha.wafflestudio.core.domain.main.meal.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "meal_v2")
@Table(name = "meal_v2")
class MealV2(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: RestaurantV2,
    @Column(nullable = false)
    val date: LocalDate,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MealType,
    val price: Int? = null,
    @Column(nullable = false)
    val noMeat: Boolean = false,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
}
