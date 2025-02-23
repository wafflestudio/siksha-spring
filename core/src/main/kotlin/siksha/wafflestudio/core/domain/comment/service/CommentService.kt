package siksha.wafflestudio.core.domain.comment.service

import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.comment.data.Comment
import siksha.wafflestudio.core.domain.comment.data.CommentLike
import siksha.wafflestudio.core.domain.comment.data.CommentReport
import siksha.wafflestudio.core.domain.comment.dto.GetCommentsResponseDto
import siksha.wafflestudio.core.domain.comment.dto.CommentResponseDto
import siksha.wafflestudio.core.domain.comment.dto.CreateCommentRequestDto
import siksha.wafflestudio.core.domain.comment.dto.PatchCommentRequestDto
import siksha.wafflestudio.core.domain.comment.dto.CommentsReportResponseDto
import siksha.wafflestudio.core.domain.comment.repository.CommentLikeRepository
import siksha.wafflestudio.core.domain.comment.repository.CommentReportRepository
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
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
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.time.LocalDateTime
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
        postId: Long,
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
            CommentResponseDto(
                id = comment.id,
                postId = comment.post.id,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                nickname = if (comment.anonymous) null else comment.user.nickname,
                profileUri = if (comment.anonymous) null else comment.user.profileUrl,
                available = comment.available,
                anonymous = comment.anonymous,
                isMine = false,
                likeCnt = likeCount,
                isLiked = false,
            )
        }

        return GetCommentsResponseDto(
            result = commentDtos,
            totalCount = commentsPage.totalElements,
            hasNext = commentsPage.hasNext(),
        )
    }

    fun getComments(
        userId: Long,
        postId: Long,
        page: Int,
        perPage: Int,
    ):GetCommentsResponseDto {
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

            CommentResponseDto(
                id = comment.id,
                postId = comment.post.id,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                nickname = if (comment.anonymous) null else comment.user.nickname,
                profileUri = if (comment.anonymous) null else comment.user.profileUrl,
                available = comment.available,
                anonymous = comment.anonymous,
                isMine = comment.user.id == userId,
                likeCnt = likeCount,
                isLiked = isLiked,
            )
        }

        return GetCommentsResponseDto(
            result = commentDtos,
            totalCount = commentsPage.totalElements,
            hasNext = commentsPage.hasNext(),
        )
    }

    fun createComment(userId: Long, createDto: CreateCommentRequestDto): CommentResponseDto? {
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

        return CommentResponseDto(
            id = comment.id,
            postId = comment.post.id,
            content = comment.content,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
            nickname = if (comment.anonymous) null else comment.user.nickname,
            profileUri = if (comment.anonymous) null else comment.user.profileUrl,
            available = comment.available,
            anonymous = comment.anonymous,
            isMine = true,
            likeCnt = 0,
            isLiked = false,
        )
    }

    fun patchComment(userId: Long, commentId: Long, patchDto: PatchCommentRequestDto): CommentResponseDto {
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
                    updatedAt = LocalDateTime.now(),
                )
            )
        }.getOrElse {
            throw CustomNotFoundException(NotFoundItem.USER, NotFoundItem.POST)
        }

        val commentLikes = commentLikeRepository.findByCommentId(commentId)
        val isLiked = commentLikes.any { it.user.id == userId }

        return CommentResponseDto(
            id = newComment.id,
            postId = newComment.post.id,
            content = newComment.content,
            createdAt = newComment.createdAt,
            updatedAt = newComment.updatedAt,
            nickname = if (comment.anonymous) null else comment.user.nickname,
            profileUri = if (comment.anonymous) null else comment.user.profileUrl,
            available = newComment.available,
            anonymous = newComment.anonymous,
            isMine = true,
            likeCnt = commentLikes.count(),
            isLiked = isLiked,
        )
    }

    fun deleteComment(userId: Long, commentId: Long) {
        val comment = commentRepository.findById(commentId).getOrNull() ?: throw CommentNotFoundException()
        if (comment.user.id != userId) throw NotCommentOwnerException()

        commentRepository.deleteById(commentId)
    }

    fun createOrUpdateCommentLike(
        userId: Long,
        commentId: Long,
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
        reportingUid: Long,
        commentId: Long,
        reason: String,
    ): CommentsReportResponseDto {
        val reportingUser = userRepository.findByIdOrNull(reportingUid) ?: throw UnauthorizedUserException()
        val comment = commentRepository.findByIdOrNull(commentId) ?: throw CommentNotFoundException()

        if (reason.length > 200 || reason.isBlank()) {
            throw InvalidCommentReportFormException()
        }
        if (commentReportRepository.existsByCommentIdAndReportingUser(commentId, reportingUser)) {
            throw CommentAlreadyReportedException()
        }

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
    }
}
