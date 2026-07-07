package siksha.wafflestudio.core.domain.main.review.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity(name = "keyword_review_v2")
@Table(name = "keyword_review_v2")
class KeywordReviewV2(
    @Id
    val id: Long = 0,
    @Column(nullable = false)
    var taste: Int = -1,
    @Column(nullable = false)
    var price: Int = -1,
    @Column(name = "food_composition", nullable = false)
    var foodComposition: Int = -1,
    @OneToOne
    @MapsId
    @JoinColumn(name = "review_id")
    val review: ReviewV2,
)
