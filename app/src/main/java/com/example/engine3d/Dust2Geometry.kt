package com.example.engine3d

import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Float) = Vec3(x * factor, y * factor, z * factor)
    fun distanceTo(other: Vec3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
    fun distance2D(other: Vec3): Float {
        val dx = x - other.x
        val dz = z - other.z
        return sqrt(dx * dx + dz * dz)
    }
}

enum class PolyType {
    GROUND,
    WALL,
    CRATE,
    CRATE_A,
    CRATE_B,
    DOOR,
    ARCH,
    RAILING
}

data class Poly3D(
    val p1: Vec3,
    val p2: Vec3,
    val p3: Vec3,
    val p4: Vec3,
    val color: Color,
    val type: PolyType = PolyType.WALL,
    val label: String? = null
)

data class BoxCollider(
    val minX: Float,
    val maxX: Float,
    val minZ: Float,
    val maxZ: Float,
    val minY: Float = -2f,
    val maxY: Float = 6f
) {
    fun contains(x: Float, z: Float): Boolean {
        return x in minX..maxX && z in minZ..maxZ
    }
}

data class Dust2Waypoint(
    val id: String,
    val pos: Vec3,
    val name: String,
    val isBombsiteA: Boolean = false,
    val isBombsiteB: Boolean = false,
    val neighbors: List<String> = emptyList()
)

object Dust2Geometry {

    val BOMBSITE_A_POS = Vec3(50f, 0.8f, 60f)
    val BOMBSITE_B_POS = Vec3(-60f, 0f, 45f)
    val T_SPAWN_POS = Vec3(0f, 0f, -100f)
    val CT_SPAWN_POS = Vec3(5f, 0f, 45f)

    val WAYPOINTS = listOf(
        Dust2Waypoint("t_spawn", Vec3(0f, 0f, -95f), "T Spawn", neighbors = listOf("suicide", "t_long_approach", "t_tunnels_outside")),
        Dust2Waypoint("t_long_approach", Vec3(25f, 0f, -70f), "Long Doors Outside", neighbors = listOf("t_spawn", "long_doors")),
        Dust2Waypoint("long_doors", Vec3(45f, 0f, -40f), "Long Doors", neighbors = listOf("t_long_approach", "long_pit", "long_a_straight")),
        Dust2Waypoint("long_pit", Vec3(65f, -1f, -25f), "Pit", neighbors = listOf("long_doors", "long_a_straight")),
        Dust2Waypoint("long_a_straight", Vec3(55f, 0f, 10f), "A Long", neighbors = listOf("long_doors", "long_pit", "a_ramp")),
        Dust2Waypoint("a_ramp", Vec3(52f, 0.4f, 40f), "A Ramp", neighbors = listOf("long_a_straight", "a_site")),
        Dust2Waypoint("a_site", BOMBSITE_A_POS, "Bombsite A", isBombsiteA = true, neighbors = listOf("a_ramp", "short_a", "ct_ramp_a")),
        Dust2Waypoint("short_a", Vec3(25f, 0.8f, 50f), "A Short / Catwalk", neighbors = listOf("a_site", "catwalk_mid")),
        Dust2Waypoint("catwalk_mid", Vec3(15f, 0.5f, 10f), "Catwalk", neighbors = listOf("short_a", "top_mid")),
        Dust2Waypoint("suicide", Vec3(0f, 0f, -60f), "Suicide", neighbors = listOf("t_spawn", "top_mid")),
        Dust2Waypoint("top_mid", Vec3(0f, 0f, -20f), "Top Mid / Xbox", neighbors = listOf("suicide", "catwalk_mid", "mid_doors", "lower_tunnels")),
        Dust2Waypoint("mid_doors", Vec3(2f, 0f, 15f), "Mid Doors", neighbors = listOf("top_mid", "ct_mid")),
        Dust2Waypoint("ct_mid", Vec3(3f, 0f, 35f), "CT Mid", neighbors = listOf("mid_doors", "ct_spawn", "b_doors_outside")),
        Dust2Waypoint("ct_spawn", CT_SPAWN_POS, "CT Spawn", neighbors = listOf("ct_mid", "ct_ramp_a", "b_ramp")),
        Dust2Waypoint("ct_ramp_a", Vec3(25f, 0.4f, 45f), "CT Ramp to A", neighbors = listOf("ct_spawn", "a_site")),
        Dust2Waypoint("t_tunnels_outside", Vec3(-35f, 0f, -80f), "T Outside Tunnels", neighbors = listOf("t_spawn", "upper_tunnels_enter")),
        Dust2Waypoint("upper_tunnels_enter", Vec3(-55f, 0f, -60f), "Upper Tunnels", neighbors = listOf("t_tunnels_outside", "upper_tunnels_mid", "lower_tunnels")),
        Dust2Waypoint("lower_tunnels", Vec3(-30f, -0.5f, -30f), "Lower Tunnels", neighbors = listOf("upper_tunnels_enter", "top_mid")),
        Dust2Waypoint("upper_tunnels_mid", Vec3(-65f, 0f, -15f), "Upper Tunnels Dark", neighbors = listOf("upper_tunnels_enter", "b_tunnels_exit")),
        Dust2Waypoint("b_tunnels_exit", Vec3(-65f, 0f, 20f), "B Tunnels Exit", neighbors = listOf("upper_tunnels_mid", "b_site")),
        Dust2Waypoint("b_doors_outside", Vec3(-25f, 0f, 30f), "B Doors Outside", neighbors = listOf("ct_mid", "b_window", "b_site")),
        Dust2Waypoint("b_window", Vec3(-40f, 0.8f, 35f), "B Window", neighbors = listOf("b_doors_outside", "b_site")),
        Dust2Waypoint("b_ramp", Vec3(-15f, 0f, 40f), "B Ramp CT", neighbors = listOf("ct_spawn", "b_doors_outside")),
        Dust2Waypoint("b_site", BOMBSITE_B_POS, "Bombsite B", isBombsiteB = true, neighbors = listOf("b_tunnels_exit", "b_doors_outside", "b_window"))
    )

