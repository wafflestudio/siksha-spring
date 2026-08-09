package siksha.wafflestudio.core.domain.main.meal.usecase

object MenuNameTokenizer {
    fun tokenize(name: String): List<String> =
        buildList {
            add(name)
            addAll(name.windowed(size = 2, step = 1))
            addAll(name.windowed(size = 3, step = 1))
        }.distinct()
}
