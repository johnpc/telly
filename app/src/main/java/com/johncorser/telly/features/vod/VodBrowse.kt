package com.johncorser.telly.features.vod

import com.johncorser.telly.features.vod.db.VodItemEntity
import com.johncorser.telly.features.vod.db.VodPositionEntity

/** One browser card: the item plus its stored progress ("Continue watching"). */
data class VodCard(
    val item: VodItemEntity,
    val progressPermille: Int?,
)

/**
 * Pure shaping for the Movies browser: categories are the items' preserved
 * group-titles in playlist order (blank/missing group -> [UNCATEGORIZED]),
 * cards are the selected category's items joined with stored positions.
 */
object VodBrowse {
    const val UNCATEGORIZED = "Uncategorized"
    private const val PERMILLE = 1000L

    fun categories(items: List<VodItemEntity>): List<String> = items.map { categoryOf(it) }.distinct()

    fun cards(
        items: List<VodItemEntity>,
        category: String?,
        positions: List<VodPositionEntity>,
    ): List<VodCard> {
        val byKey = positions.associateBy { it.itemKey }
        return items
            .filter { category == null || categoryOf(it) == category }
            .map { item -> VodCard(item, byKey[item.itemKey]?.let(::permille)) }
    }

    fun categoryOf(item: VodItemEntity): String = item.groupTitle?.takeIf { it.isNotBlank() } ?: UNCATEGORIZED

    private fun permille(position: VodPositionEntity): Int? =
        position.durationMs
            .takeIf { it > 0 }
            ?.let { duration -> (position.positionMs * PERMILLE / duration).toInt().coerceIn(0, PERMILLE.toInt()) }
}