    val polygons: List<Poly3D> by lazy { buildDust2Polygons() }
    val colliders: List<BoxCollider> by lazy { buildDust2Colliders() }

    private fun buildDust2Polygons(): List<Poly3D> {
        val list = mutableListOf<Poly3D>()

        // Sandy Dust 2 Colors
        val sandGround = Color(0xFFD8B578)
        val pitGround = Color(0xFFBFA065)
        val stoneGround = Color(0xFFC7A972)
        val wallDesertSun = Color(0xFFE8C88C)
        val wallDesertShadow = Color(0xFFC9A86C)
        val wallDarkTunnel = Color(0xFF6B5843)
        val woodCrate = Color(0xFF8B5A2B)
        val woodCrateHighlight = Color(0xFFA06C38)
        val archColor = Color(0xFFB5935A)
        val doorWood = Color(0xFF5C381E)

        // 1. Massive Ground Plazas
        // Main Dust 2 Floor
        list.add(Poly3D(Vec3(-120f, 0f, -130f), Vec3(100f, 0f, -130f), Vec3(100f, 0f, 90f), Vec3(-120f, 0f, 90f), sandGround, PolyType.GROUND))

        // Pit depression (Lower Y = -1.2)
        list.add(Poly3D(Vec3(55f, -1.2f, -35f), Vec3(80f, -1.2f, -35f), Vec3(80f, -1.2f, -10f), Vec3(55f, -1.2f, -10f), pitGround, PolyType.GROUND))

        // A Site Elevated Platform (Y = 0.8)
        list.add(Poly3D(Vec3(35f, 0.8f, 45f), Vec3(68f, 0.8f, 45f), Vec3(68f, 0.8f, 75f), Vec3(35f, 0.8f, 75f), stoneGround, PolyType.GROUND))
        // A Ramp slope connecting Long to A Site
        list.add(Poly3D(Vec3(45f, 0f, 30f), Vec3(65f, 0f, 30f), Vec3(65f, 0.8f, 45f), Vec3(45f, 0.8f, 45f), stoneGround, PolyType.GROUND))

        // Catwalk walkway (Y = 0.6)
        list.add(Poly3D(Vec3(10f, 0.6f, 0f), Vec3(25f, 0.6f, 0f), Vec3(25f, 0.8f, 50f), Vec3(10f, 0.8f, 50f), stoneGround, PolyType.GROUND))

        // 2. Dust 2 Outer Boundary Walls
        fun addWall(p1: Vec3, p2: Vec3, height: Float, col: Color, label: String? = null, type: PolyType = PolyType.WALL) {
            val p3 = Vec3(p2.x, p2.y + height, p2.z)
            val p4 = Vec3(p1.x, p1.y + height, p1.z)
            list.add(Poly3D(p1, p2, p3, p4, col, type, label))
        }

        // T Spawn Back Walls
        addWall(Vec3(-40f, 0f, -115f), Vec3(40f, 0f, -115f), 4.5f, wallDesertShadow)
        addWall(Vec3(-40f, 0f, -115f), Vec3(-40f, 0f, -80f), 4.5f, wallDesertSun)
        addWall(Vec3(40f, 0f, -115f), Vec3(40f, 0f, -60f), 4.5f, wallDesertSun)

        // Long Doors Corridor
        addWall(Vec3(35f, 0f, -50f), Vec3(55f, 0f, -50f), 4.5f, wallDesertShadow)
        addWall(Vec3(35f, 0f, -30f), Vec3(55f, 0f, -30f), 4.5f, wallDesertSun)
        // Double Wooden Doors (Left and Right door leaves)
        addWall(Vec3(42f, 0f, -30.2f), Vec3(46f, 0f, -30.2f), 3.2f, doorWood, "DOOR", PolyType.DOOR)
        addWall(Vec3(46f, 0f, -30.2f), Vec3(50f, 0f, -30.2f), 3.2f, doorWood, "DOOR", PolyType.DOOR)

        // A Long Right Wall & Pit Wall
        addWall(Vec3(82f, 0f, -35f), Vec3(82f, 0f, 35f), 5f, wallDesertShadow)
        addWall(Vec3(55f, -1.2f, -10f), Vec3(80f, -1.2f, -10f), 1.2f, wallDesertSun) // Pit step

        // A Long Left Corner (Long Corner where CTs peek!)
        addWall(Vec3(35f, 0f, -30f), Vec3(35f, 0f, 30f), 4.5f, wallDesertSun)
        addWall(Vec3(35f, 0f, 30f), Vec3(45f, 0f, 30f), 4.5f, wallDesertShadow)

        // A Site Back Walls (Goose corner & A back platform)
        addWall(Vec3(35f, 0.8f, 75f), Vec3(68f, 0.8f, 75f), 4f, wallDesertSun, "A")
        addWall(Vec3(68f, 0.8f, 35f), Vec3(68f, 0.8f, 75f), 4f, wallDesertShadow)

        // CT Spawn Back Wall & Archway
        addWall(Vec3(-20f, 0f, 75f), Vec3(30f, 0f, 75f), 4.5f, wallDesertShadow)
        addWall(Vec3(30f, 0f, 50f), Vec3(30f, 0f, 75f), 4.5f, wallDesertSun)

        // Middle Walls & Doors
        addWall(Vec3(-18f, 0f, -60f), Vec3(-18f, 0f, 10f), 5f, wallDesertSun) // Left mid wall
        addWall(Vec3(10f, 0f, -60f), Vec3(10f, 0f, -20f), 5f, wallDesertShadow) // Right suicide wall
        // Mid Doors (with iconic sniper crack!)
        addWall(Vec3(-10f, 0f, 15f), Vec3(-1f, 0f, 15f), 4f, doorWood, "MID_DOOR", PolyType.DOOR)
        addWall(Vec3(2f, 0f, 15f), Vec3(12f, 0f, 15f), 4f, doorWood, "MID_DOOR", PolyType.DOOR)

        // B Tunnels (Upper Dark & Lower Dark covered corridors)
        addWall(Vec3(-75f, 0f, -70f), Vec3(-75f, 0f, 25f), 4.5f, wallDarkTunnel)
        addWall(Vec3(-45f, 0f, -70f), Vec3(-45f, 0f, -20f), 4.5f, wallDarkTunnel)
        addWall(Vec3(-45f, 0f, -20f), Vec3(-45f, 0f, 25f), 4.5f, wallDarkTunnel)

        // B Site Walls & Platform
        addWall(Vec3(-85f, 0f, 25f), Vec3(-85f, 0f, 70f), 5f, wallDesertShadow)
        addWall(Vec3(-85f, 0f, 70f), Vec3(-35f, 0f, 70f), 5f, wallDesertSun, "B")
        addWall(Vec3(-35f, 0f, 50f), Vec3(-35f, 0f, 70f), 5f, wallDesertShadow)
        // B Doors & B Window
        addWall(Vec3(-35f, 0f, 25f), Vec3(-35f, 0f, 35f), 4f, wallDesertSun)
        addWall(Vec3(-35f, 0f, 42f), Vec3(-35f, 0f, 50f), 4f, wallDesertSun)
        // B Window opening
        addWall(Vec3(-45f, 1.2f, 25f), Vec3(-38f, 1.2f, 25f), 2.5f, wallDesertShadow)

        // 3. Iconic Crates & Props
        fun addCrate(cx: Float, cy: Float, cz: Float, w: Float, h: Float, d: Float, isSiteA: Boolean = false, isSiteB: Boolean = false) {
            val hw = w / 2f
            val hd = d / 2f
            val baseCol = if (isSiteA) Color(0xFFC75D38) else if (isSiteB) Color(0xFF3880C7) else woodCrate
            val type = if (isSiteA) PolyType.CRATE_A else if (isSiteB) PolyType.CRATE_B else PolyType.CRATE
            val label = if (isSiteA) "A" else if (isSiteB) "B" else null

            // Front
            addWall(Vec3(cx - hw, cy, cz - hd), Vec3(cx + hw, cy, cz - hd), h, baseCol, label, type)
            // Back
            addWall(Vec3(cx + hw, cy, cz + hd), Vec3(cx - hw, cy, cz + hd), h, baseCol, label, type)
            // Left
            addWall(Vec3(cx - hw, cy, cz + hd), Vec3(cx - hw, cy, cz - hd), h, woodCrateHighlight, label, type)
            // Right
            addWall(Vec3(cx + hw, cy, cz - hd), Vec3(cx + hw, cy, cz + hd), h, woodCrateHighlight, label, type)
            // Top lid
            list.add(Poly3D(
                Vec3(cx - hw, cy + h, cz - hd),
                Vec3(cx + hw, cy + h, cz - hd),
                Vec3(cx + hw, cy + h, cz + hd),
                Vec3(cx - hw, cy + h, cz + hd),
                woodCrateHighlight,
                type,
                label
            ))
        }

        // A Site Default Plant Triple Crates (with iconic Red 'A' mark!)
        addCrate(50f, 0.8f, 58f, 3.2f, 2.0f, 3.2f, isSiteA = true)
        addCrate(53f, 0.8f, 58f, 3.0f, 2.0f, 3.0f, isSiteA = true)
        addCrate(51.5f, 2.8f, 58f, 2.6f, 1.8f, 2.6f, isSiteA = true)

        // Goose crate at A back
        addCrate(62f, 0.8f, 70f, 2.5f, 1.8f, 2.5f)

        // Long A corner crate
        addCrate(68f, 0f, 15f, 2.4f, 1.8f, 2.4f)

        // Mid Xbox crate (allows jumping to Catwalk from Lower Mid!)
        addCrate(3f, 0f, -8f, 3.2f, 2.4f, 3.2f)

        // B Site Default Plant Double Crates (with iconic Blue 'B' mark!)
        addCrate(-60f, 0f, 48f, 3.5f, 2.2f, 3.5f, isSiteB = true)
        addCrate(-57f, 0f, 48f, 3.0f, 2.0f, 3.0f, isSiteB = true)
        addCrate(-58.5f, 2.2f, 48f, 2.6f, 1.8f, 2.6f, isSiteB = true)

        // B Car / Humvee at back of B Site
        addCrate(-76f, 0f, 62f, 4.0f, 1.6f, 5.0f)

        // Dust 2 Arches
        addWall(Vec3(-25f, 0f, -80f), Vec3(-25f, 0f, -75f), 3.5f, archColor, type = PolyType.ARCH)
        addWall(Vec3(25f, 0f, -40f), Vec3(25f, 0f, -35f), 3.5f, archColor, type = PolyType.ARCH)

        return list
    }

