package siksha.wafflestudio.core.domain.post.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.application.post.dto.PostCreateRequestDto
import siksha.wafflestudio.core.application.post.dto.PostPatchRequestDto
import siksha.wafflestudio.core.domain.common.exception.InvalidPostFormException
import siksha.wafflestudio.core.domain.post.data.Post

@Service
class PostDomainService {
    // TODO: remove this class
    fun validatePost(post: Post) {
        if (post.title.isBlank() || post.title.length > 200) {
            throw InvalidPostFormException("제목은 1자에서 200자 사이여야 합니다.")
        }
        if (post.content.isBlank() || post.content.length > 1000) {
            throw InvalidPostFormException("내용은 1자에서 1000자 사이여야 합니다.")
        }
    }

    fun validateDto(postCreateRequestDto: PostCreateRequestDto) {
        if (postCreateRequestDto.title.isBlank() || postCreateRequestDto.title.length > 200) {
            throw InvalidPostFormException("제목은 1자에서 200자 사이여야 합니다.")
        }
        if (postCreateRequestDto.content.isBlank() || postCreateRequestDto.content.length > 1000) {
            throw InvalidPostFormException("내용은 1자에서 1000자 사이여야 합니다.")
        }
    }

    fun validatePatchDto(postPatchRequestDto: PostPatchRequestDto) {
        postPatchRequestDto.title?.let {
            if (it.isBlank() || it.length > 200) {
                throw InvalidPostFormException("제목은 1자에서 200자 사이여야 합니다.")
            }
        }
        postPatchRequestDto.content?.let {
            if (it.isBlank() || it.length > 1000) {
                throw InvalidPostFormException("내용은 1자에서 1000자 사이여야 합니다.")
            }
        }
    }
}
