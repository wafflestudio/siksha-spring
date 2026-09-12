package siksha.wafflestudio.core.domain.v1.main.restaurant.service

import siksha.wafflestudio.core.domain.v1.main.restaurant.data.Restaurant

// Festival easter egg: give Sillichochi Valley the first seat, preserving everyone else's order.
internal fun List<Restaurant>.withSillichochiFirst(): List<Restaurant> {
    val (sillichochi, others) = partition { it.code == "[축제]실리꼬치밸리" }
    return sillichochi + others
}
