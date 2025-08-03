package siksha.wafflestudio.core.domain.image.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.image.data.Image

@Repository
interface ImageRepository : JpaRepository<Image, Long> {
    @Modifying
    @Query("UPDATE image i SET i.isDeleted = true WHERE i.key IN :keys")
    fun softDeleteByKeyIn(
        @Param("keys") keys: List<String>,
    ): Int
}
