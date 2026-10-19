package com.github.gezimos.inkos.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAppUiStateTest {
    private fun slot(
        id: Int,
        activityPackage: String = "example.app$id",
        label: String = "App $id",
        shortcutId: String? = null
    ): HomeAppUiState = HomeAppUiState(
        id = id,
        label = label,
        font = null,
        color = 0,
        activityPackage = activityPackage,
        shortcutId = shortcutId
    )

    @Test
    fun blankPackagesWithoutShortcutsAreUnassigned(): Unit {
        assertTrue(slot(0, activityPackage = "").isUnassignedSlot)
        assertTrue(slot(1, activityPackage = " \t\n").isUnassignedSlot)
    }

    @Test
    fun labelDoesNotDetermineWhetherSlotIsAssigned(): Unit {
        assertFalse(slot(0, label = "").isUnassignedSlot)
        assertFalse(slot(1, label = "选择应用").isUnassignedSlot)
        assertTrue(slot(2, activityPackage = "", label = "Old app name").isUnassignedSlot)
    }

    @Test
    fun shortcutIdentifiersKeepSlotsAssigned(): Unit {
        assertFalse(slot(0, activityPackage = "", shortcutId = "pinned").isUnassignedSlot)
        assertFalse(slot(1, activityPackage = "", shortcutId = "").isUnassignedSlot)
    }

    @Test
    fun normalModeHidesTwoUnassignedSlotsOutOfFifteen(): Unit {
        val slots = (0 until 15).map { id ->
            slot(id, activityPackage = if (id < 13) "example.app$id" else "")
        }

        val pages = homeAppPages(slots, appsPerPage = 15, totalPages = 1, isEditMode = false)

        assertEquals(1, pages.size)
        assertEquals((0 until 13).toList(), pages.single().map { it.id })
        assertSame(slots[12], pages.single().last())
        assertEquals(15, slots.size)
        assertTrue(slots[14].isUnassignedSlot)
    }

    @Test
    fun editModeKeepsEveryOriginalSlot(): Unit {
        val slots = (0 until 15).map { id ->
            slot(id, activityPackage = if (id < 13) "example.app$id" else "")
        }

        val pages = homeAppPages(slots, appsPerPage = 15, totalPages = 1, isEditMode = true)

        assertEquals(slots, pages.single())
        assertSame(slots[14], pages.single().last())
    }

    @Test
    fun pageBoundariesArePreservedBeforeFiltering(): Unit {
        val assigned = setOf(0, 2, 4, 8)
        val slots = (0 until 9).map { id ->
            slot(id, activityPackage = if (id in assigned) "example.app$id" else "")
        }

        val pages = homeAppPages(slots, appsPerPage = 3, totalPages = 3, isEditMode = false)

        assertEquals(listOf(listOf(0, 2), listOf(4), listOf(8)), pages.map { page -> page.map { it.id } })
    }

    @Test
    fun emptyMiddlePagesAreNotRemoved(): Unit {
        val slots = (0 until 6).map { id ->
            slot(id, activityPackage = if (id == 0 || id == 5) "example.app$id" else "")
        }

        val pages = homeAppPages(slots, appsPerPage = 2, totalPages = 3, isEditMode = false)

        assertEquals(listOf(listOf(0), emptyList(), listOf(5)), pages.map { page -> page.map { it.id } })
    }

    @Test
    fun configuredTrailingPagesStayEmptyInsteadOfRepeatingLastChunk(): Unit {
        val slots = listOf(slot(0), slot(1))

        val pages = homeAppPages(slots, appsPerPage = 2, totalPages = 4, isEditMode = false)

        assertEquals(listOf(2, 0, 0, 0), pages.map { it.size })
        assertTrue(pages[3].isEmpty())
    }

    @Test
    fun emptyHomeKeepsConfiguredPageCount(): Unit {
        val pages = homeAppPages(emptyList(), appsPerPage = 5, totalPages = 3, isEditMode = false)

        assertEquals(3, pages.size)
        assertTrue(pages.all { it.isEmpty() })
    }

    @Test
    fun allUnassignedSlotsOnlyAppearInEditMode(): Unit {
        val slots = (0 until 15).map { slot(it, activityPackage = "") }

        val normal = homeAppPages(slots, appsPerPage = 5, totalPages = 3, isEditMode = false)
        val editing = homeAppPages(slots, appsPerPage = 5, totalPages = 3, isEditMode = true)

        assertEquals(listOf(0, 0, 0), normal.map { it.size })
        assertEquals(listOf(5, 5, 5), editing.map { it.size })
    }

    @Test
    fun blankSeparatorsAndInternalEntriesRemainVisible(): Unit {
        val slots = listOf(
            slot(0, activityPackage = "com.inkos.separator.blank", label = ""),
            slot(1, activityPackage = "com.inkos.settings"),
            slot(2, activityPackage = "")
        )

        val page = homeAppPages(slots, appsPerPage = 3, totalPages = 1, isEditMode = false).single()

        assertEquals(listOf(0, 1), page.map { it.id })
    }

    @Test
    fun shortcutWithBlankPackageRemainsVisible(): Unit {
        val shortcut = slot(7, activityPackage = "", shortcutId = "pinned")

        val page = homeAppPages(listOf(shortcut), appsPerPage = 1, totalPages = 1, isEditMode = false).single()

        assertSame(shortcut, page.single())
    }

    @Test
    fun nonPositivePagingValuesDoNotThrow(): Unit {
        val pages = homeAppPages(listOf(slot(0)), appsPerPage = 0, totalPages = -1, isEditMode = false)

        assertEquals(listOf(listOf(0)), pages.map { page -> page.map { it.id } })
    }

    @Test
    fun selectionFollowsOriginalSlotIdAfterFiltering(): Unit {
        val visible = listOf(slot(1), slot(3), slot(4))

        assertEquals(1, homeAppSelectionIndex(visible, previousSlotId = 3, previousIndex = 3))
    }

    @Test
    fun selectionFollowsOriginalSlotIdWhenEditingRestoresEmptySlots(): Unit {
        val editing = listOf(slot(0, activityPackage = ""), slot(1), slot(2, activityPackage = ""), slot(3))

        assertEquals(3, homeAppSelectionIndex(editing, previousSlotId = 3, previousIndex = 1))
    }

    @Test
    fun removedSelectionFallsBackToNearestValidIndex(): Unit {
        val visible = listOf(slot(0), slot(2))

        assertEquals(1, homeAppSelectionIndex(visible, previousSlotId = 4, previousIndex = 4))
    }

    @Test
    fun emptyPagesHaveNoSelection(): Unit {
        assertEquals(-1, homeAppSelectionIndex(emptyList(), previousSlotId = 1, previousIndex = 0))
    }

    @Test
    fun enteringANonEmptyPageNormalizesNegativeSelection(): Unit {
        assertEquals(0, homeAppSelectionIndex(listOf(slot(3)), previousSlotId = null, previousIndex = -1))
    }

    @Test
    fun crossingToAShorterPageUsesItsActualLastItem(): Unit {
        assertEquals(1, homePageEdgeIndex(listOf(5, 2, 4), page = 1, fromEnd = true))
    }

    @Test
    fun crossingToANonEmptyPageStartsAtItsFirstItem(): Unit {
        assertEquals(0, homePageEdgeIndex(listOf(5, 2, 4), page = 2, fromEnd = false))
    }

    @Test
    fun crossingToAnEmptyPageHasNoSelectedItem(): Unit {
        assertEquals(-1, homePageEdgeIndex(listOf(5, 0, 4), page = 1, fromEnd = false))
        assertEquals(-1, homePageEdgeIndex(listOf(5, 0, 4), page = 1, fromEnd = true))
    }

    @Test
    fun invalidNavigationTargetsHaveNoSelectedItem(): Unit {
        assertEquals(-1, homePageEdgeIndex(listOf(3), page = -1, fromEnd = true))
        assertEquals(-1, homePageEdgeIndex(listOf(3), page = 1, fromEnd = false))
        assertEquals(-1, homePageEdgeIndex(listOf(-2), page = 0, fromEnd = true))
    }
}
