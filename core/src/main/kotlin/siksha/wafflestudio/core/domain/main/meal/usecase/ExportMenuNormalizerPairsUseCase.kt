package siksha.wafflestudio.core.domain.main.meal.usecase

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import siksha.wafflestudio.core.domain.main.meal.dto.MenuNormalizerPairsExportResponseDto
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAliasV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import java.io.File

@Component
class ExportMenuNormalizerPairsUseCase(
    private val menuAliasV2Repository: MenuAliasV2Repository,
    private val menuV2Repository: MenuV2Repository,
    @param:Value("\${siksha.menu-normalizer.pair-path:data/menu-normalizer-pairs.tsv}")
    private val pairPath: String,
) {
    @Transactional(readOnly = true)
    operator fun invoke(): MenuNormalizerPairsExportResponseDto {
        val aliasPairs =
            menuAliasV2Repository
                .findAllByOrderByIdAsc()
                .mapNotNull { alias ->
                    MenuNamePair(
                        originalName = alias.alias,
                        normalizedName = alias.menuName,
                    ).cleaned()
                }
        val menuNamePairs =
            menuV2Repository
                .findDistinctNames()
                .mapNotNull { name ->
                    MenuNamePair(
                        originalName = name,
                        normalizedName = name,
                    ).cleaned()
                }
        val pairs = (aliasPairs + menuNamePairs).toCollection(linkedSetOf()).toList()
        val outputFile = File(pairPath)

        writePairs(outputFile, pairs)

        return MenuNormalizerPairsExportResponseDto(
            path = outputFile.absolutePath,
            pairCount = pairs.size,
            aliasPairCount = aliasPairs.size,
            menuNamePairCount = menuNamePairs.size,
        )
    }

    private fun writePairs(
        outputFile: File,
        pairs: List<MenuNamePair>,
    ) {
        require(pairs.isNotEmpty()) {
            "No menu normalizer pairs were found in database."
        }

        outputFile.parentFile?.mkdirs()
        outputFile.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.appendLine("# original_name\tnormalized_name")
            pairs.forEach { pair ->
                writer
                    .append(pair.originalName)
                    .append('\t')
                    .append(pair.normalizedName)
                    .appendLine()
            }
        }
    }

    private fun MenuNamePair.cleaned(): MenuNamePair? {
        val originalName = originalName.cleanTsvValue()
        val normalizedName = normalizedName.cleanTsvValue()

        if (originalName.isBlank() || normalizedName.isBlank()) {
            return null
        }

        return MenuNamePair(originalName, normalizedName)
    }

    private fun String.cleanTsvValue(): String = replace(TSV_UNSAFE_CHARACTERS, " ").trim()

    private data class MenuNamePair(
        val originalName: String,
        val normalizedName: String,
    )

    private companion object {
        val TSV_UNSAFE_CHARACTERS = Regex("""[\t\r\n]+""")
    }
}
