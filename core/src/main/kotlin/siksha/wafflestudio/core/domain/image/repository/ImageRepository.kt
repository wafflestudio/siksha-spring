package siksha.wafflestudio.core.domain.image.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.image.data.Image

@Repository
interface ImageRepository: JpaRepository<Image, Long> {
}
