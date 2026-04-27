package siksha.wafflestudio.core.domain.main.meal.data

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
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2

@Entity(name = "meal_menu_v2")
@Table(
    name = "meal_menu_v2",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["meal_id", "menu_id"]),
    ],
)
class MealMenuV2(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    val meal: MealV2,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    val menu: MenuV2,
    @Column(nullable = false, length = 300)
    val originalName: String,
)
