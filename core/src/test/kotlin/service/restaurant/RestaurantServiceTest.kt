package siksha.wafflestudio.core.domain.main.restaurant.service

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.domain.v1.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.v1.main.restaurant.data.RestaurantCustom
import siksha.wafflestudio.core.domain.v1.main.restaurant.dto.RestaurantResponseDto
import siksha.wafflestudio.core.domain.v1.main.restaurant.repository.RestaurantCustomRepository
import siksha.wafflestudio.core.domain.v1.main.restaurant.repository.RestaurantRepository
import siksha.wafflestudio.core.domain.v1.main.restaurant.service.RestaurantService
import kotlin.test.assertNotNull

class RestaurantServiceTest {
    private lateinit var restaurantRepository: RestaurantRepository
    private lateinit var userRepository: UserRepository
    private lateinit var restaurantCustomRepository: RestaurantCustomRepository
    private lateinit var service: RestaurantService

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        restaurantRepository = mockk()
        userRepository = mockk()
        restaurantCustomRepository = mockk()
        service = RestaurantService(restaurantRepository, userRepository, restaurantCustomRepository)
    }

    @Test
    fun `get restaurants`() {
        // given
        val restaurant =
            Restaurant(
                id = 1,
                code = "test",
                nameKr = "test",
                nameEn = "test",
                addr = "test",
                lat = 0.0,
                lng = 0.0,
                etc = null,
            )
        every { restaurantRepository.findAll() } returns listOf(restaurant)

        // when
        val result = service.getAllRestaurants()

        // then
        assertNotNull(result)
        assertEquals(result.result.size, result.count)
        assertEquals(1, result.result[0].id)
    }

    @Test
    fun `festival easter egg moves Sillichochi first without changing other restaurants`() {
        val first = restaurant(3, "[축제]홍등반점")
        val ordinary = restaurant(1, "학생회관")
        val featured = restaurant(589, "[축제]실리꼬치밸리")
        val last = restaurant(2, "[축제]판타스틱붐")
        val original = listOf(first, ordinary, featured, last)
        every { restaurantRepository.findAll() } returns original

        val result = service.getAllRestaurants()

        assertEquals(4, result.count)
        assertEquals(listOf(featured, first, ordinary, last).map { RestaurantResponseDto.from(it) }, result.result)
        assertEquals(listOf(3, 1, 589, 2), original.map { it.id })
    }

    @Test
    fun `list stays unchanged without the exact festival code`() {
        val original = listOf(restaurant(295, "실리꼬치밸리"), restaurant(2, "[축제]실리꼬치밸리2"))
        every { restaurantRepository.findAll() } returns original

        assertEquals(original.map { RestaurantResponseDto.from(it) }, service.getAllRestaurants().result)
    }

    @Test
    fun `empty list stays empty`() {
        every { restaurantRepository.findAll() } returns emptyList()

        assertEquals(0, service.getAllRestaurants().count)
        assertEquals(emptyList<RestaurantResponseDto>(), service.getAllRestaurants().result)
    }

    @Test
    fun `personalized list pins Sillichochi while preserving other order and preferences`() {
        val first = restaurant(1, "학생회관")
        val featured = restaurant(295, "[축제]실리꼬치밸리")
        val last = restaurant(2, "[축제]홍등반점")
        val featuredCustom = RestaurantCustom(user = mockk(), restaurant = featured, like = true, visible = false, orderIndex = 3)
        every { restaurantRepository.findAll() } returns listOf(first, featured, last)
        every { restaurantCustomRepository.findAllByUserId(1) } returns
            listOf(featuredCustom, RestaurantCustom(user = mockk(), restaurant = last, orderIndex = 1))

        val result = service.getAllPersonalizedRestaurants(1)

        assertEquals(3, result.count)
        assertEquals(listOf(295, 2, 1), result.result.map { it.id })
        assertEquals(true, result.result.first().liked)
        assertEquals(false, result.result.first().visible)
        assertEquals(3, featuredCustom.orderIndex)
    }

    private fun restaurant(
        id: Int,
        code: String,
    ) = Restaurant(id, code, code, null, null, null, null, null)
}
