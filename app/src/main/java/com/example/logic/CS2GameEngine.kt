package com.example.logic

import androidx.compose.ui.graphics.Color
import com.example.audio.CS2SoundManager
import com.example.engine3d.BulletTracer
import com.example.engine3d.Dust2Geometry
import com.example.engine3d.Particle
import com.example.engine3d.Vec3
import com.example.model.BombState
import com.example.model.BotPlayer
import com.example.model.CS2SkinCatalog
import com.example.model.CheatSettings
import com.example.model.GameMode
import com.example.model.HudSettings
import com.example.model.KillFeedEntry
import com.example.model.Team
import com.example.model.WeaponSkin
import com.example.model.WeaponType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class CS2GameEngine {

    // Player State
    var playerPos = Vec3(0f, 0f, -95f)
    var playerVel = Vec3(0f, 0f, 0f)
    var playerYaw = 0f
    var playerPitch = 0f
    var playerHealth = 100
    var playerArmor = 100
    var playerTeam = Team.CT
    var playerKills = 0
    var playerDeaths = 0
    var playerMvps = 0
    var playerMoney = 16000

    var isCrouching = false
    var isJumping = false
    var isGrounded = true
    var isScoping = false
    var isInspecting = false
    var inspectProgress = 0f
    var recoilAnim = 0f
    var isReloading = false
    var reloadProgress = 0f
    private var lastShotTime = 0L

    // Active weapon and skins
    var activeWeaponType = WeaponType.AK47
    var equippedKnifeSkin: WeaponSkin = CS2SkinCatalog.KNIVES[0] // Karambit Fade
    var equippedAkSkin: WeaponSkin = CS2SkinCatalog.RIFLES[0] // AK47 Case Hardened
    var equippedAwpSkin: WeaponSkin = CS2SkinCatalog.SNIPERS[0] // AWP Dragon Lore
    var equippedM4Skin: WeaponSkin = CS2SkinCatalog.RIFLES[4] // M4A4 Howl
    var equippedDeagleSkin: WeaponSkin = CS2SkinCatalog.PISTOLS[0] // Deagle Blaze

    // Ammo state per weapon
    val ammoInClip = mutableMapOf(
        WeaponType.AK47 to 30,
        WeaponType.M4A4 to 30,
        WeaponType.AWP to 5,
        WeaponType.DEAGLE to 7,
        WeaponType.KNIFE to 1,
        WeaponType.HE_GRENADE to 1,
        WeaponType.C4 to 1
    )

    // Configs
    var cheatSettings = CheatSettings()
    var hudSettings = HudSettings()
    var gameMode = GameMode.COMPETITIVE_5V5

    // Match & Round State
    var ctScore = 0
    var tScore = 0
    var roundNumber = 1
    var roundTimerSeconds = 115
    var roundEndAnnouncement: String? = null

    // Bomb State
    var bombState = BombState.CARRIED
    var bombSite = "A"
    var bombPos = Dust2Geometry.BOMBSITE_A_POS
    var bombTimer = 40
    var plantProgress = 0f
    var defuseProgress = 0f

    // 5v5 Bots
    val bots = mutableListOf<BotPlayer>()

    // Visual Tracers & FX
    val tracers = mutableListOf<BulletTracer>()
    val particles = mutableListOf<Particle>()
    val killFeed = mutableListOf<KillFeedEntry>()

    private val _stateUpdateTick = MutableStateFlow(0L)
    val stateUpdateTick = _stateUpdateTick.asStateFlow()

    init {
        resetMatch()
    }

    fun resetMatch() {
        ctScore = 0
        tScore = 0
        roundNumber = 1
        playerKills = 0
        playerDeaths = 0
        playerMvps = 0
        playerMoney = 16000
        startNewRound()
    }

    fun startNewRound() {
        roundTimerSeconds = 115
        roundEndAnnouncement = null
        bombState = BombState.CARRIED
        bombTimer = 40
        plantProgress = 0f
        defuseProgress = 0f

        // Spawn player
        if (playerTeam == Team.CT) {
            playerPos = Vec3(5f, 0f, 45f)
            playerYaw = 180f
            activeWeaponType = WeaponType.M4A4
        } else {
            playerPos = Vec3(0f, 0f, -95f)
            playerYaw = 0f
            activeWeaponType = WeaponType.AK47
        }
        playerHealth = 100
        playerArmor = 100
        playerVel = Vec3(0f, 0f, 0f)
        isScoping = false
        isReloading = false

        // Refill ammo
        for (w in WeaponType.values()) {
            ammoInClip[w] = w.maxClip
        }

        // Spawn 5 CTs and 5 Ts
        initBots()
        notifyStateChanged()
    }

    private fun initBots() {
        bots.clear()
        val ctNames = listOf("s1mple", "ZywOo", "m0NESY", "b1t", "NiKo")
        val tNames = listOf("electronic", "ropz", "donk", "device", "sh1ro")

        var idCounter = 1

        // CT Team Bots
        val ctSpawns = listOf(
            Vec3(8f, 0f, 45f),
            Vec3(2f, 0f, 48f),
            Vec3(15f, 0f, 42f),
            Vec3(-2f, 0f, 46f),
            Vec3(10f, 0f, 50f)
        )
        for (i in 0 until 5) {
            val isPlayerSlot = (playerTeam == Team.CT && i == 0)
            if (!isPlayerSlot) {
                bots.add(
                    BotPlayer(
                        id = idCounter++,
                        name = ctNames[i],
                        team = Team.CT,
                        x = ctSpawns[i].x,
                        y = ctSpawns[i].y,
                        z = ctSpawns[i].z,
                        yaw = 180f,
                        weapon = if (i == 1) WeaponType.AWP else WeaponType.M4A4,
                        targetWaypointIndex = (i % 4)
                    )
                )
            }
        }

        // T Team Bots
        val tSpawns = listOf(
            Vec3(0f, 0f, -98f),
            Vec3(5f, 0f, -95f),
            Vec3(-5f, 0f, -95f),
            Vec3(8f, 0f, -92f),
            Vec3(-8f, 0f, -92f)
        )
        for (i in 0 until 5) {
            val isPlayerSlot = (playerTeam == Team.T && i == 0)
            if (!isPlayerSlot) {
                bots.add(
                    BotPlayer(
                        id = idCounter++,
                        name = tNames[i],
                        team = Team.T,
                        x = tSpawns[i].x,
                        y = tSpawns[i].y,
                        z = tSpawns[i].z,
                        yaw = 0f,
                        weapon = if (i == 2) WeaponType.AWP else WeaponType.AK47,
                        targetWaypointIndex = (i % 4)
                    )
                )
            }
        }
    }

    fun getActiveSkin(): WeaponSkin {
        if (cheatSettings.skinHackUnlocked) {
            // Unlock supreme skins instantly
            return when (activeWeaponType) {
                WeaponType.KNIFE -> equippedKnifeSkin
                WeaponType.AK47 -> equippedAkSkin
                WeaponType.AWP -> equippedAwpSkin
                WeaponType.M4A4 -> equippedM4Skin
                WeaponType.DEAGLE -> equippedDeagleSkin
                else -> CS2SkinCatalog.KNIVES[0]
            }
        }
        return when (activeWeaponType) {
            WeaponType.KNIFE -> equippedKnifeSkin
            WeaponType.AK47 -> equippedAkSkin
            WeaponType.AWP -> equippedAwpSkin
            WeaponType.M4A4 -> equippedM4Skin
            WeaponType.DEAGLE -> equippedDeagleSkin
            else -> CS2SkinCatalog.KNIVES[0]
        }
    }

    fun updateGame(deltaSec: Float) {
        val dt = deltaSec.coerceIn(0.001f, 0.05f)

        // 1. Player Physics & Movement
        if (playerHealth > 0) {
            updatePlayerPhysics(dt)
        }

        // 2. Aimbot Logic (Cheat)
        if (cheatSettings.aimbotEnabled && playerHealth > 0) {
            updateAimbot()
        }

        // 3. Recoil recovery and inspect animation
        if (recoilAnim > 0f) {
            recoilAnim = max(0f, recoilAnim - dt * 4.5f)
        }
        if (isInspecting) {
            inspectProgress += dt * 1.5f
            if (inspectProgress >= 1f) {
                isInspecting = false
                inspectProgress = 0f
            }
        }
        if (isReloading) {
            reloadProgress += dt * (1000f / activeWeaponType.reloadTimeMs)
            if (reloadProgress >= 1f) {
                finishReload()
            }
        }

        // 4. Update Bots AI
        updateBotsAI(dt)

        // 5. Clean expired tracers and particles
        val now = System.currentTimeMillis()
        tracers.removeAll { now - it.createdAtMs > 120L }
        particles.removeAll { now - it.createdAtMs > it.lifeMaxMs }
        for (p in particles) {
            p.pos = p.pos + (p.vel * dt)
        }

        // 6. Bomb timer and countdown
        if (bombState == BombState.PLANTED) {
            // 40 second C4 ticker
            if (now % 1000 < 60) {
                CS2SoundManager.playBombBeep()
            }
        }

        notifyStateChanged()
    }

    private fun updatePlayerPhysics(dt: Float) {
        val speedMultiplier = cheatSettings.speedHack

        // Apply friction
        playerVel = Vec3(playerVel.x * 0.82f, playerVel.y, playerVel.z * 0.82f)

        // NoClip cheat
        if (cheatSettings.noClip) {
            playerPos = playerPos + (playerVel * dt * speedMultiplier)
            return
        }

        // Normal gravity & jump
        if (!isGrounded) {
            playerVel = Vec3(playerVel.x, playerVel.y - 18f * dt, playerVel.z)
        }

        // Potential next position
        val nextX = playerPos.x + playerVel.x * dt * speedMultiplier
        val nextZ = playerPos.z + playerVel.z * dt * speedMultiplier
        val nextY = playerPos.y + playerVel.y * dt

        // Collision against Dust 2 walls
        if (!Dust2Geometry.isPointColliding(nextX, playerPos.z)) {
            playerPos = Vec3(nextX, playerPos.y, playerPos.z)
        }
        if (!Dust2Geometry.isPointColliding(playerPos.x, nextZ)) {
            playerPos = Vec3(playerPos.x, playerPos.y, nextZ)
        }

        // Ground floor check
        if (nextY <= 0f) {
            playerPos = Vec3(playerPos.x, 0f, playerPos.z)
            playerVel = Vec3(playerVel.x, 0f, playerVel.z)
            isGrounded = true

            // BHOP Cheat: automatic bunnyhop momentum chain!
            if (cheatSettings.bhopEnabled && isJumping) {
                val currentSpeed = sqrt(playerVel.x * playerVel.x + playerVel.z * playerVel.z)
                val boostedSpeed = (currentSpeed * 1.08f).coerceAtMost(32f)
                val yawRad = Math.toRadians(playerYaw.toDouble()).toFloat()
                playerVel = Vec3(-sin(yawRad) * boostedSpeed, 7.5f, cos(yawRad) * boostedSpeed)
                isGrounded = false
            }
        } else {
            playerPos = Vec3(playerPos.x, nextY, playerPos.z)
            isGrounded = false
        }
    }

    fun handleJoystickInput(dx: Float, dy: Float) {
        if (playerHealth <= 0) return
        val baseSpeed = if (isCrouching) 5f else 11f
        val yawRad = Math.toRadians(playerYaw.toDouble()).toFloat()

        val forwardX = -sin(yawRad)
        val forwardZ = cos(yawRad)
        val rightX = cos(yawRad)
        val rightZ = sin(yawRad)

        val moveX = (rightX * dx + forwardX * (-dy)) * baseSpeed
        val moveZ = (rightZ * dx + forwardZ * (-dy)) * baseSpeed

        playerVel = Vec3(moveX, playerVel.y, moveZ)
    }

    fun handleLookSwipe(deltaYaw: Float, deltaPitch: Float) {
        val sens = hudSettings.sensitivity
        playerYaw = (playerYaw + deltaYaw * sens * 0.18f) % 360f
        if (playerYaw < 0f) playerYaw += 360f
        playerPitch = (playerPitch - deltaPitch * sens * 0.18f).coerceIn(-80f, 80f)
    }

    fun jump() {
        if (playerHealth <= 0) return
        isJumping = true
        if (isGrounded) {
            playerVel = Vec3(playerVel.x, 7.2f, playerVel.z)
            isGrounded = false
        }
    }

    fun releaseJump() {
        isJumping = false
    }

    fun toggleCrouch() {
        isCrouching = !isCrouching
    }

    fun toggleScope() {
        if (activeWeaponType == WeaponType.AWP) {
            isScoping = !isScoping
        }
    }

    fun triggerInspect() {
        isInspecting = true
        inspectProgress = 0f
    }

    fun startReload() {
        if (isReloading || cheatSettings.infiniteAmmo) return
        isReloading = true
        reloadProgress = 0f
    }

    private fun finishReload() {
        isReloading = false
        reloadProgress = 0f
        ammoInClip[activeWeaponType] = activeWeaponType.maxClip
    }

    fun fireWeapon() {
        if (playerHealth <= 0 || isReloading) return
        val now = System.currentTimeMillis()
        if (now - lastShotTime < activeWeaponType.fireRateMs) return
        lastShotTime = now

        val clip = ammoInClip[activeWeaponType] ?: 0
        if (clip <= 0 && !cheatSettings.infiniteAmmo) {
            startReload()
            return
        }

        // Decrement ammo if not infinite ammo cheat
        if (!cheatSettings.infiniteAmmo) {
            ammoInClip[activeWeaponType] = clip - 1
        }

        // Sound effect
        when (activeWeaponType) {
            WeaponType.AK47 -> CS2SoundManager.playAk47()
            WeaponType.M4A4 -> CS2SoundManager.playM4a4()
            WeaponType.AWP -> CS2SoundManager.playAwp()
            WeaponType.DEAGLE -> CS2SoundManager.playDeagle()
            WeaponType.KNIFE -> CS2SoundManager.playKnifeSlash()
            else -> CS2SoundManager.playAk47()
        }

        // Visual recoil
        recoilAnim = 1.0f

        // Raycast hitscan
        performHitscan()
    }

    private fun performHitscan() {
        val yawRad = Math.toRadians(playerYaw.toDouble()).toFloat()
        val pitchRad = Math.toRadians(playerPitch.toDouble()).toFloat()

        // Direction vector from camera
        val dirX = -sin(yawRad) * cos(pitchRad)
        val dirY = sin(pitchRad)
        val dirZ = cos(yawRad) * cos(pitchRad)
        val rayDir = Vec3(dirX, dirY, dirZ)

        val eyePos = Vec3(playerPos.x, playerPos.y + if (isCrouching) 1.1f else 1.6f, playerPos.z)
        var hitBot: BotPlayer? = null
        var isHeadshot = false
        var closestDist = 200f

        for (bot in bots) {
            if (!bot.isAlive || bot.team == playerTeam) continue

            val toBot = Vec3(bot.x - eyePos.x, (bot.y + 1.2f) - eyePos.y, bot.z - eyePos.z)
            val dist = eyePos.distanceTo(Vec3(bot.x, bot.y, bot.z))

            // Dot product projection
            val dot = toBot.x * rayDir.x + toBot.y * rayDir.y + toBot.z * rayDir.z
            if (dot > 0 && dist < closestDist) {
                val projPoint = eyePos + (rayDir * dot)
                val perpDist = projPoint.distanceTo(Vec3(bot.x, bot.y + 1.2f, bot.z))

                // Hitbox radius: 0.65m for body, 0.35m for head
                if (perpDist < 0.7f) {
                    closestDist = dist
                    hitBot = bot
                    val headDist = projPoint.distanceTo(Vec3(bot.x, bot.y + 1.8f, bot.z))
                    isHeadshot = headDist < 0.38f
                }
            }
        }

        val bulletEnd = if (hitBot != null) {
            Vec3(hitBot.x, hitBot.y + 1.4f, hitBot.z)
        } else {
            eyePos + (rayDir * 80f)
        }

        tracers.add(BulletTracer(eyePos, bulletEnd))

        if (hitBot != null) {
            val baseDmg = activeWeaponType.damage
            val damage = if (isHeadshot) baseDmg * 4 else baseDmg
            hitBot.health -= damage

            // Blood / spark particles
            particles.add(Particle(bulletEnd, Vec3(0f, 1f, 0f), Color(0xFFFF2222)))

            if (isHeadshot) {
                CS2SoundManager.playHeadshotDink()
            }

            if (hitBot.health <= 0) {
                hitBot.health = 0
                hitBot.isAlive = false
                playerKills++
                playerMoney += 300

                killFeed.add(
                    KillFeedEntry(
                        killerName = "Всі (You)",
                        killerTeam = playerTeam,
                        victimName = hitBot.name,
                        victimTeam = hitBot.team,
                        weaponName = activeWeaponType.title,
                        isHeadshot = isHeadshot,
                        isNoscope = isScoping.not() && activeWeaponType == WeaponType.AWP
                    )
                )

                checkRoundConditions()
            }
        }
    }

    private fun updateAimbot() {
        var closestAngle = cheatSettings.aimbotFov
        var targetBot: BotPlayer? = null

        val eyePos = Vec3(playerPos.x, playerPos.y + 1.6f, playerPos.z)
        for (bot in bots) {
            if (!bot.isAlive || bot.team == playerTeam) continue
            val targetY = if (cheatSettings.aimbotTargetHead) bot.y + 1.8f else bot.y + 1.2f
            val dx = bot.x - eyePos.x
            val dy = targetY - eyePos.y
            val dz = bot.z - eyePos.z

            val targetYaw = (Math.toDegrees(atan2(-dx.toDouble(), dz.toDouble())).toFloat() + 360f) % 360f
            val dist2D = sqrt(dx * dx + dz * dz)
            val targetPitch = Math.toDegrees(atan2(dy.toDouble(), dist2D.toDouble())).toFloat()

            var diffYaw = targetYaw - playerYaw
            while (diffYaw > 180f) diffYaw -= 360f
            while (diffYaw < -180f) diffYaw += 360f
            val diffPitch = targetPitch - playerPitch

            val angularDistance = sqrt(diffYaw * diffYaw + diffPitch * diffPitch)
            if (angularDistance < closestAngle) {
                closestAngle = angularDistance
                targetBot = bot
            }
        }

        if (targetBot != null) {
            val targetY = if (cheatSettings.aimbotTargetHead) targetBot.y + 1.8f else targetBot.y + 1.2f
            val dx = targetBot.x - eyePos.x
            val dy = targetY - eyePos.y
            val dz = targetBot.z - eyePos.z
            val idealYaw = (Math.toDegrees(atan2(-dx.toDouble(), dz.toDouble())).toFloat() + 360f) % 360f
            val dist2D = sqrt(dx * dx + dz * dz)
            val idealPitch = Math.toDegrees(atan2(dy.toDouble(), dist2D.toDouble())).toFloat()

            // Smooth interpolation or instant lock
            val smooth = cheatSettings.aimbotSmoothness.coerceIn(0.05f, 1f)
            var diffYaw = idealYaw - playerYaw
            while (diffYaw > 180f) diffYaw -= 360f
            while (diffYaw < -180f) diffYaw += 360f

            playerYaw = (playerYaw + diffYaw * (1f - smooth * 0.85f)) % 360f
            playerPitch += (idealPitch - playerPitch) * (1f - smooth * 0.85f)

            // Triggerbot: auto fire if locked on
            if (cheatSettings.triggerbotAutoFire && closestAngle < 8f) {
                fireWeapon()
            }
        }
    }

    private fun updateBotsAI(dt: Float) {
        val now = System.currentTimeMillis()
        val waypoints = Dust2Geometry.WAYPOINTS

        for (bot in bots) {
            if (!bot.isAlive) continue

            // 1. Movement along tactical waypoints
            val currentTarget = waypoints[bot.targetWaypointIndex % waypoints.size]
            val dx = currentTarget.pos.x - bot.x
            val dz = currentTarget.pos.z - bot.z
            val dist = sqrt(dx * dx + dz * dz)

            if (dist < 3f) {
                // Pick next strategic neighbor waypoint
                bot.targetWaypointIndex = (bot.targetWaypointIndex + 1) % waypoints.size
            } else {
                val moveSpeed = 4.5f
                bot.x += (dx / dist) * moveSpeed * dt
                bot.z += (dz / dist) * moveSpeed * dt
                bot.yaw = (Math.toDegrees(atan2(-dx.toDouble(), dz.toDouble())).toFloat() + 360f) % 360f
            }

            // 2. Bot Combat: Check if bot sees player or enemy bot
            val isEnemyToPlayer = bot.team != playerTeam
            val distToPlayer = botBasePos(bot).distanceTo(playerPos)

            if (isEnemyToPlayer && playerHealth > 0 && distToPlayer < 45f) {
                if (now - bot.lastShotTimeMs > 600L) {
                    bot.lastShotTimeMs = now
                    // Bot fires at player
                    val hitChance = if (distToPlayer < 20f) 0.65f else 0.35f
                    if (Random.nextFloat() < hitChance) {
                        val dmg = (Random.nextInt(12, 28))
                        playerHealth = max(0, playerHealth - dmg)
                        CS2SoundManager.playAk47()
                        if (playerHealth <= 0) {
                            playerDeaths++
                            killFeed.add(
                                KillFeedEntry(
                                    killerName = bot.name,
                                    killerTeam = bot.team,
                                    victimName = "Всі (You)",
                                    victimTeam = playerTeam,
                                    weaponName = bot.weapon.title
                                )
                            )
                            checkRoundConditions()
                        }
                    }
                }
            }
        }
    }

    private fun botBasePos(bot: BotPlayer) = Vec3(bot.x, bot.y, bot.z)

    fun checkRoundConditions() {
        val aliveCTs = bots.count { it.team == Team.CT && it.isAlive } + (if (playerTeam == Team.CT && playerHealth > 0) 1 else 0)
        val aliveTs = bots.count { it.team == Team.T && it.isAlive } + (if (playerTeam == Team.T && playerHealth > 0) 1 else 0)

        if (aliveCTs <= 0) {
            tScore++
            roundEndAnnouncement = "Терористи перемогли! (Terrorists Win)"
            startNewRoundDelayed()
        } else if (aliveTs <= 0 && bombState != BombState.PLANTED) {
            ctScore++
            roundEndAnnouncement = "Спецназ переміг! (Counter-Terrorists Win)"
            startNewRoundDelayed()
        }
    }

    private fun startNewRoundDelayed() {
        notifyStateChanged()
    }

    fun plantBomb() {
        if (bombState == BombState.CARRIED && playerTeam == Team.T) {
            val distA = playerPos.distanceTo(Dust2Geometry.BOMBSITE_A_POS)
            val distB = playerPos.distanceTo(Dust2Geometry.BOMBSITE_B_POS)
            if (distA < 15f || distB < 15f) {
                bombState = BombState.PLANTED
                bombSite = if (distA < distB) "A" else "B"
                bombPos = if (distA < distB) Dust2Geometry.BOMBSITE_A_POS else Dust2Geometry.BOMBSITE_B_POS
                bombTimer = 40
                CS2SoundManager.playBombBeep()
                roundEndAnnouncement = "Бомбу встановлено на пленті $bombSite!"
                notifyStateChanged()
            }
        }
    }

    fun defuseBomb() {
        if (bombState == BombState.PLANTED && playerTeam == Team.CT) {
            val dist = playerPos.distanceTo(bombPos)
            if (dist < 6f) {
                bombState = BombState.DEFUSED
                ctScore++
                CS2SoundManager.playDefuseClick()
                roundEndAnnouncement = "Бомбу знешкоджено! Спецназ переміг!"
                notifyStateChanged()
            }
        }
    }

    fun buyWeapon(weapon: WeaponType) {
        if (playerMoney >= weapon.price) {
            playerMoney -= weapon.price
            activeWeaponType = weapon
            ammoInClip[weapon] = weapon.maxClip
            notifyStateChanged()
        }
    }

    fun selectWeaponSlot(type: WeaponType) {
        activeWeaponType = type
        isScoping = false
        isReloading = false
        notifyStateChanged()
    }

    private fun notifyStateChanged() {
        _stateUpdateTick.value = System.currentTimeMillis()
    }
}
