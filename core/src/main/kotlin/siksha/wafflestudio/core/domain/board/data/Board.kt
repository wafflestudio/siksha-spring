package siksha.wafflestudio.core.domain.board.data

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp

@Entity(name = "board")
@Table(name = "board")
data class Board(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false, unique = true, length = 200)
    val name: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String,
    @Column(nullable = false)
    val type: Int = 1,
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: Timestamp = Timestamp(System.currentTimeMillis()),
    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: Timestamp = Timestamp(System.currentTimeMillis()),
)
