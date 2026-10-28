package com.example

import com.example.engine3d.Dust2Geometry
import com.example.logic.CS2GameEngine
import com.example.model.CS2SkinCatalog
import com.example.model.SkinRarity
import com.example.model.Team
import com.example.model.WeaponType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testGameEngineInitialization() {
        val engine = CS2GameEngine()
        assertEquals(100, engine.playerHealth)
        assertEquals(100, engine.playerArmor)
        assertTrue(engine.bots.isNotEmpty())
        assertEquals(30, engine.ammoInClip[WeaponType.AK47])
    }

    @Test
    fun testDust2WaypointsAndBombSites() {
        val waypoints = Dust2Geometry.WAYPOINTS
        assertTrue(waypoints.isNotEmpty())

        val bombsiteA = waypoints.find { it.isBombsiteA }
        val bombsiteB = waypoints.find { it.isBombsiteB }

        assertNotNull(bombsiteA)
        assertNotNull(bombsiteB)
        assertEquals("Bombsite A", bombsiteA?.name)
        assertEquals("Bombsite B", bombsiteB?.name)
    }

    @Test
    fun testCheatsConfig() {
        val engine = CS2GameEngine()
        engine.cheatSettings = engine.cheatSettings.copy(
            bhopEnabled = true,
            aimbotEnabled = true,
            infiniteAmmo = true,
            wallhackEsp = true,
            skinHackUnlocked = true
        )

        assertTrue(engine.cheatSettings.bhopEnabled)
        assertTrue(engine.cheatSettings.aimbotEnabled)
        assertTrue(engine.cheatSettings.infiniteAmmo)
        assertTrue(engine.cheatSettings.wallhackEsp)
        assertTrue(engine.cheatSettings.skinHackUnlocked)
    }

    @Test
    fun testSkinCatalogContainsKnivesAndRifles() {
        val knives = CS2SkinCatalog.KNIVES
        assertTrue(knives.isNotEmpty())
        assertTrue(knives.any { it.rarity == SkinRarity.EXTRAORDINARY })
        assertTrue(knives.any { it.name.contains("Karambit") })
        assertTrue(knives.any { it.name.contains("Butterfly") })

        val rifles = CS2SkinCatalog.RIFLES
        assertTrue(rifles.any { it.name.contains("AK-47") })

        val snipers = CS2SkinCatalog.SNIPERS
        assertTrue(rifles.isNotEmpty())
        assertTrue(snipers.any { it.name.contains("AWP") })
    }
}