    private fun buildDust2Colliders(): List<BoxCollider> {
        val colliders = mutableListOf<BoxCollider>()

        // Bounds boundaries
        colliders.add(BoxCollider(-120f, -110f, -130f, 90f))
        colliders.add(BoxCollider(90f, 100f, -130f, 90f))
        colliders.add(BoxCollider(-120f, 100f, -130f, -120f))
        colliders.add(BoxCollider(-120f, 100f, 80f, 90f))

        // Long walls
        colliders.add(BoxCollider(34f, 36f, -30f, 30f))
        colliders.add(BoxCollider(81f, 83f, -35f, 35f))

        // Mid walls
        colliders.add(BoxCollider(-19f, -17f, -60f, 10f))
        colliders.add(BoxCollider(9f, 11f, -60f, -20f))

        // Tunnels walls
        colliders.add(BoxCollider(-76f, -74f, -70f, 25f))
        colliders.add(BoxCollider(-46f, -44f, -70f, 25f))

        // Crates colliders
        colliders.add(BoxCollider(48f, 55f, 56f, 60f)) // A Site crate
        colliders.add(BoxCollider(1.5f, 4.5f, -9.5f, -6.5f)) // Xbox
        colliders.add(BoxCollider(-62f, -55f, 46f, 50f)) // B Site crate
        colliders.add(BoxCollider(-78f, -74f, 59f, 65f)) // B Car

        return colliders
    }

    fun isPointColliding(x: Float, z: Float): Boolean {
        for (c in colliders) {
            if (c.contains(x, z)) return true
        }
        return false
    }
}
