package siksha.wafflestudio.core

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository

@Entity(name = "restaurants")
class Restaurant(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0L
)

interface RestaurantRepository : JpaRepository<Restaurant, Long>

