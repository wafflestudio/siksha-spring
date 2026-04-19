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
import siksha.wafflestudio.core.domain.user.data.User
import java.time.OffsetDateTime
import java.time.ZoneId

class RestaurantLikePk(
    val user: User? = null,
    val restaurant: Restaurant? = null,
)

@Entity(name = "restaurant_like")
@Table(name = "restaurant_like")
@IdClass(RestaurantLikePk::class)
data class RestaurantLike(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var restaurant: Restaurant,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
