package siksha.wafflestudio.core.infrastructure.imageupload.oci

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.exception.ImageUploadFailedException
import siksha.wafflestudio.core.infrastructure.imageupload.ImagePrefix
import siksha.wafflestudio.core.infrastructure.imageupload.ImageUploadUseCase
import siksha.wafflestudio.core.infrastructure.imageupload.UploadFileDto
import java.io.IOException

@Service
class OciObjectStorageService(
    @Value("\${oci.object-storage.namespace}") private val namespace: String,
    @Value("\${oci.object-storage.bucket}") private val bucketName: String,
    @Value("\${oci.object-storage.region}") private val region: String,
) : ImageUploadUseCase {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val authProvider: BasicAuthenticationDetailsProvider by lazy { createAuthProvider() }
    private val client: ObjectStorageClient by lazy {
        ObjectStorageClient.builder().region(region).build(authProvider)
    }

    private fun objectUrl(key: String) =
        "https://objectstorage.$region.oraclecloud.com/n/$namespace/b/$bucketName/o/${encodeKey(key)}"

    private fun encodeKey(key: String) = key.split("/").joinToString("/") { java.net.URLEncoder.encode(it, "UTF-8").replace("+", "%20") }

    override fun uploadFile(
        file: MultipartFile,
        prefix: ImagePrefix,
        nameKey: String,
    ): UploadFileDto {
        val key = "${prefix.prefix}/$nameKey.jpeg"
        return runCatching {
            val request =
                PutObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(key)
                    .contentLength(file.size)
                    .putObjectBody(file.inputStream)
                    .build()
            client.putObject(request)
            UploadFileDto(key = key, url = objectUrl(key))
        }.onFailure { e ->
            logger.error("OCI Object Storage upload err: ${e.message}")
            if (e is IOException) throw ImageUploadFailedException()
            throw e
        }.getOrThrow()
    }

    override fun uploadFiles(
        files: List<MultipartFile>,
        prefix: ImagePrefix,
        nameKey: String,
    ): List<UploadFileDto> {
        val uploaded = mutableListOf<UploadFileDto>()
        files.forEachIndexed { idx, file ->
            val key = "${prefix.prefix}/$nameKey/$idx.jpeg"
            runCatching {
                val request =
                    PutObjectRequest.builder()
                        .namespaceName(namespace)
                        .bucketName(bucketName)
                        .objectName(key)
                        .contentLength(file.size)
                        .putObjectBody(file.inputStream)
                        .build()
                client.putObject(request)
                uploaded.add(UploadFileDto(key = key, url = objectUrl(key)))
            }.onFailure { e ->
                logger.error("OCI Object Storage upload error: ${e.message}")
                throw ImageUploadFailedException()
            }.getOrThrow()
        }
        return uploaded
    }

    override fun getKeyFromUrl(url: String): String? {
        val pattern = Regex("https://objectstorage\\..*\\.oraclecloud\\.com/n/$namespace/b/[^/]+/o/(.*)")
        return pattern.matchEntire(url)?.groupValues?.get(1)?.let {
            java.net.URLDecoder.decode(it, "UTF-8")
        }
    }

    private fun createAuthProvider(): BasicAuthenticationDetailsProvider =
        try {
            ConfigFileAuthenticationDetailsProvider("DEFAULT")
        } catch (e: Exception) {
            logger.info("OCI config file auth failed; falling back to instance principal.", e)
            InstancePrincipalsAuthenticationDetailsProvider.builder().build()
        }
}
