package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine3d.CS2Renderer3D
import com.example.engine3d.Dust2Geometry
import com.example.logic.CS2GameEngine
import com.example.model.BombState
import com.example.model.Team
import com.example.model.WeaponType
import com.example.ui.theme.CS2Background
import com.example.ui.theme.CS2Border
import com.example.ui.theme.CS2CardBg
import com.example.ui.theme.CS2CtBlue
import com.example.ui.theme.CS2Cyan
import com.example.ui.theme.CS2Gold
import com.example.ui.theme.CS2Green
import com.example.ui.theme.CS2Red
import com.example.ui.theme.CS2TAmber
import com.example.ui.theme.CS2TextPrimary
import com.example.ui.theme.CS2TextSecondary
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CS2GameScreen(
    gameEngine: CS2GameEngine,
    onExitToLobby: () -> Unit
) {
    val renderer = remember { CS2Renderer3D() }
    val tick by gameEngine.stateUpdateTick.collectAsState()

    var showScoreboard by remember { mutableStateOf(false) }
    var showBuyMenu by remember { mutableStateOf(false) }
    var showInGameCheatMenu by remember { mutableStateOf(false) }

    // Virtual Joystick local state
    var joystickDeltaX by remember { mutableFloatStateOf(0f) }
    var joystickDeltaY by remember { mutableFloatStateOf(0f) }

    // 60 FPS Game Loop
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastNanos != 0L) {
                    val deltaSec = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                    gameEngine.updateGame(deltaSec)
                }
                lastNanos = frameNanos
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // 1. 3D DUST II CANVAS
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cs2_3d_canvas")
        ) {
            // Read tick to guarantee recomposition
            val _t = tick
            renderer.renderScene(
                drawScope = this,
                camPos = gameEngine.playerPos,
                camYaw = gameEngine.playerYaw,
                camPitch = gameEngine.playerPitch,
                hudSettings = gameEngine.hudSettings,
                cheatSettings = gameEngine.cheatSettings,
                bots = gameEngine.bots,
                playerTeam = gameEngine.playerTeam,
                activeSkin = gameEngine.getActiveSkin(),
                recoilAnim = gameEngine.recoilAnim,
                inspectAnim = gameEngine.inspectProgress,
                isReloading = gameEngine.isReloading,
                reloadProgress = gameEngine.reloadProgress,
                isScoping = gameEngine.isScoping,
                tracers = gameEngine.tracers,
                particles = gameEngine.particles
            )
        }

        // 2. RIGHT-SIDE TOUCH AREA FOR CAMERA LOOK / SWIPE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Only trigger if touch started on right 65% of screen
                        if (change.position.x > size.width * 0.35f) {
                            gameEngine.handleLookSwipe(dragAmount.x, dragAmount.y)
                        }
                    }
                }
        )

        // 3. TOP HUD: RADAR, MATCH HEADER, KILLFEED, BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // LEFT: Tactical Minimap (Radar)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xBB141A24))
                    .border(2.dp, CS2Border, CircleShape)
            ) {
                RadarMinimap(gameEngine)
            }

            // CENTER: Match Score & Round Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Team Scores & Timer Card
                Row(
                    modifier = Modifier
                        .background(Color(0xCC141A24), RoundedCornerShape(8.dp))
                        .border(1.dp, CS2Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CT Team
                    Text("CT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CS2CtBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${gameEngine.ctScore}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = CS2CtBlue
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    // Timer & Round
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${gameEngine.roundTimerSeconds / 60}:${(gameEngine.roundTimerSeconds % 60).toString().padStart(2, '0')}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (gameEngine.bombState == BombState.PLANTED) CS2Red else Color.White
                        )
                        Text("РАУНД ${gameEngine.roundNumber}", fontSize = 9.sp, color = CS2TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    // T Team
                    Text(
                        text = "${gameEngine.tScore}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = CS2TAmber
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("T", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CS2TAmber)
                }

                // Round Announcement banner
                gameEngine.roundEndAnnouncement?.let { ann ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xEE1E272C), RoundedCornerShape(6.dp))
                            .border(1.5.dp, CS2Gold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = ann,
                            color = CS2Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // RIGHT: Killfeed & Menu Buttons
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Top control buttons (TAB, CHEATS, EXIT)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // In-Game Cheats Mod Menu Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xDDEF4444))
                            .clickable { showInGameCheatMenu = !showInGameCheatMenu }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("in_game_cheats_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = "Hacks", tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HACKS", fontWeight = FontWeight.Black, fontSize = 10.sp, color = Color.Black)
                        }
                    }

                    // Scoreboard Button (TAB)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x99141A24))
                            .border(1.dp, CS2Border, RoundedCornerShape(6.dp))
                            .clickable { showScoreboard = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("scoreboard_tab_button")
                    ) {
                        Text("TAB", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CS2TextPrimary)
                    }

                    // Buy Menu Button (B)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x99141A24))
                            .border(1.dp, CS2Border, RoundedCornerShape(6.dp))
                            .clickable { showBuyMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("buy_menu_button")
                    ) {
                        Text("BUY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CS2Gold)
                    }

                    // Exit Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x99141A24))
                            .border(1.dp, CS2Border, RoundedCornerShape(6.dp))
                            .clickable(onClick = onExitToLobby)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Killfeed Stack
                val recentKills = gameEngine.killFeed.takeLast(4).reversed()
                for (entry in recentKills) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 1.dp)
                            .background(Color(0x99000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.killerName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.killerTeam == Team.CT) CS2CtBlue else CS2TAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "[${entry.weaponName}]",
                            fontSize = 9.sp,
                            color = Color(0xFFDDDDDD)
                        )
                        if (entry.isHeadshot) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("⚡HS", fontSize = 9.sp, color = CS2Red, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = entry.victimName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.victimTeam == Team.CT) CS2CtBlue else CS2TAmber
                        )
                    }
                }
            }
        }

        // 4. VIRTUAL JOYSTICK (LEFT SIDE)
        val hud = gameEngine.hudSettings
        val joyX = (screenW.value * (hud.joystickXPercent / 100f)).dp
        val joyY = (screenH.value * (hud.joystickYPercent / 100f)).dp
        val joySize = (110f * hud.joystickScale).dp

        Box(
            modifier = Modifier
                .offset { IntOffset(joyX.roundToPx() - (joySize.roundToPx() / 2), joyY.roundToPx() - (joySize.roundToPx() / 2)) }
                .size(joySize)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .border(2.dp, Color(0x66FFFFFF), CircleShape)
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            val r = joySize.value * 1.5f
                            val cx = motionEvent.x - r
                            val cy = motionEvent.y - r
                            val dist = sqrt(cx * cx + cy * cy)
                            val normDist = dist.coerceAtMost(r)
                            val normX = if (dist > 0) (cx / dist) * normDist else 0f
                            val normY = if (dist > 0) (cy / dist) * normDist else 0f

                            joystickDeltaX = normX / r
                            joystickDeltaY = normY / r
                            gameEngine.handleJoystickInput(joystickDeltaX, joystickDeltaY)
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            joystickDeltaX = 0f
                            joystickDeltaY = 0f
                            gameEngine.handleJoystickInput(0f, 0f)
                            true
                        }
                        else -> false
                    }
                }
                .testTag("virtual_joystick"),
            contentAlignment = Alignment.Center
        ) {
            // Thumbstick handle
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (joystickDeltaX * (joySize.roundToPx() / 3)).toInt(),
                            (joystickDeltaY * (joySize.roundToPx() / 3)).toInt()
                        )
                    }
                    .size((joySize.value * 0.42f).dp)
                    .clip(CircleShape)
                    .background(Color(0x99FFFFFF))
            )
        }

        // 5. BOTTOM HUD: HEALTH, ARMOR, WEAPON CAROUSEL, AMMO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp, start = 14.dp, end = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // HEALTH & ARMOR
                Row(
                    modifier = Modifier
                        .background(Color(0xCC141A24), RoundedCornerShape(8.dp))
                        .border(1.dp, CS2Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF00FF66))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${gameEngine.playerHealth}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Icon(Icons.Default.Security, contentDescription = "Armor", tint = CS2Cyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${gameEngine.playerArmor}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // WEAPON CAROUSEL
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeaponSlotButton("1", WeaponType.AK47, gameEngine)
                    WeaponSlotButton("2", WeaponType.DEAGLE, gameEngine)
                    WeaponSlotButton("3", WeaponType.KNIFE, gameEngine)
                    WeaponSlotButton("4", WeaponType.HE_GRENADE, gameEngine)
                    if (gameEngine.playerTeam == Team.T) {
                        WeaponSlotButton("5", WeaponType.C4, gameEngine)
                    }
                }

                // AMMO & WEAPON NAME
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .background(Color(0xCC141A24), RoundedCornerShape(8.dp))
                        .border(1.dp, CS2Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val clip = gameEngine.ammoInClip[gameEngine.activeWeaponType] ?: 0
                    val isInf = gameEngine.cheatSettings.infiniteAmmo
                    Text(
                        text = if (isInf) "$clip | ∞" else "$clip | ${gameEngine.activeWeaponType.maxReserve}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (isInf) CS2Cyan else CS2Gold
                    )
                    Text(
                        text = gameEngine.getActiveSkin().name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = CS2TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // 6. ACTION BUTTONS (FIRE, JUMP, CROUCH, RELOAD, SCOPE, INSPECT)
        // Fire Button
        val fireX = (screenW.value * (hud.fireButtonXPercent / 100f)).dp
        val fireY = (screenH.value * (hud.fireButtonYPercent / 100f)).dp
        val fireSize = (72f * hud.fireButtonScale).dp

        Box(
            modifier = Modifier
                .offset { IntOffset(fireX.roundToPx() - (fireSize.roundToPx() / 2), fireY.roundToPx() - (fireSize.roundToPx() / 2)) }
                .size(fireSize)
                .clip(CircleShape)
                .background(Color(0x99EF4444))
                .border(2.5.dp, Color(0xFFFF5252), CircleShape)
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            gameEngine.fireWeapon()
                            true
                        }
                        else -> true
                    }
                }
                .testTag("fire_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FIRE",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = Color.White
            )
        }

        // Jump Button
        val jumpX = (screenW.value * (hud.jumpButtonXPercent / 100f)).dp
        val jumpY = (screenH.value * (hud.jumpButtonYPercent / 100f)).dp
        Box(
            modifier = Modifier
                .offset { IntOffset(jumpX.roundToPx() - 30, jumpY.roundToPx() - 30) }
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0x66FFFFFF))
                .border(1.5.dp, Color.White, CircleShape)
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            gameEngine.jump()
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            gameEngine.releaseJump()
                            true
                        }
                        else -> false
                    }
                }
                .testTag("jump_button"),
            contentAlignment = Alignment.Center
        ) {
            Text("JUMP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Crouch Button
        val crouchX = (screenW.value * (hud.crouchButtonXPercent / 100f)).dp
        val crouchY = (screenH.value * (hud.crouchButtonYPercent / 100f)).dp
        Box(
            modifier = Modifier
                .offset { IntOffset(crouchX.roundToPx() - 25, crouchY.roundToPx() - 25) }
                .size(48.dp)
                .clip(CircleShape)
                .background(if (gameEngine.isCrouching) Color(0x9900E5FF) else Color(0x66FFFFFF))
                .border(1.5.dp, Color.White, CircleShape)
                .clickable { gameEngine.toggleCrouch() }
                .testTag("crouch_button"),
            contentAlignment = Alignment.Center
        ) {
            Text("DUCK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Reload Button
        val reloadX = (screenW.value * (hud.reloadButtonXPercent / 100f)).dp
        val reloadY = (screenH.value * (hud.reloadButtonYPercent / 100f)).dp
        Box(
            modifier = Modifier
                .offset { IntOffset(reloadX.roundToPx() - 24, reloadY.roundToPx() - 24) }
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0x66141A24))
                .border(1.5.dp, CS2Gold, CircleShape)
                .clickable { gameEngine.startReload() }
                .testTag("reload_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = CS2Gold, modifier = Modifier.size(20.dp))
        }

        // Scope Button (AWP)
        if (gameEngine.activeWeaponType == WeaponType.AWP) {
            val scopeX = (screenW.value * (hud.scopeButtonXPercent / 100f)).dp
            val scopeY = (screenH.value * (hud.scopeButtonYPercent / 100f)).dp
            Box(
                modifier = Modifier
                    .offset { IntOffset(scopeX.roundToPx() - 24, scopeY.roundToPx() - 24) }
                .size(46.dp)
                .clip(CircleShape)
                .background(if (gameEngine.isScoping) Color(0x9900FF66) else Color(0x66141A24))
                .border(1.5.dp, Color.White, CircleShape)
                .clickable { gameEngine.toggleScope() }
                .testTag("scope_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.RemoveRedEye, contentDescription = "Scope", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Inspect Weapon Button (F)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 72.dp, end = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x55141A24))
                .border(1.dp, Color.White, CircleShape)
                .clickable { gameEngine.triggerInspect() }
                .testTag("inspect_weapon_button"),
            contentAlignment = Alignment.Center
        ) {
            Text("F", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Bomb Plant / Defuse Action Prompt (when nearby)
        val distToA = gameEngine.playerPos.distanceTo(Dust2Geometry.BOMBSITE_A_POS)
        val distToB = gameEngine.playerPos.distanceTo(Dust2Geometry.BOMBSITE_B_POS)
        val canPlant = gameEngine.playerTeam == Team.T && gameEngine.bombState == BombState.CARRIED && (distToA < 15f || distToB < 15f)
        val canDefuse = gameEngine.playerTeam == Team.CT && gameEngine.bombState == BombState.PLANTED && gameEngine.playerPos.distanceTo(gameEngine.bombPos) < 7f

        if (canPlant) {
            Button(
                onClick = { gameEngine.plantBomb() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 80.dp)
                    .testTag("plant_bomb_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CS2Red)
            ) {
                Text("ВСТАНОВИТИ C4 (PLANT)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        } else if (canDefuse) {
            Button(
                onClick = { gameEngine.defuseBomb() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 80.dp)
                    .testTag("defuse_bomb_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CS2CtBlue)
            ) {
                Text("ЗНЕШКОДИТИ C4 (DEFUSE)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // 7. IN-GAME CHEAT MENU POPUP (User requested toggling cheats directly in game!)
        AnimatedVisibility(
            visible = showInGameCheatMenu,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            InGameCheatMenuDialog(
                gameEngine = gameEngine,
                onDismiss = { showInGameCheatMenu = false }
            )
        }

        // 8. SCOREBOARD DIALOG (TAB)
        if (showScoreboard) {
            ScoreboardDialog(
                gameEngine = gameEngine,
                onDismiss = { showScoreboard = false }
            )
        }

        // 9. BUY MENU DIALOG (B)
        if (showBuyMenu) {
            BuyMenuDialog(
                gameEngine = gameEngine,
                onDismiss = { showBuyMenu = false }
            )
        }
    }
}

@Composable
fun WeaponSlotButton(
    slotNumber: String,
    weaponType: WeaponType,
    gameEngine: CS2GameEngine
) {
    val isSelected = gameEngine.activeWeaponType == weaponType
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0x88DE9B35) else Color(0x66141A24))
            .border(1.dp, if (isSelected) CS2Gold else CS2Border, RoundedCornerShape(6.dp))
            .clickable { gameEngine.selectWeaponSlot(weaponType) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(slotNumber, fontSize = 8.sp, color = CS2TextSecondary)
            Text(
                text = weaponType.title.take(6),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else CS2TextPrimary
            )
        }
    }
}

@Composable
fun RadarMinimap(gameEngine: CS2GameEngine) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val scale = 0.55f

        // Draw Dust 2 Outline / Waypoints
        for (wp in Dust2Geometry.WAYPOINTS) {
            val rx = cx + (wp.pos.x - gameEngine.playerPos.x) * scale
            val ry = cy + (wp.pos.z - gameEngine.playerPos.z) * scale

            if (wp.isBombsiteA) {
                drawCircle(Color(0xFFFF2222), radius = 5f, center = Offset(rx, ry))
            } else if (wp.isBombsiteB) {
                drawCircle(Color(0xFF2288FF), radius = 5f, center = Offset(rx, ry))
            } else {
                drawCircle(Color(0x33FFFFFF), radius = 2f, center = Offset(rx, ry))
            }
        }

        // Draw Teammates & Enemies (if spotted or wallhack ESP)
        for (bot in gameEngine.bots) {
            if (!bot.isAlive) continue
            val bx = cx + (bot.x - gameEngine.playerPos.x) * scale
            val by = cy + (bot.z - gameEngine.playerPos.z) * scale

            val isTeammate = bot.team == gameEngine.playerTeam
            if (isTeammate) {
                drawCircle(CS2CtBlue, radius = 3.5f, center = Offset(bx, by))
            } else if (gameEngine.cheatSettings.wallhackEsp || gameEngine.playerPos.distanceTo(com.example.engine3d.Vec3(bot.x, bot.y, bot.z)) < 30f) {
                // Enemy spotted on radar
                drawCircle(CS2Red, radius = 3.5f, center = Offset(bx, by))
            }
        }

        // Draw Player Arrow in center
        val yawRad = Math.toRadians(gameEngine.playerYaw.toDouble()).toFloat()
        val dirX = -sin(yawRad) * 8f
        val dirY = cos(yawRad) * 8f

        drawCircle(Color(0xFF00FF66), radius = 4f, center = Offset(cx, cy))
        drawLine(
            color = Color(0xFF00FF66),
            start = Offset(cx, cy),
            end = Offset(cx + dirX, cy + dirY),
            strokeWidth = 2.5f
        )
    }
}

@Composable
fun InGameCheatMenuDialog(
    gameEngine: CS2GameEngine,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(360.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA141A24)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CS2Red),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = CS2Red, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CS2 IN-GAME CHEATS", fontWeight = FontWeight.Black, fontSize = 13.sp, color = CS2Red)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CheatRow(
                title = "Бехоб (Bunnyhop)",
                checked = gameEngine.cheatSettings.bhopEnabled,
                onChecked = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(bhopEnabled = it) }
            )
            CheatRow(
                title = "Аімбот (Aimbot Headlock)",
                checked = gameEngine.cheatSettings.aimbotEnabled,
                onChecked = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(aimbotEnabled = it) }
            )
            CheatRow(
                title = "Бачити крізь стіни (Wallhack ESP)",
                checked = gameEngine.cheatSettings.wallhackEsp,
                onChecked = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(wallhackEsp = it) }
            )
            CheatRow(
                title = "ESP Snaplines (Промені до ворогів)",
                checked = gameEngine.cheatSettings.wallhackSnaplines,
                onChecked = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(wallhackSnaplines = it) }
            )
            CheatRow(
                title = "Нескінченні патрони (Infinite Ammo)",
                checked = gameEngine.cheatSettings.infiniteAmmo,
                onChecked = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(infiniteAmmo = it) }
            )
            CheatRow(
                title = "NoClip (Політ крізь стіни)",
                checked = gameEngine.cheatSettings.noClip,
                onChecked = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(noClip = it) }
            )
            CheatRow(
                title = "Скін Хак (Skin Changer)",
                checked = gameEngine.cheatSettings.skinHackUnlocked,
                onChecked = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(skinHackUnlocked = it) }
            )
        }
    }
}

