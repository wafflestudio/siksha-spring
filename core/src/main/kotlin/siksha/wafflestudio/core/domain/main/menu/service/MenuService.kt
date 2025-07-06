package siksha.wafflestudio.core.domain.main.menu.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import siksha.wafflestudio.core.domain.main.menu.dto.DateWithTypeInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuListResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.RestaurantInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.menu.repository.MenuLikeRepository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import siksha.wafflestudio.core.util.EtcUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MenuService(
    private val menuRepository: MenuRepository,
    private val restaurantRepository: RestaurantRepository,
    private val menuLikeRepository: MenuLikeRepository
) {
    fun getMenusWhereDate(
        startDate: LocalDate,
        endDate: LocalDate,
        exceptEmpty: Boolean,
        userId: Int,
    ): MenuListResponseDto {
        // userId가 0인 경우 비로그인 -> is_liked = false
        val targetUserId = userId.toString()
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
        val allRestaurants = restaurantRepository.findAll()

        // 날짜·타입별 기본 구조 초기화 (모든 식당, 빈 메뉴 리스트 포함)
        val dateGroupMap = mutableMapOf<LocalDate, MutableMap<String, MutableList<RestaurantInListDto>>>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val typeMap = mutableMapOf<String, MutableList<RestaurantInListDto>>()
            listOf("BR", "LU", "DN").forEach { type ->
                val list = mutableListOf<RestaurantInListDto>()
                allRestaurants.forEach { restaurant ->
                    list.add(
                        RestaurantInListDto(
                            createdAt = restaurant.createdAt,
                            updatedAt = restaurant.updatedAt,
                            id = restaurant.id,
                            code = restaurant.code,
                            nameKr = restaurant.nameKr,
                            nameEn = restaurant.nameEn,
                            addr = restaurant.addr,
                            lat = restaurant.lat,
                            lng = restaurant.lng,
                            etc = EtcUtils.convertRestEtc(restaurant.etc),
                            menus = mutableListOf()
                        )
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
            val menuDtos = summaries.map { menu ->
                val likeInfo = likeInfoMap[menu.getId()]
                MenuInListDto(
                    createdAt = menu.getCreatedAt(),
                    updatedAt = menu.getUpdatedAt(),
                    id = menu.getId(),
                    code = menu.getCode(),
                    nameKr = menu.getNameKr(),
                    nameEn = menu.getNameEn(),
                    price = menu.getPrice(),
                    etc = EtcUtils.convertMenuEtc(menu.getEtc()),
                    score = menu.getScore(),
                    reviewCnt = menu.getReviewCnt(),
                    likeCnt = likeInfo?.getLikeCnt() ?: 0,
                    isLiked = likeInfo?.getIsLiked() ?: false
                )
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
                    typedMap[type] = restaurantList
                        .filter { (it.menus as List<MenuInListDto>).isNotEmpty() }
                        .toMutableList()
                }
            }
        }

        // 최종 응답 DTO로 변환
        val resultList = dateGroupMap.entries
            .sortedBy { it.key }
            .map { (date, typedMap) ->
                DateWithTypeInListDto(
                    date = date,
                    BR = typedMap["BR"] ?: emptyList(),
                    LU = typedMap["LU"] ?: emptyList(),
                    DN = typedMap["DN"] ?: emptyList()
                )
            }

        return MenuListResponseDto(count = resultList.size, result = resultList)
    }

    fun getMenuById(menuId: Int, userId: Int): MenuDetailsDto {
        // userId가 0인 경우 비로그인 -> is_liked = false
        val targetUserId = userId.toString()
        val menuIdStr = menuId.toString()

        // 1) 메뉴 기본 정보 조회 (score, review_cnt 포함)
        val menu = try {
            menuRepository.findMenuById(menuIdStr)
        } catch (e: EmptyResultDataAccessException) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 메뉴를 찾을 수 없습니다."
            )
        }

        // 메뉴 좋아요 정보 조회 (like_cnt, is_liked 포함)
        val menuLikeInfo = menuRepository.findMenuLikeByMenuIdAndUserId(menuIdStr, targetUserId)

        return MenuDetailsDto(
            createdAt = menu.getCreatedAt(),
            updatedAt = menu.getUpdatedAt(),
            id = menu.getId(),
            restaurantId = menu.getRestaurantId(),
            code = menu.getCode(),
            date = menu.getDate(),
            type = menu.getType(),
            nameKr = menu.getNameKr(),
            nameEn = menu.getNameEn(),
            price = menu.getPrice(),
            etc = EtcUtils.convertMenuEtc(menu.getEtc()),
            score = menu.getScore(),
            reviewCnt = menu.getReviewCnt(),
            isLiked = menuLikeInfo.getIsLiked(),
            likeCnt = menuLikeInfo.getLikeCnt(),
        )
    }

    @Transactional
    fun likeMenu(menuId: Int, userId: Int): MenuDetailsDto {
        // 메뉴 좋아요 처리
        try {
            menuLikeRepository.postMenuLike(userId = userId, menuId = menuId)
        } catch (e: DataIntegrityViolationException) {
            // menu가 존재하지 않는 경우
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 메뉴를 찾을 수 없습니다."
            )
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "좋아요 처리 중에 오류가 발생했습니다."
            )
        }

        // 삽입 후 변경된 좋아요 수와 상태를 포함한 상세 정보 반환
        return getMenuById(menuId = menuId, userId = userId)
    }

    @Transactional
    fun unlikeMenu(menuId: Int, userId: Int): MenuDetailsDto {
        // subquery를 없애기 위해 menu 정보를 먼저 조회
        val menuIdStr = menuId.toString()
        val menu = try {
            menuRepository.findPlainMenuById(menuIdStr)
        } catch (e: EmptyResultDataAccessException) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 메뉴를 찾을 수 없습니다."
            )
        }

        // 메뉴 좋아요 취소 처리
        try {
            menuLikeRepository.deleteMenuLike(
                userId = userId,
                restaurantId = menu.getRestaurantId(),
                code = menu.getCode()
            )
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "좋아요 취소 처리 중에 오류가 발생했습니다."
            )
        }
        // like 없는 경우에 대해 별도 exception 처리 안함

        // 삭제 후 변경된 좋아요 수와 상태를 포함한 상세 정보 반환
        return getMenuById(menuId = menuId, userId = userId)
    }
}
