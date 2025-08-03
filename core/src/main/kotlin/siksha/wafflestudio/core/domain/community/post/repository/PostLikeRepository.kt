package siksha.wafflestudio.core.domain.community.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.community.post.data.PostLike

@Repository
interface PostLikeRepository : JpaRepository<PostLike, Int> {
    @Query("SELECT pl FROM post_like pl WHERE pl.post.id IN :postIds AND pl.isLiked = true")
    fun findByPostIdInAndIsLikedTrue(postIds: List<Int>): List<PostLike>

    @Query("SELECT pl FROM post_like pl WHERE pl.post.id = :postId AND pl.isLiked = true")
    fun findByPostIdAndIsLikedTrue(postId: Int): List<PostLike>

    @Query("SELECT count(*) FROM post_like pl WHERE pl.post.id = :postId AND pl.isLiked = true")
    fun countPostLikesByPostIdAndLiked(postId: Int): Long

    fun findPostLikeByPostIdAndUserId(
        postId: Int,
        userId: Int,
    ): PostLike?
}
