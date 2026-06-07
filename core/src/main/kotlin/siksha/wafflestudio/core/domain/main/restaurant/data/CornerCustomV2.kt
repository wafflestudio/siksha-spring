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

class CornerCustomV2Pk(
    var user: Int = 0,
    var corner: Int = 0,
) : Serializable

@Entity(name = "corner_custom_v2")
@Table(name = "corner_custom_v2")
@IdClass(CornerCustomV2Pk::class)
data class CornerCustomV2(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User,
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var corner: CornerV2,
    @Column(name = "`like`", nullable = false)
    var like: Boolean = false,
    @Column(nullable = false)
    var visible: Boolean = true,
    @Column(name = "order_index")
    var orderIndex: Int? = null,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)
