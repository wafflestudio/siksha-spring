package siksha.wafflestudio.core.domain.community.comment.service

import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.community.comment.data.Comment
import siksha.wafflestudio.core.domain.community.comment.data.CommentLike
import siksha.wafflestudio.core.domain.community.comment.data.CommentReport
import siksha.wafflestudio.core.domain.community.comment.dto.GetCommentsResponseDto
import siksha.wafflestudio.core.domain.community.comment.dto.CommentResponseDto
import siksha.wafflestudio.core.domain.community.comment.dto.CreateCommentRequestDto
import siksha.wafflestudio.core.domain.community.comment.dto.PatchCommentRequestDto
import siksha.wafflestudio.core.domain.community.comment.dto.CommentsReportResponseDto
import siksha.wafflestudio.core.domain.community.comment.repository.CommentLikeRepository
import siksha.wafflestudio.core.domain.community.comment.repository.CommentReportRepository
import siksha.wafflestudio.core.domain.community.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.common.exception.InvalidPageNumberException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.common.exception.CommentNotFoundException
import siksha.wafflestudio.core.domain.common.exception.PostNotFoundException
import siksha.wafflestudio.core.domain.common.exception.NotCommentOwnerException
import siksha.wafflestudio.core.domain.common.exception.CustomNotFoundException
import siksha.wafflestudio.core.domain.common.exception.NotFoundItem
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.common.exception.InvalidCommentReportFormException
import siksha.wafflestudio.core.domain.common.exception.CommentAlreadyReportedException
import siksha.wafflestudio.core.domain.common.exception.CommentReportSaveFailedException
import siksha.wafflestudio.core.domain.community.post.repository.PostRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.time.OffsetDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class CommentService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val commentLikeRepository: CommentLikeRepository,
    private val commentReportRepository: CommentReportRepository,
) {
    fun getCommentsWithoutAuth(
        postId: Int,
        page: Int,
        perPage: Int,
    ): GetCommentsResponseDto {
        val pageable = PageRequest.of(page-1, perPage)
        val commentsPage = commentRepository.findPageByPostId(postId, pageable)
        if (commentsPage.isEmpty && commentsPage.totalElements > 0) throw InvalidPageNumberException()
        val comments = commentsPage.content
        val commentIdToCommentLikes = commentLikeRepository.findByCommentIdInAndIsLiked(comments.map { it.id }).groupBy { it.comment.id }
        val commentDtos = comments.map { comment ->
            val likeCount = commentIdToCommentLikes[comment.id]?.size ?: 0
            CommentResponseDto.of(
                comment = comment,
                isMine = false,
                likeCount = likeCount,
                isLiked = false
            )
        }

        return GetCommentsResponseDto(
            result = commentDtos,
            totalCount = commentsPage.totalElements,
            hasNext = commentsPage.hasNext(),
        )
    }

    fun getComments(
        userId: Int,
        postId: Int,
        page: Int,
        perPage: Int,
    ): GetCommentsResponseDto {
        val pageable = PageRequest.of(page-1, perPage)
        val commentsPage = commentRepository.findPageByPostId(postId, pageable)
        if (commentsPage.isEmpty && commentsPage.totalElements > 0) throw InvalidPageNumberException()
        val comments = commentsPage.content

        val commentLikes = commentLikeRepository.findByCommentIdInAndIsLiked(comments.map { it.id })
        val commentIdsILiked = commentLikes.filter { it.user.id == userId }.map { it.comment.id }.toSet()
        val commentIdToCommentLikes = commentLikes.groupBy { it.comment.id }

        val commentDtos = comments.map { comment ->
            val likeCount = commentIdToCommentLikes[comment.id]?.size ?: 0
            val isLiked = comment.id in commentIdsILiked

            CommentResponseDto.of(
                comment = comment,
                isMine = comment.user.id == userId,
                likeCount = likeCount,
                isLiked = isLiked
            )
        }

        return GetCommentsResponseDto(
            result = commentDtos,
            totalCount = commentsPage.totalElements,
            hasNext = commentsPage.hasNext(),
        )
    }

    fun createComment(userId: Int, createDto: CreateCommentRequestDto): CommentResponseDto? {
        val me = userRepository.findById(userId).getOrNull() ?: throw UserNotFoundException()
        val post = postRepository.findById(createDto.postId).getOrNull() ?: throw PostNotFoundException()

        val comment = commentRepository.save(
            Comment(
                user = me,
                post = post,
                content= createDto.content,
                available = true,
                anonymous = createDto.anonymous
            )
        )

        return CommentResponseDto.of(
            comment = comment,
            isMine = true,
            likeCount = 0,
            isLiked = false,
        )
    }

    fun patchComment(userId: Int, commentId: Int, patchDto: PatchCommentRequestDto): CommentResponseDto {
        val comment = commentRepository.findById(commentId).getOrNull() ?: throw CommentNotFoundException()
        if (comment.user.id != userId) throw NotCommentOwnerException()

        val newComment = runCatching {
            commentRepository.save(
                Comment(
                    id = comment.id,
                    user = comment.user,
                    post = comment.post,
                    content = patchDto.content ?: comment.content,
                    available = comment.available,
                    anonymous = patchDto.anonymous ?: comment.anonymous,
                    createdAt = comment.createdAt,
                    updatedAt = OffsetDateTime.now(),
                )
            )
        }.getOrElse {
            throw CustomNotFoundException(NotFoundItem.USER, NotFoundItem.POST)
        }

        val commentLikes = commentLikeRepository.findByCommentId(commentId)
        val isLiked = commentLikes.any { it.user.id == userId }

        return CommentResponseDto.of(
            comment = comment,
            isMine = true,
            likeCount = commentLikes.count(),
            isLiked = isLiked,
        )
    }

    fun deleteComment(userId: Int, commentId: Int) {
        val comment = commentRepository.findById(commentId).getOrNull() ?: throw CommentNotFoundException()
        if (comment.user.id != userId) throw NotCommentOwnerException()

        commentRepository.deleteById(commentId)
    }

    fun createOrUpdateCommentLike(
        userId: Int,
        commentId: Int,
        isLiked: Boolean,
    ): CommentResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val comment = commentRepository.findByIdOrNull(commentId) ?: throw CommentNotFoundException()

        val commentLike = commentLikeRepository.findCommentLikeByCommentIdAndUserId(commentId, userId)
            ?: CommentLike(
                user = user,
                comment = comment,
                isLiked = isLiked,
            )

        commentLike.isLiked = isLiked
        commentLikeRepository.save(commentLike)

        val likeCount = commentLikeRepository.countCommentLikesByCommentIdAndIsLiked(commentId)
        return CommentResponseDto.of(
            comment = comment,
            isMine = comment.user.id == userId,
            likeCount = likeCount.toInt(),
            isLiked = isLiked
        )
    }

    @Transactional
    fun createCommentReport(
        reportingUid: Int,
        commentId: Int,
        reason: String,
    ): CommentsReportResponseDto {
        val reportingUser = userRepository.findByIdOrNull(reportingUid) ?: throw UnauthorizedUserException()
        val comment = commentRepository.findByIdOrNull(commentId) ?: throw CommentNotFoundException()

        if (reason.length > 200 || reason.isBlank()) {
            throw InvalidCommentReportFormException()
        }

        try {
            val commentReport = commentReportRepository.save(
                CommentReport(
                    comment = comment,
                    reason = reason,
                    reportingUser = reportingUser,
                    reportedUser = comment.user,
                )
            )

            //신고 5개 이상 누적시 숨기기
            val commentReportCount = commentReportRepository.countCommentReportByCommentId(commentId)
            if (commentReportCount >= 5 && comment.available) {
                comment.available = false
                commentRepository.save(comment)
            }

            return CommentsReportResponseDto(
                id = commentReport.id,
                reason = commentReport.reason,
                commentId = commentReport.comment.id,
            )
        } catch (ex: DataIntegrityViolationException) {
            throw CommentAlreadyReportedException()
        } catch (ex: Exception) {
            throw CommentReportSaveFailedException()
        }
    }
}
