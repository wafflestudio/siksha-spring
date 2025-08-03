package siksha.wafflestudio.core.domain.main.menu.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "menu")
@Table(name = "menu")
data class Menu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,
    val code: String,
    val date: LocalDate,
    val type: String,
    val nameKr: String?,
    val nameEn: String?,
    val price: Int?,
    val etc: String?,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
