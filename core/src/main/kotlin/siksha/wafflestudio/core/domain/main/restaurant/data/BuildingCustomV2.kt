package siksha.wafflestudio.core.domain.main.restaurant.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.user.data.User
import java.io.Serializable
import java.time.OffsetDateTime
import java.time.ZoneId

class BuildingCustomV2Pk(
    var user: Int = 0,
    var building: Int = 0,
) : Serializable

@Entity(name = "building_custom_v2")
@Table(name = "building_custom_v2")
@IdClass(BuildingCustomV2Pk::class)
data class BuildingCustomV2(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var building: BuildingV2,
    @Column(name = "order_index")
    var orderIndex: Int? = null,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
