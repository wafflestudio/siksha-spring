package siksha.wafflestudio.core.domain.main.menu.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import siksha.wafflestudio.core.domain.common.exception.MenuAlarmAlreadyExistsException
import siksha.wafflestudio.core.domain.common.exception.MenuAlarmException
import siksha.wafflestudio.core.domain.common.exception.MenuLikeException
import siksha.wafflestudio.core.domain.common.exception.MenuNotFoundException
import siksha.wafflestudio.core.domain.common.exception.MenuNotLikedException
import siksha.wafflestudio.core.domain.main.menu.dto.DateWithTypeInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuAlarmDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuListResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.MyMenuInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MyMenuListResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.MyRestaurantInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.RestaurantInListDto
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAlarmRepository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuLikeRepository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MenuService(
    private val menuRepository: MenuRepository,
    private val restaurantRepository: RestaurantRepository,
    private val menuLikeRepository: MenuLikeRepository,
    private val menuAlarmRepository: MenuAlarmRepository,
) {
    private val holidays: Set<LocalDate> = loadHolidays()

    private fun loadHolidays(): Set<LocalDate> {
        val resourcePath = "/2025.json"
        val stream: InputStream =
            this::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val mapper = jacksonObjectMapper()
        val raw: Map<String, List<String>> = mapper.readValue(stream)

        return raw.keys
            .map { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
            .toSet()
    }

    private fun isHoliday(date: LocalDate): Boolean {
        return holidays.contains(date)
    }

    private fun getDateType(date: LocalDate): String {
        return when {
            isHoliday(date) -> "HOLIDAY"
            date.dayOfWeek == DayOfWeek.SUNDAY -> "HOLIDAY"
            date.dayOfWeek == DayOfWeek.SATURDAY -> "SATURDAY"
            else -> "WEEKDAY"
        }
    }

    fun getMenusWhereDate(
        startDate: LocalDate,
        endDate: LocalDate,
        exceptEmpty: Boolean,
        userId: Int?,
    ): MenuListResponseDto {
        // userId가 null인 경우 비로그인 -> is_liked = false
        val targetUserId = userId?.toString() ?: "0"
        val startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // date 범위에 해당하는 menu 조회 (score, review_cnt 포함)
        val menuSummaries = menuRepository.findMenusByDate(startDateStr, endDateStr)
        if (menuSummaries.isEmpty()) {
            return MenuListResponseDto(count = 0, result = emptyList())
        }

        // date 범위에 해당하는 menu likes 조회 (like_cnt, is_liked 포함)
        // menu id를 기준으로 map 구성
        val menuLikeSummaries = menuRepository.findMenuLikesByDateAndUserId(targetUserId, startDateStr, endDateStr)
        val likeInfoMap = menuLikeSummaries.associateBy { it.getId() }

        // 모든 restaurant 정보 조회
        val allRestaurants = restaurantRepository.findAllByOrderByNameKr()

        // 날짜·타입별 기본 구조 초기화 (모든 식당, 빈 메뉴 리스트 포함)
        val dateGroupMap = mutableMapOf<LocalDate, MutableMap<String, MutableList<RestaurantInListDto>>>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val typeMap = mutableMapOf<String, MutableList<RestaurantInListDto>>()
            listOf("BR", "LU", "DN").forEach { type ->
                val list = mutableListOf<RestaurantInListDto>()
                allRestaurants.forEach { restaurant ->
                    list.add(
                        RestaurantInListDto.from(restaurant, mutableListOf()),
                    )
                }
                typeMap[type] = list
            }
            dateGroupMap[currentDate] = typeMap
            currentDate = currentDate.plusDays(1)
        }

        // 메뉴 데이터를 날짜·타입·식당별로 그룹핑해서 DTO 변환 후 추가
        val menusByKey = menuSummaries.groupBy { Triple(it.getDate(), it.getType(), it.getRestaurantId()) }
        menusByKey.forEach { (date, type, restaurantId), summaries ->
            val menuDtos =
                summaries.map { menu ->
                    val likeInfo = likeInfoMap[menu.getId()]
                    MenuInListDto.from(menu, likeInfo)
                }
            val restaurantList = dateGroupMap[date]?.get(type) ?: return@forEach
            restaurantList.find { it.id == restaurantId }?.let {
                (it.menus as MutableList<MenuInListDto>).addAll(menuDtos)
            }
        }

        // exceptEmpty=true 인 경우, 메뉴가 없는 식당은 리스트에서 제거
        if (exceptEmpty) {
            dateGroupMap.forEach { (_, typedMap) ->
                typedMap.forEach { (type, restaurantList) ->
                    typedMap[type] =
                        restaurantList
                            .filter { (it.menus as List<MenuInListDto>).isNotEmpty() }
                            .toMutableList()
                }
            }
        }

        // 최종 응답 DTO로 변환
        val resultList =
            dateGroupMap.entries
                .sortedBy { it.key }
                .map { (date, typedMap) ->
                    DateWithTypeInListDto(
                        date = date,
                        dateType = getDateType(date),
                        BR = typedMap["BR"] ?: emptyList(),
                        LU = typedMap["LU"] ?: emptyList(),
                        DN = typedMap["DN"] ?: emptyList(),
                    )
                }

        return MenuListResponseDto(count = resultList.size, result = resultList)
    }

    fun getMenuById(
        menuId: Int,
        userId: Int?,
    ): MenuDetailsDto {
        // userId가 0인 경우 비로그인 -> is_liked = false
        val targetUserId = userId?.toString() ?: "0"
        val menuIdStr = menuId.toString()

        // 1) 메뉴 기본 정보 조회 (score, review_cnt 포함)
        val menu =
            try {
                menuRepository.findMenuById(menuIdStr)
            } catch (e: EmptyResultDataAccessException) {
                throw MenuNotFoundException()
            }

        // 메뉴 좋아요 정보 조회 (like_cnt, is_liked 포함)
        val menuLikeInfo = menuRepository.findMenuLikeByMenuIdAndUserId(menuIdStr, targetUserId)

        return MenuDetailsDto.from(menu, menuLikeInfo)
    }

    @Transactional
    fun likeMenu(
        menuId: Int,
        userId: Int,
    ): MenuDetailsDto {
        // 메뉴 좋아요 처리
        try {
            menuLikeRepository.postMenuLike(userId = userId, menuId = menuId)
        } catch (e: DataIntegrityViolationException) {
            throw MenuNotFoundException()
        } catch (e: Exception) {
            throw MenuLikeException()
        }

        // 삽입 후 변경된 좋아요 수와 상태를 포함한 상세 정보 반환
        return getMenuById(menuId = menuId, userId = userId)
    }

    @Transactional
    fun unlikeMenu(
        menuId: Int,
        userId: Int,
    ): MenuDetailsDto {
        // subquery를 없애기 위해 menu 정보를 먼저 조회
        val menuIdStr = menuId.toString()
        val menu =
            try {
                menuRepository.findPlainMenuById(menuIdStr)
            } catch (e: EmptyResultDataAccessException) {
                throw MenuNotFoundException()
            }

        // 메뉴 좋아요 취소 처리
        try {
            menuLikeRepository.deleteMenuLike(
                userId = userId,
                restaurantId = menu.getRestaurantId(),
                code = menu.getCode(),
            )
        } catch (e: Exception) {
            throw MenuLikeException()
        }
        // like 없는 경우에 대해 별도 exception 처리 안함

        // 좋아요 해제될 때 알림 역시 해제
        menuAlarmRepository.deleteMenuAlarm(userId, menu.getRestaurantId(), menu.getCode())

        // 삭제 후 변경된 좋아요 수와 상태를 포함한 상세 정보 반환
        return getMenuById(menuId = menuId, userId = userId)
    }

    fun getMyMenus(userId: Int): MyMenuListResponseDto {
        val userIdStr = userId.toString()
        val menuIds = menuRepository.findMyMenuByUserId(userIdStr)
        if (menuIds.isEmpty()) {
            return MyMenuListResponseDto(
                count = 0,
                result = emptyList(),
            )
        }

        val menuSummaries = menuRepository.findMenusByMenuIds(menuIds)
        val menuLikeSummaries = menuRepository.findMenuLikesByMenuIds(menuIds)
        val likeInfoMap = menuLikeSummaries.associateBy { it.getId() }
        val alarmsInfoList = menuAlarmRepository.findMenuLikesByMenuIds(userId, menuIds)

        val allRestaurants = restaurantRepository.findAllByOrderByNameKr()

        val list = mutableListOf<MyRestaurantInListDto>()
        allRestaurants.forEach { restaurant ->
            list.add(
                MyRestaurantInListDto.from(restaurant, mutableListOf()),
            )
        }

        val menusByKey = menuSummaries.groupBy { it.getRestaurantId() }
        menusByKey.forEach { (restaurantId, summaries) ->
            val myMenuDtos =
                summaries.map { menu ->
                    val likeInfo = likeInfoMap[menu.getId()]
                    val alarmsInfo = alarmsInfoList.contains(menu.getId())
                    MyMenuInListDto.from(menu, likeInfo, alarmsInfo)
                }

            list.find { it.id == restaurantId }?.let {
                (it.menus as MutableList<MyMenuInListDto>).addAll(myMenuDtos)
            }
        }

        val result = list.filter { it.menus.isNotEmpty() }.toMutableList()

        return MyMenuListResponseDto(
            count = result.size,
            result = result,
        )
    }

    @Transactional
    fun menuAlarmOn(
        menuId: Int,
        userId: Int,
    ): MenuAlarmDto {
        val menuIdStr = menuId.toString()
        val userIdStr = userId.toString()
        val menu =
            try {
                menuRepository.findMenuById(menuIdStr)
            } catch (e: EmptyResultDataAccessException) {
                throw MenuNotFoundException()
            }

        val menuLike = menuRepository.findMenuLikeByMenuIdAndUserId(menuIdStr, userIdStr)
        if (menuLike.getIsLiked() == 0) throw MenuNotLikedException()

        val menuAlarms = menuAlarmRepository.findMenuAlarm(userId, menu.getRestaurantId(), menu.getCode())
        if (menuAlarms.isNotEmpty()) throw MenuAlarmAlreadyExistsException()

        menuAlarmRepository.postMenuAlarm(userId, menuId)

        return MenuAlarmDto.from(menu, menuLike.getIsLiked(), true)
    }

    @Transactional
    fun menuAlarmOff(
        menuId: Int,
        userId: Int,
    ): MenuAlarmDto {
        val menuIdStr = menuId.toString()
        val userIdStr = userId.toString()
        val menu =
            try {
                menuRepository.findMenuById(menuIdStr)
            } catch (e: EmptyResultDataAccessException) {
                throw MenuNotFoundException()
            }

        val menuLike = menuRepository.findMenuLikeByMenuIdAndUserId(menuIdStr, userIdStr)
        if (menuLike.getIsLiked() == 0) throw MenuNotLikedException()

        try {
            menuAlarmRepository.deleteMenuAlarm(
                userId = userId,
                restaurantId = menu.getRestaurantId(),
                code = menu.getCode(),
            )
        } catch (e: Exception) {
            throw MenuAlarmException()
        }

        return MenuAlarmDto.from(menu, menuLike.getIsLiked(), false)
    }

    @Transactional
    fun menuAlarmOffAll(userId: Int) {
        try {
            menuAlarmRepository.deleteMenuAlarmByUserId(userId)
        } catch (e: Exception) {
            throw MenuAlarmException()
        }
    }

    @Transactional
    fun menuAlarmOnAll(userId: Int) {
        val userIdStr = userId.toString()
        val myMenuIds = menuRepository.findMyMenuByUserId(userIdStr)
        val alarmMenuIds = menuAlarmRepository.findAllByUserId(userId).map { it.id }
        val targetIds = myMenuIds.filterNot { it in alarmMenuIds }

        try {
            targetIds.forEach { targetId ->
                menuAlarmRepository.postMenuAlarm(userId, targetId)
            }
        } catch (e: Exception) {
            throw MenuAlarmException()
        }
    }
}
