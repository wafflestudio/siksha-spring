package siksha.wafflestudio.core.domain.user.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.exception.DuplicatedNicknameException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.dto.UserWithProfileUrlResponseDto
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class UserService(
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
    private val s3Service: S3Service,
) {
    fun getUser(userId: Int): UserResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        return UserResponseDto.from(user)
    }

    fun getUserWithProfileUrl(userId: Int): UserWithProfileUrlResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        return UserWithProfileUrlResponseDto.from(user)
    }

    @Transactional
    fun deleteUser(userId: Int) {
        if (!userRepository.existsById(userId)) throw UserNotFoundException()
        userRepository.deleteById(userId)
    }

    @Transactional
    fun patchUser(userId: Int, patchDto: UserProfilePatchDto): UserResponseDto {
        var user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        patchDto.let {
            it.nickname?.let { nickname ->
                this.validateNickname(nickname)
                user.nickname = nickname
            }

            // TODO: refactor this after modify API request spec
            if (it.changeToDefaultImage) {
                user = this.deleteProfileImage(user)
            } else if (it.image != null) {
                val profileUrl = this.uploadProfileImage(userId, it.image)
                user.profileUrl = profileUrl
            }
        }

        userRepository.save(user)
        return UserResponseDto.from(user)
    }

    // TODO remove duplicated code
    @Transactional
    fun patchUserWithProfileUrl(userId: Int, patchDto: UserProfilePatchDto): UserWithProfileUrlResponseDto {
        var user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        patchDto.let {
            it.nickname?.let { nickname ->
                this.validateNickname(nickname)
                user.nickname = nickname
            }

            // TODO: refactor this after modify API request spec
            if (it.changeToDefaultImage) {
                user = this.deleteProfileImage(user)
            } else if (it.image != null) {
                val profileUrl = this.uploadProfileImage(userId, it.image)
                user.profileUrl = profileUrl
            }
        }

        userRepository.save(user)
        return UserWithProfileUrlResponseDto.from(user)
    }

    /**
     * throws exception if the nickname is invalid (duplicated or contains banned words)
     * this method does not guarantee that there is no duplicated entry, due to race condition
     * @param nickname: String
     * @return null
     */
    private fun validateNickname(nickname: String) {
        // TODO: implement this
        if (this.userRepository.existsByNickname(nickname)) throw DuplicatedNicknameException()
        return
    }

    private fun generateImageNameKey(userId: Int) = "user-$userId/${OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))}"

    /**
     * upload a profile image to S3, and save the url to DB
     * @param userId: Int
     * @param image: MultipartFile
     * @return S3 url of uploaded image
     */
    private fun uploadProfileImage(userId: Int, image: MultipartFile): String {
        val nameKey = generateImageNameKey(userId)
        val uploadFile = s3Service.uploadFile(image, S3ImagePrefix.PROFILE, nameKey)

        val imageEntity = Image(
            key = uploadFile.key,
            category = ImageCategory.PROFILE,
            userId = userId,
            isDeleted = false
        )
        imageRepository.save(imageEntity)

        return uploadFile.url
    }

    /**
     * reset profile image to default
     * soft-delete from DB
     */
    private fun deleteProfileImage(user: User): User {
        user.profileUrl?.let {
            val oldKey = s3Service.getKeyFromUrl(it)
            oldKey?.let {key ->
                imageRepository.softDeleteByKeyIn(listOf(key))
            }
            user.profileUrl = null
        }
        return user
    }

}
