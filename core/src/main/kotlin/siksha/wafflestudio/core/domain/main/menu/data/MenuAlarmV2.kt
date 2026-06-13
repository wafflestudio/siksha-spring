package siksha.wafflestudio.core.domain.main.menu.data

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.user.data.User
import java.io.Serializable
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "menu_alarm_v2")
@Table(name = "menu_alarm_v2")
class MenuAlarmV2(
    @EmbeddedId
    val id: MenuAlarmV2Id,
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @MapsId("menuId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    val menu: MenuV2,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
) : Serializable {
    constructor(
        user: User,
        menu: MenuV2,
    ) : this(
        id = MenuAlarmV2Id(userId = user.id, menuId = menu.id),
        user = user,
        menu = menu,
    )
}

@Embeddable
data class MenuAlarmV2Id(
    @Column(name = "user_id")
    var userId: Int = 0,
    @Column(name = "menu_id")
    var menuId: Long = 0,
) : Serializable
