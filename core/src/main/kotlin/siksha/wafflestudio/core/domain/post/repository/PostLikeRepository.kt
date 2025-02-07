package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.data.PostLike

@Repository
<<<<<<< HEAD
interface PostLikeRepository: JpaRepository<PostLike, Long> {
    @Query("SELECT pl FROM post_like pl WHERE pl.post.id IN :postIds AND pl.isLiked = true")
    fun findByPostIdInAndIsLikedTrue(postIds: List<Long>): List<PostLike>

    @Query("SELECT pl FROM post_like pl WHERE pl.post.id = :postId AND pl.isLiked = true")
    fun findByPostIdAndIsLikedTrue(postId: Long): List<PostLike>

    @Query("SELECT count(*) FROM post_like pl WHERE pl.post.id = :postId AND pl.isLiked = true")
    fun countPostLikesByPostIdAndLiked(postId: Long): Long

    fun findPostLikeByPostIdAndUserId(postId: Long, userId: Long): PostLike?
=======
interface PostLikeRepository: JpaRepository<PostLike, Int> {
    @Query("SELECT pl FROM post_like pl WHERE pl.post.id IN :postIds")
    fun findByPostIdIn(postIds: List<Int>): List<PostLike>

    @Query("SELECT pl FROM post_like pl WHERE pl.post.id = :postId AND pl.user.id = :userId")
    fun findByPostIdAndUserId(postId: Int, userId: Int): PostLike?
>>>>>>> d75df59 (feat: flyway 세팅, id 값 long -> int)
}
