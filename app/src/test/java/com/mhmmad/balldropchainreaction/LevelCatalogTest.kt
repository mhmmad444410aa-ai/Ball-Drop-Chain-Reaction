package com.mhmmad.balldropchainreaction

import org.junit.Assert.*
import org.junit.Test

class LevelCatalogTest {
    @Test fun exactlyTwentyUniqueLevels() {
        val levels=LevelCatalog.levels
        assertEquals(20,levels.size)
        assertEquals((1..20).toList(),levels.map { it.id })
        assertEquals(5,levels.map { it.world }.distinct().size)
        assertEquals(20,levels.map { it.name }.distinct().size)
    }
    @Test fun eachLevelHasUsableHintAndGeometry() {
        for (level in LevelCatalog.levels) {
            assertTrue(level.hint.kind in level.inventory)
            assertTrue(level.ramps.isNotEmpty())
            assertTrue(level.spawnX in 0f..16f)
            assertTrue(level.targetX in 0f..16f)
            assertTrue(level.targetY in .75f..8.5f)
            assertTrue(level.hint.x in 1f..15f)
            assertTrue(level.hint.y in 1f..8.3f)
        }
    }
}
