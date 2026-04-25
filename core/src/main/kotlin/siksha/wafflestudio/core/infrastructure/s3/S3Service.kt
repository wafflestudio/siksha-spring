package siksha.wafflestudio.core.infrastructure.s3

import io.awspring.cloud.s3.S3Exception
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.exception.image.ImageUploadFailedException
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.IOException

@Service
class S3Service(
    @Value("\${spring.cloud.aws.s3.bucket}")
    private val bucketName: String,
    private val s3Client: S3Client,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * AWS S3 bucket에 하나의 파일을 업로드합니다.
     * @param file 업로드할 파일
     * @param prefix S3 key에 사용할 접두사
     * @param nameKey S3 key에 사용할 파일명
     * @return UploadFileDto
     */
    fun uploadFile(
        file: MultipartFile,
        prefix: S3ImagePrefix,
        nameKey: String,
    ): UploadFileDto {
        val key = "${prefix.prefix}/$nameKey.jpeg"

        return runCatching {
            val putObjectRequest =
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build()

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.inputStream, file.size))
            UploadFileDto(key = key, url = "https://$bucketName.s3.ap-northeast-2.amazonaws.com/$key")
        }.onFailure { e ->
            when (e) {
                is S3Exception, is SdkClientException, is IOException -> {
                    logger.error("AWS S3 upload err: ${e.message}")
                    throw e
                }
                else -> throw e
            }
        }.getOrThrow()
    }

    /**
     * AWS S3 bucket에 여러 파일을 업로드합니다.
     * @param file 업로드할 파일
     * @param prefix S3 key에 사용할 접두사
     * @param nameKey S3 key에 공통으로 사용할 파일명
     * @return List<UploadFileDto>
     */
    fun uploadFiles(
        files: List<MultipartFile>,
        prefix: S3ImagePrefix,
        nameKey: String,
    ): List<UploadFileDto> {
        val uploadFiles = mutableListOf<UploadFileDto>()

        files.forEachIndexed { idx, file ->
            val key = "${prefix.prefix}/$nameKey/$idx.jpeg"
            runCatching {
                val putObjectRequest =
                    PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build()

                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.inputStream, file.size))
                uploadFiles.add(
                    UploadFileDto(key = key, url = "https://$bucketName.s3.ap-northeast-2.amazonaws.com/$key"),
                )
            }.onFailure { e ->
                when (e) {
                    is S3Exception, is SdkClientException, is IOException, is AwsServiceException -> {
                        logger.error("AWS S3 upload error: ${e.message}")
                        throw ImageUploadFailedException()
                    }
                    else -> throw e
                }
            }.getOrThrow()
        }
        return uploadFiles
    }

    fun getKeyFromUrl(url: String): String? {
        val pattern = Regex("https://.*\\.s3\\.ap-northeast-2\\.amazonaws\\.com/(.*)")
        val match = pattern.matchEntire(url)

        return match?.groupValues?.get(1)
    }
}

data class UploadFileDto(
    val key: String,
    val url: String,
)

enum class S3ImagePrefix(val prefix: String) {
    POST("post-images"),
    PROFILE("profile-images"),
    REVIEW("review-images"),
}