@Composable
fun CheatRow(
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (checked) CS2Cyan else CS2TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            modifier = Modifier.size(width = 36.dp, height = 22.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = CS2Cyan,
                checkedTrackColor = Color(0x4400E5FF)
            )
        )
    }
}

@Composable
fun ScoreboardDialog(
    gameEngine: CS2GameEngine,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFA141A24)),
            border = androidx.compose.foundation.BorderStroke(1.dp, CS2Border),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ТАБЛИЦЯ РЕЗУЛЬТАТІВ (SCOREBOARD 5v5)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CS2Gold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CT Score Section
                Text("СПЕЦНАЗ (CT) - ${gameEngine.ctScore}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CS2CtBlue)
                ScoreboardHeader()
                if (gameEngine.playerTeam == Team.CT) {
                    ScoreboardRow("Всі (You)", gameEngine.playerKills, gameEngine.playerDeaths, 15, true)
                }
                gameEngine.bots.filter { it.team == Team.CT }.forEach {
                    ScoreboardRow(it.name, it.kills, it.deaths, it.ping, false)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // T Score Section
                Text("ТЕРОРИСТИ (T) - ${gameEngine.tScore}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CS2TAmber)
                ScoreboardHeader()
                if (gameEngine.playerTeam == Team.T) {
                    ScoreboardRow("Всі (You)", gameEngine.playerKills, gameEngine.playerDeaths, 15, true)
                }
                gameEngine.bots.filter { it.team == Team.T }.forEach {
                    ScoreboardRow(it.name, it.kills, it.deaths, it.ping, false)
                }
            }
        }
    }
}

