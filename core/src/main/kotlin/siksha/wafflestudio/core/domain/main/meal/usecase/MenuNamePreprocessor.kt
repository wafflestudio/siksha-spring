package siksha.wafflestudio.core.domain.main.meal.usecase

object MenuNamePreprocessor {
    fun preprocess(name: String): String {
        var normalized = name.trim()
        normalized = normalized.replace(OPERATING_PARENTHESIS_REGEX, " ")
        normalized = normalized.replace(DELIMITER_REGEX, " ")
        normalized = normalized.replace(OPERATING_TOKEN_REGEX, " ")
        normalized = normalized.replace(DESCRIPTIVE_TOKEN_REGEX, " ")
        normalized = normalized.replace(MULTIPLE_SPACES_REGEX, " ").trim()
        normalized = normalized.replace(SPACES_REGEX, "")

        return normalized.ifBlank { name.trim() }
    }

    private val OPERATING_PARENTHESIS_REGEX = Regex("""\((HOT|NEW|추천|특식|한정|셀프|추가|별도)[^)]*\)""")
    private val DELIMITER_REGEX = Regex("""[\/,|]+""")
    private val OPERATING_TOKEN_REGEX =
        Regex("""(?<![가-힣A-Za-z0-9])(HOT|NEW|추천|특식|한정|셀프|추가|별도)(?![가-힣A-Za-z0-9])""")
    private val DESCRIPTIVE_TOKEN_REGEX =
        Regex("""(원산지[^\s]*|알레르기[^\s]*|소스\s*별도|밥\s*/?\s*김치\s*포함|밥\s*포함|김치\s*포함)""")
    private val MULTIPLE_SPACES_REGEX = Regex("""\s+""")
    private val SPACES_REGEX = Regex("""\s+""")
}
