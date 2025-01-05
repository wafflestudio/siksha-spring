package siksha.wafflestudio.core.infrastructure.s3

import io.awspring.cloud.s3.S3Exception
import org.springframework.stereotype.Service
import io.awspring.cloud.s3.S3Template
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.exception.S3ImageUploadException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Str
import java.io.IOException
import java.net.URL

@Service
class S3Service (
    @Value("\${spring.cloud.aws.s3.bucket}")
    private val bucketName: String,
    private val s3Template: S3Template,
) {
    /**
     * AWS S3 bucket에 하나의 파일을 업로드합니다.
     * @param file 업로드할 파일('MultipartFile')
     * @param prefix S3 key에 사용할 접두사 ("review-images", "post-images" 등 이미지 종류)
     * @param nameKey S3 key에 사용할 파일명
     * @return (S3 URL, S3 key) Pair
     */
    fun uploadOneFile(file: MultipartFile, prefix: String, nameKey: String): Pair<String, String> {
        val key = "$prefix/$nameKey.jpeg"
        val logger = LoggerFactory.getLogger(javaClass)
        return try {
            val s3Resource = s3Template.upload(bucketName, key, file.inputStream)
            Pair(s3Resource.url.toString(), key)
        } catch (e: Exception) {
            when (e) {
                is S3Exception, is SdkClientException, is IOException -> {
                    logger.error("AWS S3 upload err: ${e.message}")
                    throw e
                }
                else -> throw e
            }
        }
    }

    /**
     * AWS S3 bucket에 여러 파일을 업로드합니다.
     * @param file 업로드할 파일('List<MultipartFile>')
     * @param prefix S3 key에 사용할 접두사 ("review-images", "post-images" 등 이미지 종류)
     * @param nameKey S3 key에 공통으로 사용할 파일명
     * @return (S3 URLs List, S3 keys List) Pair
     */
    fun uploadFiles(files: List<MultipartFile>, prefix: String, nameKey: String): Pair<List<String>, List<String>> {
        val logger = LoggerFactory.getLogger(javaClass)
        val urls = mutableListOf<String>()//mutableListOf<Pair<URL, String>>()
        val keys = mutableListOf<String>()

        files.forEachIndexed { idx, file ->
            val key = "$prefix/$nameKey/$idx.jpeg"
            try {
                val s3Resource = s3Template.upload(bucketName, key, file.inputStream)
                urls.add(s3Resource.url.toString())
                keys.add(key)
            } catch (e: Exception) {
                when (e) {
                    is S3Exception, is SdkClientException, is IOException -> {
                        logger.error("AWS S3 upload error: ${e.message}")
                        throw S3ImageUploadException()
                    }
                    else -> throw e
                }
            }
        }

        return Pair(urls, keys)
    }
}

