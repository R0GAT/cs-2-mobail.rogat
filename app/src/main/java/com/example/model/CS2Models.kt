package com.example.model

import androidx.compose.ui.graphics.Color

enum class Team(val displayName: String, val shortName: String, val color: Color) {
    CT("Спецназ (CT)", "CT", Color(0xFF4B7BEC)),
    T("Терористи (T)", "T", Color(0xFFF59E0B))
}

enum class WeaponType(
    val title: String,
    val slot: Int, // 1: Primary, 2: Secondary, 3: Knife, 4: Grenade, 5: C4
    val maxClip: Int,
    val maxReserve: Int,
    val damage: Int,
    val fireRateMs: Long,
    val reloadTimeMs: Long,
    val price: Int
) {
    AK47("AK-47", 1, 30, 90, 36, 110L, 2200L, 2700),
    M4A4("M4A4", 1, 30, 90, 33, 100L, 2100L, 3100),
    AWP("AWP", 1, 5, 30, 115, 950L, 3400L, 4750),
    DEAGLE("Desert Eagle", 2, 7, 35, 53, 260L, 1900L, 700),
    KNIFE("Ніж", 3, 1, 1, 65, 400L, 0L, 0),
    HE_GRENADE("HE Grenade", 4, 1, 1, 85, 800L, 0L, 300),
    C4("C4 Explosive", 5, 1, 1, 500, 1000L, 0L, 0)
}

enum class KnifeType(val title: String, val inspectStyle: String) {
    KARAMBIT("Karambit (Керамбіт)", "Spinning curved talon blade"),
    BUTTERFLY("Butterfly Knife (Ніж-метелик)", "Fluid two-handle flip flip"),
    M9_BAYONET("M9 Bayonet (M9 Штик-ніж)", "Heavy tactical serrated saw"),
    SKELETON("Skeleton Knife (Скелетний ніж)", "Lightweight hole-spin finger"),
    DEFAULT_CT("CT Tactical Knife", "Standard issue tactical blade"),
    DEFAULT_T("T Fold Knife", "Classic terrorist switchblade")
}

enum class SkinRarity(val title: String, val color: Color) {
    EXTRAORDINARY("★ Рідкісний спеціальний предмет", Color(0xFFFFD700)), // Gold / Knives
    COVERT("Таємне (Covert)", Color(0xFFEB4B4B)), // Red
    CLASSIFIED("Засекречене (Classified)", Color(0xFFD32CE6)), // Pink
    RESTRICTED("Заборонене (Restricted)", Color(0xFF8847FF)), // Purple
    MIL_SPEC("Армійська якість", Color(0xFF4B69FF)) // Blue
}

data class WeaponSkin(
    val id: String,
    val name: String,
    val weaponType: WeaponType,
    val knifeType: KnifeType? = null,
    val rarity: SkinRarity,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val wear: String = "Factory New (Прямо з заводу)",
    val isStatTrak: Boolean = false,
    val description: String = ""
)

/**
 * CS2 Cheat Settings requested by user:
 * - Bhop (бехоб)
 * - Aimbot (аім)
 * - Infinite Ammo & Grenades (бесконечні патрони)
 * - Wallhack / ESP (бачити через стіни)
 * - Skin Hack / Unlock All (скін хак)
 */
data class CheatSettings(
    val bhopEnabled: Boolean = false,
    val aimbotEnabled: Boolean = false,
    val aimbotFov: Float = 60f, // Degrees
    val aimbotSmoothness: Float = 0.25f, // 0 = instant, 1 = slow
    val aimbotTargetHead: Boolean = true,
    val triggerbotAutoFire: Boolean = false,
    val infiniteAmmo: Boolean = false,
    val infiniteGrenades: Boolean = false,
    val wallhackEsp: Boolean = false,
    val wallhackBoxes: Boolean = true,
    val wallhackHealth: Boolean = true,
    val wallhackSnaplines: Boolean = false,
    val wallhackChams: Boolean = true,
    val skinHackUnlocked: Boolean = false,
    val speedHack: Float = 1.0f,
    val noClip: Boolean = false,
    val wireframe: Boolean = false
)

data class HudSettings(
    val fov: Float = 85f,
    val sensitivity: Float = 1.0f,
    val joystickXPercent: Float = 16f,
    val joystickYPercent: Float = 72f,
    val joystickScale: Float = 1.0f,
    val fireButtonXPercent: Float = 85f,
    val fireButtonYPercent: Float = 68f,
    val fireButtonScale: Float = 1.15f,
    val jumpButtonXPercent: Float = 94f,
    val jumpButtonYPercent: Float = 48f,
    val crouchButtonXPercent: Float = 78f,
    val crouchButtonYPercent: Float = 86f,
    val reloadButtonXPercent: Float = 86f,
    val reloadButtonYPercent: Float = 28f,
    val scopeButtonXPercent: Float = 74f,
    val scopeButtonYPercent: Float = 42f,
    val crosshairColor: Color = Color(0xFF00FF66),
    val crosshairSize: Float = 12f,
    val crosshairGap: Float = 6f,
    val crosshairDot: Boolean = true,
    val vibrationEnabled: Boolean = true
)

data class BotPlayer(
    val id: Int,
    val name: String,
    val team: Team,
    var x: Float,
    var y: Float, // height
    var z: Float,
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var health: Int = 100,
    var armor: Int = 100,
    var weapon: WeaponType = WeaponType.AK47,
    var isAlive: Boolean = true,
    var isBot: Boolean = true,
    var isPlanting: Boolean = false,
    var isDefusing: Boolean = false,
    var targetWaypointIndex: Int = 0,
    var kills: Int = 0,
    var deaths: Int = 0,
    var mvps: Int = 0,
    var ping: Int = 18,
    var lastShotTimeMs: Long = 0L
)

data class KillFeedEntry(
    val id: Long = System.currentTimeMillis(),
    val killerName: String,
    val killerTeam: Team,
    val victimName: String,
    val victimTeam: Team,
    val weaponName: String,
    val isHeadshot: Boolean = false,
    val isWallbang: Boolean = false,
    val isNoscope: Boolean = false
)

enum class BombState {
    CARRIED,
    DROPPED,
    PLANTING,
    PLANTED,
    DEFUSING,
    DEFUSED,
    EXPLODED
}

enum class GameMode(val title: String, val subtitle: String) {
    COMPETITIVE_5V5("Змагальний 5v5 (Competitive)", "Матч до 9 раундів, 5 CT проти 5 T на Dust 2 з економікою та C4"),
    DEATHMATCH("Бій на смерть (Deathmatch)", "Кожен сам за себе з миттєвим респавном і необмеженими патронами"),
    BOT_PRACTICE("Тренування з ботами", "Налаштуйте рівень ботів і відточуйте стрільбу"),
    DUST2_EXPLORE("Вільне дослідження Dust II", "Вивчайте карту Dust 2 без обмежень з NoClip та чітами")
}

data class CS2Case(
    val id: String,
    val name: String,
    val price: Int,
    val badgeColor: Color,
    val items: List<WeaponSkin>
)
