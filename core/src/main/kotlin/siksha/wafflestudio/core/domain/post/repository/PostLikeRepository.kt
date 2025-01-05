package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.post.data.PostLike

@Repository
interface PostLikeRepository: JpaRepository<PostLike, Long> {
    @Query("SELECT pl FROM post_like pl WHERE pl.post.id IN :postIds")
    fun findByPostIdIn(postIds: List<Long>): List<PostLike>

    @Query("SELECT pl FROM post_like pl WHERE pl.post.id = :postId AND pl.user.id = :userId")
    fun findByPostIdAndUserId(postId: Long, userId: Long): PostLike?
}
