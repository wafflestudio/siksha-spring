package siksha.wafflestudio.core.domain.user.service

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.auth.social.data.SocialProfile
import siksha.wafflestudio.core.domain.common.exception.BannedWordException
import siksha.wafflestudio.core.domain.common.exception.DuplicatedNicknameException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * User 객체의 CRUD를 다루는 서비스입니다.
 * 인증, 회원가입 등은 AuthService의 책임입니다.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
    private val s3Service: S3Service,
    @Value("\${siksha.banned.words:}") private val bannedWords: List<String>,
) {
    fun getUser(userId: Int): UserResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        return UserResponseDto.from(user)
    }

    @Transactional
    fun upsertUser(socialProfile: SocialProfile): UserResponseDto {
        val type = socialProfile.provider.toString()
        val identity = socialProfile.externalId

        // update if exists
        userRepository.findByTypeAndIdentity(type, identity)
            ?.let { return UserResponseDto.from(it) }

        // insert if not exists
        val toSave =
            User(
                type = type,
                identity = identity,
                nickname = NicknameGenerator.generate(),
            )

        val created =
            try {
                userRepository.save(toSave)
            } catch (e: DataIntegrityViolationException) {
                // race condition
                userRepository.findByTypeAndIdentity(type, identity)
                    ?: throw e
            }

        return UserResponseDto.from(created)
    }

    @Transactional
    fun deleteUser(userId: Int) {
        if (!userRepository.existsById(userId)) throw UserNotFoundException()
        userRepository.deleteById(userId)
    }

    @Transactional
    fun patchUser(
        userId: Int,
        patchDto: UserProfilePatchDto,
    ): UserResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        patchDto.let {
            if (it.nickname != user.nickname && it.nickname != null) {
                this.validateNickname(it.nickname)
                user.nickname = it.nickname
            }

            // TODO: refactor this after modify API request spec
            if (it.changeToDefaultImage) {
                this.deleteProfileImage(user)
            } else if (it.image != null) {
                val profileUrl = this.uploadProfileImage(userId, it.image)
                user.profileUrl = profileUrl
            }
        }

        userRepository.save(user)
        return UserResponseDto.from(user)
    }

    /**
     * validates a nickname (uniqueness and filtering banned words)
     * this method does not guarantee that there is no duplicated entry, due to potential race conditions.
     * @param nickname The nickname to validate
     * @throws DuplicatedNicknameException if the nickname already exists
     * @throws BannedWordException if the nickname contains banned words
     * @return true if valid
     */
    fun validateNickname(nickname: String): Boolean {
        if (userRepository.existsByNickname(nickname)) throw DuplicatedNicknameException()
        if (bannedWords.any { nickname.contains(it, ignoreCase = true) }) throw BannedWordException()
        return true
    }

    private fun generateImageNameKey(userId: Int) =
        "user-$userId/${OffsetDateTime.now().format(
            DateTimeFormatter.ofPattern("yyMMddHHmmss"),
        )}"

    /**
     * Uploads a profile image to S3 and saves its metadata to the database.
     * @param userId The ID of the user uploading the profile image.
     * @param image The new profile image file
     * @return The public S3 URL of the new image
     */
    private fun uploadProfileImage(
        userId: Int,
        image: MultipartFile,
    ): String {
        val nameKey = generateImageNameKey(userId)
        val uploadFile = s3Service.uploadFile(image, S3ImagePrefix.PROFILE, nameKey)

        val imageEntity =
            Image(
                key = uploadFile.key,
                category = ImageCategory.PROFILE,
                userId = userId,
                isDeleted = false,
            )
        imageRepository.save(imageEntity)

        return uploadFile.url
    }

    /**
     * Resets the user's profile image to the default by setting the profileUrl to null.
     * This method must be called within a @Transactional context to ensure that changes to the User entity are persisted.
     *
     * @param user The User entity to modify.
     */
    private fun deleteProfileImage(user: User) {
        user.profileUrl?.let {
            val oldKey = s3Service.getKeyFromUrl(it)
            oldKey?.let { key ->
                imageRepository.softDeleteByKeyIn(listOf(key))
            }
            user.profileUrl = null
        }
    }
}
