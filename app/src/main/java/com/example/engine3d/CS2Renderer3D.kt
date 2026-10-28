package com.example.engine3d

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.BotPlayer
import com.example.model.CheatSettings
import com.example.model.HudSettings
import com.example.model.Team
import com.example.model.WeaponSkin
import com.example.model.WeaponType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class ProjectedPoint(val sx: Float, val sy: Float, val z: Float, val visible: Boolean)

data class BulletTracer(
    val start: Vec3,
    val end: Vec3,
    val createdAtMs: Long = System.currentTimeMillis()
)

data class Particle(
    var pos: Vec3,
    var vel: Vec3,
    val color: Color,
    val lifeMaxMs: Long = 400L,
    val createdAtMs: Long = System.currentTimeMillis()
)

class CS2Renderer3D {

    private val textPaint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    fun renderScene(
        drawScope: DrawScope,
        camPos: Vec3,
        camYaw: Float,
        camPitch: Float,
        hudSettings: HudSettings,
        cheatSettings: CheatSettings,
        bots: List<BotPlayer>,
        playerTeam: Team,
        activeSkin: WeaponSkin,
        recoilAnim: Float,
        inspectAnim: Float,
        isReloading: Boolean,
        reloadProgress: Float,
        isScoping: Boolean,
        tracers: List<BulletTracer>,
        particles: List<Particle>
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        if (width <= 0 || height <= 0) return

        val fovRad = Math.toRadians(hudSettings.fov.toDouble()).toFloat()
        val f = (width / 2f) / tan(fovRad / 2f)

        val yawRad = Math.toRadians(camYaw.toDouble()).toFloat()
        val pitchRad = Math.toRadians(camPitch.toDouble()).toFloat()

        val cosYaw = cos(yawRad)
        val sinYaw = sin(yawRad)
        val cosPitch = cos(pitchRad)
        val sinPitch = sin(pitchRad)

        // 1. Draw Sky & Horizon
        val horizonY = (height / 2f) + (tan(pitchRad.toDouble()).toFloat() * f)
        // Sky gradient
        drawScope.drawRect(
            color = Color(0xFF6B9FE8),
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(width, horizonY.coerceAtLeast(0f))
        )
        // Horizon warm haze
        if (horizonY > 0) {
            drawScope.drawRect(
                color = Color(0xFFE2C99A),
                topLeft = Offset(0f, (horizonY - 80f).coerceAtLeast(0f)),
                size = androidx.compose.ui.geometry.Size(width, 80f)
            )
        }
        // Distant ground plane below horizon
        drawScope.drawRect(
            color = Color(0xFFC4A670),
            topLeft = Offset(0f, horizonY.coerceAtLeast(0f)),
            size = androidx.compose.ui.geometry.Size(width, height - horizonY.coerceAtLeast(0f))
        )

        fun project(world: Vec3): ProjectedPoint {
            // Translate to camera origin
            val dx = world.x - camPos.x
            val dy = world.y - camPos.y
            val dz = world.z - camPos.z

            // Rotate around Y axis (Yaw)
            val rx = dx * cosYaw - dz * sinYaw
            val rz = dx * sinYaw + dz * cosYaw

            // Rotate around X axis (Pitch)
            val ry = dy * cosPitch - rz * sinPitch
            val fz = dy * sinPitch + rz * cosPitch

            if (fz <= 0.35f) {
                return ProjectedPoint(0f, 0f, fz, false)
            }

            val sx = (rx * f / fz) + (width / 2f)
            val sy = (-ry * f / fz) + (height / 2f)
            return ProjectedPoint(sx, sy, fz, true)
        }

        // 2. Sort & Project Dust 2 Geometry
        data class RenderItem(val zAvg: Float, val drawAction: () -> Unit)
        val renderList = mutableListOf<RenderItem>()

        for (poly in Dust2Geometry.polygons) {
            val p1 = project(poly.p1)
            val p2 = project(poly.p2)
            val p3 = project(poly.p3)
            val p4 = project(poly.p4)

            // If at least 3 points are in front of camera
            val visibleCount = (if (p1.visible) 1 else 0) + (if (p2.visible) 1 else 0) +
                    (if (p3.visible) 1 else 0) + (if (p4.visible) 1 else 0)

            if (visibleCount >= 3) {
                val zAvg = (p1.z + p2.z + p3.z + p4.z) / 4f
                renderList.add(RenderItem(zAvg) {
                    val path = Path().apply {
                        moveTo(p1.sx, p1.sy)
                        lineTo(p2.sx, p2.sy)
                        lineTo(p3.sx, p3.sy)
                        lineTo(p4.sx, p4.sy)
                        close()
                    }

                    if (cheatSettings.wireframe) {
                        drawScope.drawPath(path, Color(0xFF00FF66), style = Stroke(width = 1.5f))
                    } else {
                        // Ambient shading based on depth
                        val depthFactor = (1f - (zAvg / 140f)).coerceIn(0.45f, 1f)
                        val shadedColor = Color(
                            red = poly.color.red * depthFactor,
                            green = poly.color.green * depthFactor,
                            blue = poly.color.blue * depthFactor,
                            alpha = poly.color.alpha
                        )
                        drawScope.drawPath(path, shadedColor, style = Fill)

                        // Subtle edge borders on crates and doors
                        if (poly.type == PolyType.CRATE || poly.type == PolyType.CRATE_A || poly.type == PolyType.CRATE_B || poly.type == PolyType.DOOR) {
                            drawScope.drawPath(path, Color(0x33000000), style = Stroke(width = 1.2f))
                        }

                        // Label A or B graffiti
                        if (poly.label != null && zAvg in 1f..65f) {
                            val centerX = (p1.sx + p2.sx + p3.sx + p4.sx) / 4f
                            val centerY = (p1.sy + p2.sy + p3.sy + p4.sy) / 4f
                            val labelColor = if (poly.label == "A") Color(0xFFFF2222) else Color(0xFF2288FF)
                            val fontSize = (48f * f / (zAvg * 30f)).coerceIn(12f, 72f)

                            drawScope.drawContext.canvas.nativeCanvas.apply {
                                textPaint.color = android.graphics.Color.argb(
                                    240,
                                    (labelColor.red * 255).toInt(),
                                    (labelColor.green * 255).toInt(),
                                    (labelColor.blue * 255).toInt()
                                )
                                textPaint.textSize = fontSize
                                drawText(poly.label, centerX - fontSize / 3f, centerY + fontSize / 3f, textPaint)
                            }
                        }
                    }
                })
            }
        }

        // 3. Project Bot Characters
        for (bot in bots) {
            if (!bot.isAlive) continue
            val botBase = Vec3(bot.x, bot.y, bot.z)
            val botHead = Vec3(bot.x, bot.y + 1.8f, bot.z)
            val baseP = project(botBase)
            val headP = project(botHead)

            if (baseP.visible && headP.visible) {
                val zDist = (baseP.z + headP.z) / 2f
                val botHeight = (baseP.sy - headP.sy).coerceAtLeast(10f)
                val botWidth = botHeight * 0.45f
                val centerX = (baseP.sx + headP.sx) / 2f

                renderList.add(RenderItem(zDist) {
                    val isEnemy = bot.team != playerTeam
                    val teamColor = if (bot.team == Team.CT) Color(0xFF3867D6) else Color(0xFFD35400)

                    // Draw Bot 3D representation
                    val topY = headP.sy
                    val bottomY = baseP.sy

                    // Head
                    val headRadius = botWidth * 0.28f
                    drawScope.drawCircle(
                        color = if (bot.team == Team.CT) Color(0xFF2C3E50) else Color(0xFF795548),
                        radius = headRadius,
                        center = Offset(centerX, topY + headRadius)
                    )
                    // Visor / Mask
                    drawScope.drawRect(
                        color = if (bot.team == Team.CT) Color(0xFF00E5FF) else Color(0xFF1E272C),
                        topLeft = Offset(centerX - headRadius * 0.7f, topY + headRadius * 0.6f),
                        size = androidx.compose.ui.geometry.Size(headRadius * 1.4f, headRadius * 0.5f)
                    )

                    // Torso (tactical vest)
                    val torsoTop = topY + headRadius * 2f
                    val torsoHeight = botHeight * 0.45f
                    drawScope.drawRect(
                        color = teamColor,
                        topLeft = Offset(centerX - botWidth * 0.4f, torsoTop),
                        size = androidx.compose.ui.geometry.Size(botWidth * 0.8f, torsoHeight)
                    )

                    // Weapon in bot's hands
                    drawScope.drawRect(
                        color = Color(0xFF111111),
                        topLeft = Offset(centerX - botWidth * 0.1f, torsoTop + torsoHeight * 0.4f),
                        size = androidx.compose.ui.geometry.Size(botWidth * 0.7f, botHeight * 0.08f)
                    )

                    // Legs
                    val legTop = torsoTop + torsoHeight
                    val legWidth = botWidth * 0.28f
                    val legHeight = bottomY - legTop
                    drawScope.drawRect(
                        color = Color(0xFF1E272C),
                        topLeft = Offset(centerX - botWidth * 0.35f, legTop),
                        size = androidx.compose.ui.geometry.Size(legWidth, legHeight)
                    )
                    drawScope.drawRect(
                        color = Color(0xFF1E272C),
                        topLeft = Offset(centerX + botWidth * 0.07f, legTop),
                        size = androidx.compose.ui.geometry.Size(legWidth, legHeight)
                    )

                    // Floating Overhead Health Bar & Name (Standard CS2 spectator / teammate tags)
                    if (zDist < 80f) {
                        val barW = (botWidth * 1.1f).coerceIn(24f, 70f)
                        val barH = 5f
                        val barX = centerX - barW / 2f
                        val barY = topY - 14f

                        // Background
                        drawScope.drawRect(Color(0xCC000000), Offset(barX - 1f, barY - 1f), androidx.compose.ui.geometry.Size(barW + 2f, barH + 2f))
                        // Health fill
                        val healthPct = (bot.health / 100f).coerceIn(0f, 1f)
                        val hpColor = if (healthPct > 0.5f) Color(0xFF00FF66) else if (healthPct > 0.25f) Color(0xFFFFCC00) else Color(0xFFFF2222)
                        drawScope.drawRect(hpColor, Offset(barX, barY), androidx.compose.ui.geometry.Size(barW * healthPct, barH))

                        // Bot name & weapon
                        val distMeters = (zDist / 2f).toInt()
                        drawScope.drawContext.canvas.nativeCanvas.apply {
                            textPaint.color = if (isEnemy) android.graphics.Color.WHITE else android.graphics.Color.CYAN
                            textPaint.textSize = (botWidth * 0.28f).coerceIn(10f, 16f)
                            drawText("${bot.name} [${distMeters}m]", barX, barY - 3f, textPaint)
                        }
                    }
                })
            }
        }

        // Draw in back-to-front order
        renderList.sortByDescending { it.zAvg }
        for (item in renderList) {
            item.drawAction()
        }

        // 4. Bullet Tracers in 3D
        val now = System.currentTimeMillis()
        for (tracer in tracers) {
            if (now - tracer.createdAtMs < 120L) {
                val pStart = project(tracer.start)
                val pEnd = project(tracer.end)
                if (pStart.visible && pEnd.visible) {
                    drawScope.drawLine(
                        color = Color(0xFFFFF099),
                        start = Offset(pStart.sx, pStart.sy),
                        end = Offset(pEnd.sx, pEnd.sy),
                        strokeWidth = 3f
                    )
                }
            }
        }

        // 5. Particles (Muzzle spark & impact)
        for (part in particles) {
            val p = project(part.pos)
            if (p.visible) {
                drawScope.drawCircle(
                    color = part.color,
                    radius = 4f,
                    center = Offset(p.sx, p.sy)
                )
            }
        }

        // 6. CHEAT: WALLHACK / ESP (Drawn over everything!)
        if (cheatSettings.wallhackEsp) {
            for (bot in bots) {
                if (!bot.isAlive) continue
                val botBase = Vec3(bot.x, bot.y, bot.z)
                val botHead = Vec3(bot.x, bot.y + 1.8f, bot.z)
                val baseP = project(botBase)
                val headP = project(botHead)

                if (baseP.visible && headP.visible) {
                    val isEnemy = bot.team != playerTeam
                    val espColor = if (isEnemy) Color(0xFFFF1744) else Color(0xFF00E5FF)
                    val botHeight = (baseP.sy - headP.sy).coerceAtLeast(14f)
                    val botWidth = botHeight * 0.5f
                    val centerX = (baseP.sx + headP.sx) / 2f
                    val leftX = centerX - botWidth / 2f
                    val topY = headP.sy - 4f

                    // 2D Box ESP
                    if (cheatSettings.wallhackBoxes) {
                        drawScope.drawRect(
                            color = espColor,
                            topLeft = Offset(leftX, topY),
                            size = androidx.compose.ui.geometry.Size(botWidth, botHeight + 8f),
                            style = Stroke(width = 2.0f)
                        )
                        // Corner accent brackets
                        val cornerLen = (botWidth * 0.25f).coerceAtLeast(6f)
                        drawScope.drawLine(Color.White, Offset(leftX, topY), Offset(leftX + cornerLen, topY), strokeWidth = 2.5f)
                        drawScope.drawLine(Color.White, Offset(leftX, topY), Offset(leftX, topY + cornerLen), strokeWidth = 2.5f)
                    }

                    // ESP Snaplines (Line from crosshair to enemy)
                    if (cheatSettings.wallhackSnaplines && isEnemy) {
                        drawScope.drawLine(
                            color = Color(0x99FF1744),
                            start = Offset(width / 2f, height / 2f),
                            end = Offset(centerX, topY),
                            strokeWidth = 1.5f
                        )
                    }

                    // Health Bar ESP on left side of box
                    if (cheatSettings.wallhackHealth) {
                        val hpBarW = 3.5f
                        val hpBarX = leftX - 6f
                        val hpPct = (bot.health / 100f).coerceIn(0f, 1f)
                        val hpCol = if (hpPct > 0.5f) Color(0xFF00E676) else Color(0xFFFF1744)

                        drawScope.drawRect(Color(0xCC000000), Offset(hpBarX - 1f, topY), androidx.compose.ui.geometry.Size(hpBarW + 2f, botHeight + 8f))
                        drawScope.drawRect(hpCol, Offset(hpBarX, topY + (botHeight + 8f) * (1f - hpPct)), androidx.compose.ui.geometry.Size(hpBarW, (botHeight + 8f) * hpPct))
                    }

                    // ESP Tags: Name, Distance, Weapon
                    val distM = (baseP.z / 2f).toInt()
                    drawScope.drawContext.canvas.nativeCanvas.apply {
                        textPaint.color = android.graphics.Color.WHITE
                        textPaint.textSize = (botWidth * 0.26f).coerceIn(11f, 16f)
                        drawText("${bot.name} [${distM}m]", leftX, topY - 6f, textPaint)
                        textPaint.color = android.graphics.Color.YELLOW
                        textPaint.textSize = (botWidth * 0.22f).coerceIn(9f, 13f)
                        drawText("${bot.weapon.title} | ${bot.health}HP", leftX, topY + botHeight + 20f, textPaint)
                    }
                }
            }
        }

        // 7. AWP Scope Overlay (If scoping)
        if (isScoping) {
            val scopeRadius = height * 0.44f
            val cx = width / 2f
            val cy = height / 2f

            // Black vignette outside scope lens
            drawScope.drawRect(Color.Black, Offset(0f, 0f), androidx.compose.ui.geometry.Size(cx - scopeRadius, height))
            drawScope.drawRect(Color.Black, Offset(cx + scopeRadius, 0f), androidx.compose.ui.geometry.Size(width - (cx + scopeRadius), height))
            drawScope.drawRect(Color.Black, Offset(cx - scopeRadius, 0f), androidx.compose.ui.geometry.Size(scopeRadius * 2f, cy - scopeRadius))
            drawScope.drawRect(Color.Black, Offset(cx - scopeRadius, cy + scopeRadius), androidx.compose.ui.geometry.Size(scopeRadius * 2f, height - (cy + scopeRadius)))

            // Scope crosshair lines
            drawScope.drawLine(Color(0xEE000000), Offset(cx - scopeRadius, cy), Offset(cx + scopeRadius, cy), strokeWidth = 2f)
            drawScope.drawLine(Color(0xEE000000), Offset(cx, cy - scopeRadius), Offset(cx, cy + scopeRadius), strokeWidth = 2f)
            // Lens rim
            drawScope.drawCircle(Color(0x88000000), radius = scopeRadius, center = Offset(cx, cy), style = Stroke(width = 8f))
        }

        // 8. First Person Weapon Viewmodel (drawn in bottom-right corner)
        if (!isScoping) {
            renderViewmodel(
                drawScope = drawScope,
                width = width,
                height = height,
                skin = activeSkin,
                recoil = recoilAnim,
                inspect = inspectAnim,
                isReloading = isReloading,
                reloadProgress = reloadProgress
            )
        }

        // 9. CS2 Crosshair
        if (!isScoping) {
            renderCrosshair(drawScope, width, height, hudSettings, recoilAnim)
        }

        // 10. Aimbot FOV Circle (when Aimbot cheat is active)
        if (cheatSettings.aimbotEnabled) {
            val fovCircleRadius = (hudSettings.fov / 90f) * (cheatSettings.aimbotFov * 4.5f)
            drawScope.drawCircle(
                color = Color(0x44FF2222),
                radius = fovCircleRadius,
                center = Offset(width / 2f, height / 2f),
                style = Stroke(width = 1.5f)
            )
        }
    }

