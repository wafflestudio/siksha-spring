package siksha.wafflestudio.core.domain.main.review.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity(name = "keyword_review")
@Table(name = "keyword_review")
data class KeywordReview(
    @Id
    val id: Int = 0,

    val flavor: Int,
    val price: Int,
    val foodComposition: Int,

    @OneToOne
    @MapsId
    @JoinColumn(name = "review_id")
    val review: Review
)
