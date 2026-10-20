package com.github.gezimos.inkos.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationTest {
    @Test
    fun gestureDefaultsUseRequestedThresholds(): Unit {
        assertEquals(0.20f, Constants.DEFAULT_SHORT_SWIPE_RATIO, 0.0f)
        assertEquals(4.0f, Constants.DEFAULT_LONG_SWIPE_RATIO, 0.0f)
        assertTrue(Constants.DEFAULT_SHORT_SWIPE_RATIO in Constants.MIN_SHORT_SWIPE_RATIO..Constants.MAX_SHORT_SWIPE_RATIO)
        assertTrue(Constants.DEFAULT_LONG_SWIPE_RATIO in Constants.MIN_LONG_SWIPE_RATIO..Constants.MAX_LONG_SWIPE_RATIO)
        assertEquals(1_500L, Constants.DRAWER_ICON_PREWARM_DELAY_MS)
        assertEquals(2, Constants.DRAWER_ICON_PREWARM_PARALLELISM)
    }

    @Test
    fun gestureMigrationRunsWhenUpgradeCrossesTargetVersion(): Unit {
        assertTrue(shouldApplyGestureThresholdMigration(101013, 101014))
        assertTrue(shouldApplyGestureThresholdMigration(101012, 101015))
        assertTrue(shouldApplyGestureThresholdMigration(-1, 101014))
    }

    @Test
    fun gestureMigrationDoesNotRepeatAtOrAfterTargetVersion(): Unit {
        assertFalse(shouldApplyGestureThresholdMigration(101014, 101014))
        assertFalse(shouldApplyGestureThresholdMigration(101014, 101015))
        assertFalse(shouldApplyGestureThresholdMigration(101015, 101015))
    }

    @Test
    fun gestureMigrationDoesNotRunBeforeTargetVersionExists(): Unit {
        assertFalse(shouldApplyGestureThresholdMigration(101013, 101013))
        assertFalse(shouldApplyGestureThresholdMigration(-1, 101013))
    }
}
