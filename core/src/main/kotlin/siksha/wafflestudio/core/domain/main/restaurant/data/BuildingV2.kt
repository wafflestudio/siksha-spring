package siksha.wafflestudio.core.domain.main.restaurant.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "building_v2")
@Table(name = "building_v2")
class BuildingV2(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(nullable = false, unique = true, length = 20)
    val number: String,
    @Column(length = 100)
    val name: String? = null,
    @Column(nullable = false)
    val sortOrder: Int = 0,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
