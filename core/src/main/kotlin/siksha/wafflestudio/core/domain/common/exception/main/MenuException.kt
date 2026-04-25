package siksha.wafflestudio.core.domain.common.exception.main

import org.springframework.http.HttpStatus

class MenuNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다.")

class MenuLikeException : MainException(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 좋아요 처리 중에 오류가 발생했습니다.")

class MenuAlarmException : MainException(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 알림 처리 중에 오류가 발생했습니다.")

class MenuNotLikedException : MainException(HttpStatus.NOT_FOUND, "해당 메뉴에 좋아요를 누르지 않았습니다.")

class MenuAlarmAlreadyExistsException : MainException(HttpStatus.CONFLICT, "이미 알림을 설정하였습니다.")
