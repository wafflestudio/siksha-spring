package siksha.wafflestudio.core.infrastructure.imageupload

import org.springframework.web.multipart.MultipartFile

interface ImageUploadUseCase {
    fun uploadFile(
        file: MultipartFile,
        prefix: ImagePrefix,
        nameKey: String,
    ): UploadFileDto

    fun uploadFiles(
        files: List<MultipartFile>,
        prefix: ImagePrefix,
        nameKey: String,
    ): List<UploadFileDto>

    fun getKeyFromUrl(url: String): String?
}

data class UploadFileDto(
    val key: String,
    val url: String,
)

enum class ImagePrefix(val prefix: String) {
    POST("post-images"),
    PROFILE("profile-images"),
    REVIEW("review-images"),
}
