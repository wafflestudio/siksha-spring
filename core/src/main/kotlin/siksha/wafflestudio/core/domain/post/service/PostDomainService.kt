package siksha.wafflestudio.core.domain.post.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.application.post.dto.PostCreateDto
import siksha.wafflestudio.core.domain.common.exception.InvalidBoardFormException
import siksha.wafflestudio.core.domain.common.exception.InvalidPostFormException
import siksha.wafflestudio.core.domain.post.data.Post

@Service
class PostDomainService {
    fun validatePost(post: Post) {
        val errors = mutableListOf<String>()
        if (post.title.isBlank() || post.title.length > 200) {
            errors.add("제목은 1자에서 200자 사이여야 합니다.")
        }
        if (post.content.isBlank() || post.content.length > 1000) {
            errors.add("내용은 1자에서 1000자 사이여야 합니다.")
        }
        if (errors.isNotEmpty()) {
            throw InvalidPostFormException("$errors")
        }
    }

    fun validateDto(postCreateDto: PostCreateDto) {
        val errors = mutableListOf<String>()
        if (postCreateDto.title.isBlank() || postCreateDto.title.length > 200) {
            errors.add("제목은 1자에서 200자 사이여야 합니다.")
        }
        if (postCreateDto.content.isBlank() || postCreateDto.content.length > 1000) {
            errors.add("내용은 1자에서 1000자 사이여야 합니다.")
        }
        if (errors.isNotEmpty()) {
            throw InvalidPostFormException("$errors")
        }
    }
}
