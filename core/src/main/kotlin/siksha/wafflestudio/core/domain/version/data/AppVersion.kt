package siksha.wafflestudio.core.domain.version.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import siksha.wafflestudio.core.domain.common.exception.InvalidClientTypeException
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity(name = "version")
@Table(name = "version")
data class AppVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "minimum_version")
    val minimumVersion: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", length = 10)
    val clientType: ClientType,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
)

enum class ClientType {
    AND,
    IOS,
    WEB,
    ;

    companion object {
        fun from(value: String): ClientType =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw InvalidClientTypeException(value)
    }
}
