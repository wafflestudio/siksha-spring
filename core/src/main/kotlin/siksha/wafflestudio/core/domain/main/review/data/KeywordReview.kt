package siksha.wafflestudio.core.domain.main.review.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import siksha.wafflestudio.core.domain.main.menu.data.Menu

@Entity(name = "keyword_review")
@Table(name = "keyword_review")
data class KeywordReview(
    @Id
    val id: Int = 0,

    val taste: Int,
    val price: Int,
    val foodComposition: Int,

    @OneToOne
    @MapsId
    @JoinColumn(name = "review_id")
    val review: Review,

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "restaurant_id", referencedColumnName = "restaurant_id"),
        JoinColumn(name = "menu_code", referencedColumnName = "code"),
        JoinColumn(name = "menu_date", referencedColumnName = "date"),
        JoinColumn(name = "menu_type", referencedColumnName = "type")
    )
    val menu: Menu,
)
