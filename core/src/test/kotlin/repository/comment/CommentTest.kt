package siksha.wafflestudio.core.repository.comment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.community.comment.data.Comment
import siksha.wafflestudio.core.domain.community.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.community.post.data.Post
import siksha.wafflestudio.core.domain.community.post.repository.PostRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentTest {
    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var postRepository: PostRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var boardRepository: BoardRepository

    @Test
    fun `save comment`() {
        // given
        val user = userRepository.save(User(type = "test", nickname = "test", identity = "test"))
        val board = boardRepository.save(Board(name = "test", description = "test", type = 1))
        val post = postRepository.save(Post(user = user, board = board, title = "test", content = "test", anonymous = false, available = true))
        val comment = Comment(
            post = post,
            user = user,
            content = "new comment",
            anonymous = false,
            available = true
        )

        // when
        val savedComment = commentRepository.save(comment)
        val retrievedComment = commentRepository.findByIdOrNull(savedComment.id)

        // then
        assertNotNull(retrievedComment)
        assertEquals(comment.content, retrievedComment!!.content)
        assertEquals(user.id, retrievedComment.user.id)
        assertEquals(post.id, retrievedComment.post.id)
    }

    @Test
    @Sql("/data/v001.sql")
    fun `get comment`() {
        // when
        val retrievedComment = commentRepository.findByIdOrNull(1)

        // then
        assertNotNull(retrievedComment)
        assertEquals("와~ 첫 댓글이라니 감격이에요", retrievedComment!!.content)
    }

    @Test
    fun `update comment`() {
        // given
        val user = userRepository.save(User(type = "test", nickname = "test", identity = "test"))
        val board = boardRepository.save(Board(name = "test", description = "test", type = 1))
        val post = postRepository.save(Post(user = user, board = board, title = "test", content = "test", anonymous = false, available = true))
        val comment = commentRepository.save(
            Comment(
                post = post,
                user = user,
                content = "new comment",
                anonymous = false,
                available = true
            )
        )

        // when
        comment.available = false
        commentRepository.flush()
        val updatedComment = commentRepository.findByIdOrNull(comment.id)

        // then
        assertNotNull(updatedComment)
        assertEquals(false, updatedComment!!.available)
    }

    @Test
    fun `delete comment`() {
        // given
        val user = userRepository.save(User(type = "test", nickname = "test", identity = "test"))
        val board = boardRepository.save(Board(name = "test", description = "test", type = 1))
        val post = postRepository.save(Post(user = user, board = board, title = "test", content = "test", anonymous = false, available = true))
        val comment = commentRepository.save(
            Comment(
                post = post,
                user = user,
                content = "new comment",
                anonymous = false,
                available = true
            )
        )

        // when
        commentRepository.deleteById(comment.id)
        val deletedComment = commentRepository.findByIdOrNull(comment.id)

        // then
        assertNull(deletedComment)
    }

    @Test
    @Sql("/data/v001.sql")
    fun `findPageByPostId should return paged comments`() {
        // given
        val postId = 1
        val pageable = PageRequest.of(0, 5)

        // when
        val commentPage = commentRepository.findPageByPostId(postId, pageable)

        // then
        assertEquals(1, commentPage.content.size)
        assertEquals(1, commentPage.totalElements)
        assertEquals("와~ 첫 댓글이라니 감격이에요", commentPage.content[0].content)
    }

    @Test
    @Sql("/data/v001.sql")
    fun `findByPostIdIn should return comments for given post ids`() {
        // given
        val postIds = listOf(1, 2, 3)

        // when
        val comments = commentRepository.findByPostIdIn(postIds)

        // then
        assertEquals(3, comments.size)
        assertTrue(comments.any { it.post.id == 1 })
        assertTrue(comments.any { it.post.id == 2 })
        assertTrue(comments.any { it.post.id == 3 })
    }

    @Test
    @Sql("/data/v001.sql")
    fun `countByPostId should return correct comment count`() {
        // given
        val postId = 1

        // when
        val count = commentRepository.countByPostId(postId)

        // then
        assertEquals(1, count)
    }

    @Test
    @Sql("/data/v001.sql")
    fun `countCommentsByPostId should return correct comment count`() {
        // given
        val postId = 1

        // when
        val count = commentRepository.countCommentsByPostId(postId)

        // then
        assertEquals(1, count)
    }
}