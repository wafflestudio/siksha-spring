package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import siksha.wafflestudio.core.domain.common.exception.InvalidCustomException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2BuildingItemDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2RestaurantItemDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2UpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.CustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.CustomV2Service
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.util.Optional

class CustomV2ServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var buildingRepository: BuildingV2Repository
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var customRepository: CustomV2Repository
    private lateinit var service: CustomV2Service

    private val firstBuilding = BuildingV2(id = 1, number = "B1", name = "First", defaultOrder = 1)
    private val secondBuilding = BuildingV2(id = 2, number = "B2", name = "Second", defaultOrder = 2)
    private val firstRestaurant = RestaurantV2(id = 1, building = firstBuilding, name = "R1", defaultOrder = 1)
    private val secondRestaurant = RestaurantV2(id = 2, building = firstBuilding, name = "R2", defaultOrder = 2)
    private val thirdRestaurant = RestaurantV2(id = 3, building = secondBuilding, name = "R3", defaultOrder = 1)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        userRepository = mockk()
        buildingRepository = mockk()
        restaurantRepository = mockk()
        customRepository = mockk()
        service =
            CustomV2Service(
                userRepository,
                buildingRepository,
                restaurantRepository,
                customRepository,
            )
    }

    @Test
    fun `get customs returns default building and restaurant snapshot`() {
        every { buildingRepository.findAllForList() } returns listOf(firstBuilding, secondBuilding)
        every { restaurantRepository.findAllForList() } returns listOf(firstRestaurant, secondRestaurant, thirdRestaurant)
        every { customRepository.findByUserId(1) } returns null

        val result = service.getCustoms(1)

        assertEquals(listOf("B1", "B2"), result.customs.map { it.buildingNumber })
        assertEquals(listOf("R1", "R2"), result.customs[0].restaurants.map { it.name })
        assertEquals(true, result.customs[0].visible)
        assertEquals(true, result.customs[0].restaurants[0].visible)
    }

    @Test
    fun `update customs validates full snapshot and saves unified json`() {
        val savedCustom = slot<CustomV2>()
        every { userRepository.findById(1) } returns Optional.of(testUser())
        every { buildingRepository.findAllForList() } returns listOf(firstBuilding, secondBuilding)
        every { restaurantRepository.findAllForList() } returns listOf(firstRestaurant, secondRestaurant, thirdRestaurant)
        every { customRepository.findByUserId(1) } returns null
        every { customRepository.save(capture(savedCustom)) } answers { firstArg() }

        val result = service.updateCustoms(userId = 1, request = validRequest())

        verify { customRepository.save(any()) }
        assertEquals(listOf("B2", "B1"), result.customs.map { it.buildingNumber })
        assertEquals(listOf("R2", "R1"), result.customs[1].restaurants.map { it.name })
        assertTrue(savedCustom.captured.customs.contains("building_number"))
        assertTrue(savedCustom.captured.customs.contains("restaurants"))
    }

    @Test
    fun `update customs rejects missing restaurant custom`() {
        every { userRepository.findById(1) } returns Optional.of(testUser())
        every { buildingRepository.findAllForList() } returns listOf(firstBuilding, secondBuilding)
        every { restaurantRepository.findAllForList() } returns listOf(firstRestaurant, secondRestaurant, thirdRestaurant)

        val invalidRequest =
            validRequest().copy(
                customs =
                    listOf(
                        validRequest().customs[0].copy(restaurants = emptyList()),
                        validRequest().customs[1],
                    ),
            )

        assertThrows<InvalidCustomException> {
            service.updateCustoms(userId = 1, request = invalidRequest)
        }
    }

    private fun validRequest(): CustomV2UpdateRequestDto =
        CustomV2UpdateRequestDto(
            customs =
                listOf(
                    CustomV2BuildingItemDto(
                        buildingNumber = "B1",
                        order = 2,
                        visible = true,
                        restaurants =
                            listOf(
                                CustomV2RestaurantItemDto(name = "R1", order = 2, visible = true),
                                CustomV2RestaurantItemDto(name = "R2", order = 1, visible = false),
                            ),
                    ),
                    CustomV2BuildingItemDto(
                        buildingNumber = "B2",
                        order = 1,
                        visible = false,
                        restaurants =
                            listOf(
                                CustomV2RestaurantItemDto(name = "R3", order = 1, visible = true),
                            ),
                    ),
                ),
        )

    private fun testUser(): User =
        User(
            id = 1,
            type = "TEST",
            identity = "test-user",
            nickname = "tester",
        )
}