@Composable
fun ScoreboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Гравець", fontSize = 10.sp, color = CS2TextSecondary, modifier = Modifier.weight(2f))
        Text("Вбивства", fontSize = 10.sp, color = CS2TextSecondary, modifier = Modifier.weight(1f))
        Text("Смерті", fontSize = 10.sp, color = CS2TextSecondary, modifier = Modifier.weight(1f))
        Text("Пінг", fontSize = 10.sp, color = CS2TextSecondary, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ScoreboardRow(name: String, kills: Int, deaths: Int, ping: Int, isPlayer: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isPlayer) Color(0x33DE9B35) else Color.Transparent, RoundedCornerShape(4.dp))
            .padding(vertical = 3.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = 11.sp, fontWeight = if (isPlayer) FontWeight.Bold else FontWeight.Normal, color = if (isPlayer) CS2Gold else Color.White, modifier = Modifier.weight(2f))
        Text("$kills", fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1f))
        Text("$deaths", fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1f))
        Text("${ping}ms", fontSize = 11.sp, color = CS2Cyan, modifier = Modifier.weight(1f))
    }
}

@Composable
fun BuyMenuDialog(
    gameEngine: CS2GameEngine,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFA141A24)),
            border = androidx.compose.foundation.BorderStroke(1.dp, CS2Gold),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("МЕНЮ КУПІВЛІ ЗБРОЇ (BUY MENU)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CS2Gold)
                    Text("Баланс: $${gameEngine.playerMoney}", fontSize = 12.sp, color = CS2Cyan, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                BuyItemRow(WeaponType.AK47, "$2700", gameEngine) {
                    gameEngine.buyWeapon(WeaponType.AK47)
                    onDismiss()
                }
                BuyItemRow(WeaponType.M4A4, "$3100", gameEngine) {
                    gameEngine.buyWeapon(WeaponType.M4A4)
                    onDismiss()
                }
                BuyItemRow(WeaponType.AWP, "$4750", gameEngine) {
                    gameEngine.buyWeapon(WeaponType.AWP)
                    onDismiss()
                }
                BuyItemRow(WeaponType.DEAGLE, "$700", gameEngine) {
                    gameEngine.buyWeapon(WeaponType.DEAGLE)
                    onDismiss()
                }
            }
        }
    }
}

@Composable
fun BuyItemRow(
    weapon: WeaponType,
    priceStr: String,
    gameEngine: CS2GameEngine,
    onBuy: () -> Unit
) {
    val canAfford = gameEngine.playerMoney >= weapon.price
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0x441A2230), RoundedCornerShape(6.dp))
            .clickable(enabled = canAfford, onClick = onBuy)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(weapon.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            Text("Потужність: ${weapon.damage} | Обойма: ${weapon.maxClip}", fontSize = 9.sp, color = CS2TextSecondary)
        }
        Text(priceStr, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (canAfford) CS2Green else CS2Red)
    }
}
