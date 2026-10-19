package com.github.gezimos.inkos.data

import android.graphics.Typeface
import android.os.UserHandle
import androidx.annotation.ColorInt
import com.github.gezimos.inkos.services.NotificationManager
data class HomeAppUiState(
    val id: Int,
    val label: String,
    val font: Typeface?,
    @ColorInt val color: Int,
    val notificationInfo: NotificationManager.NotificationInfo? = null,
    val activityPackage: String,
    val activityClass: String = "",
    val user: UserHandle? = null,
    val shortcutId: String? = null
)

internal val HomeAppUiState.isUnassignedSlot: Boolean
    get() = activityPackage.isBlank() && shortcutId == null

internal fun homeAppPages(
    slots: List<HomeAppUiState>,
    appsPerPage: Int,
    totalPages: Int,
    isEditMode: Boolean
): List<List<HomeAppUiState>> {
    val chunks = slots.chunked(appsPerPage.coerceAtLeast(1))
    return List(totalPages.coerceAtLeast(1)) { page ->
        val pageSlots = chunks.getOrNull(page).orEmpty()
        if (isEditMode) pageSlots else pageSlots.filterNot { it.isUnassignedSlot }
    }
}

internal fun homeAppSelectionIndex(
    apps: List<HomeAppUiState>,
    previousSlotId: Int?,
    previousIndex: Int
): Int {
    if (apps.isEmpty()) return -1
    val preservedIndex = apps.indexOfFirst { it.id == previousSlotId }
    return if (preservedIndex >= 0) preservedIndex else previousIndex.coerceIn(0, apps.lastIndex)
}

internal fun homePageEdgeIndex(
    appCounts: List<Int>,
    page: Int,
    fromEnd: Boolean
): Int {
    val count = appCounts.getOrNull(page)?.coerceAtLeast(0) ?: 0
    return when {
        count == 0 -> -1
        fromEnd -> count - 1
        else -> 0
    }
}

