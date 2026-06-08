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

class RestaurantCustomV2Pk(
    var user: Int = 0,
    var restaurant: Int = 0,
) : Serializable

@Entity(name = "restaurant_custom_v2")
@Table(name = "restaurant_custom_v2")
@IdClass(RestaurantCustomV2Pk::class)
data class RestaurantCustomV2(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var restaurant: RestaurantV2,
    @Column(name = "`like`", nullable = false)
    var like: Boolean = false,
    @Column(name = "visible", nullable = false)
    var visible: Boolean = true,
    /** 사용자별 building 내부 restaurant 정렬 순서. */
    @Column(name = "order_index")
    var orderIndex: Int? = null,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