    private fun renderViewmodel(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        skin: WeaponSkin,
        recoil: Float,
        inspect: Float,
        isReloading: Boolean,
        reloadProgress: Float
    ) {
        val gunBaseX = width * 0.72f
        val gunBaseY = height * 0.76f

        // Recoil kickback + Inspect tilt rotation + Reload drop
        val recoilY = -recoil * 35f
        val recoilX = recoil * 10f
        val inspectOffset = sin(inspect * Math.PI).toFloat() * 60f
        val reloadY = if (isReloading) sin(reloadProgress * Math.PI).toFloat() * 120f else 0f

        val currentX = gunBaseX + recoilX + inspectOffset
        val currentY = gunBaseY + recoilY + reloadY

        when (skin.weaponType) {
            WeaponType.KNIFE -> {
                // Draw 3D Blade (Karambit / Butterfly / M9)
                val bladeLength = height * 0.28f
                val bladeAngle = if (inspect > 0f) inspect * 360f else 15f

                // Arm / Hand
                drawScope.drawRoundRect(
                    color = Color(0xFFC68642), // Skin tone / glove
                    topLeft = Offset(currentX - 20f, currentY + 30f),
                    size = androidx.compose.ui.geometry.Size(90f, 150f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                )
                // Tactical glove
                drawScope.drawRoundRect(
                    color = Color(0xFF1E272C),
                    topLeft = Offset(currentX - 15f, currentY + 20f),
                    size = androidx.compose.ui.geometry.Size(80f, 80f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(15f, 15f)
                )

                // Knife Handle
                drawScope.drawRoundRect(
                    color = skin.secondaryColor,
                    topLeft = Offset(currentX, currentY - 20f),
                    size = androidx.compose.ui.geometry.Size(26f, 65f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

                // Curved Blade / Karambit Claw or M9 Steel
                val bladePath = Path().apply {
                    if (skin.knifeType == com.example.model.KnifeType.KARAMBIT) {
                        moveTo(currentX + 12f, currentY - 20f)
                        cubicTo(
                            currentX + 40f, currentY - 70f,
                            currentX - 50f, currentY - 140f,
                            currentX - 90f, currentY - 110f
                        )
                        cubicTo(
                            currentX - 40f, currentY - 80f,
                            currentX + 10f, currentY - 45f,
                            currentX + 24f, currentY - 20f
                        )
                        close()
                    } else {
                        // Standard / Butterfly / M9 straight jagged blade
                        moveTo(currentX + 6f, currentY - 20f)
                        lineTo(currentX + 8f, currentY - bladeLength)
                        lineTo(currentX + 30f, currentY - bladeLength * 0.85f)
                        lineTo(currentX + 24f, currentY - 20f)
                        close()
                    }
                }
                // Primary skin color (e.g. Fade / Blue Gem / Doppler)
                drawScope.drawPath(bladePath, skin.primaryColor)
                // Metallic shine overlay
                drawScope.drawPath(bladePath, skin.accentColor.copy(alpha = 0.5f), style = Stroke(width = 3f))
            }
            WeaponType.AK47, WeaponType.M4A4 -> {
                // Rifle barrel, receiver, wooden/skin grip, magazine
                val gunLen = width * 0.28f
                val gunH = height * 0.16f

                // Receiver
                drawScope.drawRoundRect(
                    color = skin.primaryColor,
                    topLeft = Offset(currentX - 40f, currentY - 40f),
                    size = androidx.compose.ui.geometry.Size(gunLen * 0.6f, gunH * 0.45f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                // Barrel
                drawScope.drawRect(
                    color = skin.secondaryColor,
                    topLeft = Offset(currentX - 40f - gunLen * 0.45f, currentY - 32f),
                    size = androidx.compose.ui.geometry.Size(gunLen * 0.5f, 14f)
                )
                // Muzzle brake
                drawScope.drawRect(
                    color = Color(0xFF111111),
                    topLeft = Offset(currentX - 40f - gunLen * 0.48f, currentY - 35f),
                    size = androidx.compose.ui.geometry.Size(18f, 20f)
                )
                // Curved Magazine
                drawScope.drawRoundRect(
                    color = skin.accentColor,
                    topLeft = Offset(currentX + 10f, currentY - 10f),
                    size = androidx.compose.ui.geometry.Size(32f, 75f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                // Hands / Gloves holding weapon
                drawScope.drawCircle(
                    color = Color(0xFF222B32),
                    radius = 35f,
                    center = Offset(currentX + 70f, currentY + 15f)
                )
                // Foregrip hand
                drawScope.drawCircle(
                    color = Color(0xFF222B32),
                    radius = 28f,
                    center = Offset(currentX - 30f, currentY - 15f)
                )

                // Recoil Muzzle Flash
                if (recoil > 0.4f) {
                    val muzzleX = currentX - 40f - gunLen * 0.48f
                    val muzzleY = currentY - 25f
                    drawScope.drawCircle(
                        color = Color(0xFFFFAA00),
                        radius = recoil * 35f,
                        center = Offset(muzzleX, muzzleY)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = recoil * 18f,
                        center = Offset(muzzleX, muzzleY)
                    )
                }
            }
            WeaponType.AWP -> {
                // Long green/gold sniper rifle body and massive scope
                val gunLen = width * 0.35f
                drawScope.drawRoundRect(
                    color = skin.primaryColor,
                    topLeft = Offset(currentX - 60f, currentY - 35f),
                    size = androidx.compose.ui.geometry.Size(gunLen * 0.65f, 32f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Long Fluted Barrel
                drawScope.drawRect(
                    color = skin.secondaryColor,
                    topLeft = Offset(currentX - 60f - gunLen * 0.5f, currentY - 30f),
                    size = androidx.compose.ui.geometry.Size(gunLen * 0.52f, 16f)
                )
                // Huge AWP Scope cylinder
                drawScope.drawRoundRect(
                    color = Color(0xFF1A1A1A),
                    topLeft = Offset(currentX - 30f, currentY - 65f),
                    size = androidx.compose.ui.geometry.Size(110f, 24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Gloves
                drawScope.drawCircle(
                    color = Color(0xFF222B32),
                    radius = 36f,
                    center = Offset(currentX + 60f, currentY + 15f)
                )
                // Muzzle flash on AWP
                if (recoil > 0.3f) {
                    val mX = currentX - 60f - gunLen * 0.5f
                    val mY = currentY - 22f
                    drawScope.drawCircle(Color(0xFFFFDD00), radius = recoil * 48f, center = Offset(mX, mY))
                }
            }
            WeaponType.DEAGLE -> {
                // Heavy pistol slide & grip
                drawScope.drawRoundRect(
                    color = skin.primaryColor,
                    topLeft = Offset(currentX - 25f, currentY - 35f),
                    size = androidx.compose.ui.geometry.Size(95f, 34f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawScope.drawRoundRect(
                    color = skin.secondaryColor,
                    topLeft = Offset(currentX + 25f, currentY - 5f),
                    size = androidx.compose.ui.geometry.Size(35f, 65f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawScope.drawCircle(
                    color = Color(0xFF222B32),
                    radius = 32f,
                    center = Offset(currentX + 40f, currentY + 30f)
                )
                if (recoil > 0.4f) {
                    drawScope.drawCircle(Color(0xFFFFCC00), radius = recoil * 32f, center = Offset(currentX - 30f, currentY - 20f))
                }
            }
            else -> {}
        }
    }

    private fun renderCrosshair(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        hud: HudSettings,
        recoil: Float
    ) {
        val cx = width / 2f
        val cy = height / 2f
        val gap = hud.crosshairGap + (recoil * 12f)
        val len = hud.crosshairSize
        val color = hud.crosshairColor

        // Center dot
        if (hud.crosshairDot) {
            drawScope.drawCircle(color, radius = 2f, center = Offset(cx, cy))
        }

        // Top line
        drawScope.drawLine(color, Offset(cx, cy - gap - len), Offset(cx, cy - gap), strokeWidth = 2.5f)
        // Bottom line
        drawScope.drawLine(color, Offset(cx, cy + gap), Offset(cx, cy + gap + len), strokeWidth = 2.5f)
        // Left line
        drawScope.drawLine(color, Offset(cx - gap - len, cy), Offset(cx - gap, cy), strokeWidth = 2.5f)
        // Right line
        drawScope.drawLine(color, Offset(cx + gap, cy), Offset(cx + gap + len, cy), strokeWidth = 2.5f)
    }
}
