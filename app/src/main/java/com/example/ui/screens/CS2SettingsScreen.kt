package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CS2SoundManager
import com.example.logic.CS2GameEngine
import com.example.model.HudSettings
import com.example.ui.theme.CS2Background
import com.example.ui.theme.CS2Border
import com.example.ui.theme.CS2Cyan
import com.example.ui.theme.CS2Gold
import com.example.ui.theme.CS2Red
import com.example.ui.theme.CS2TextPrimary
import com.example.ui.theme.CS2TextSecondary

@Composable
fun CS2SettingsScreen(
    gameEngine: CS2GameEngine,
    onBack: () -> Unit
) {
    var activeTab by remember { mutableStateOf("GAMEPLAY") }
    val hud = gameEngine.hudSettings

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CS2Background)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0x99141A24), RoundedCornerShape(8.dp))
                    .border(1.dp, CS2Border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "НАЛАШТУВАННЯ CS2 (SETTINGS & HUD)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CS2Gold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsSubTab("КУТ ОГЛЯДУ ТА КЕРУВАННЯ", activeTab == "GAMEPLAY") { activeTab = "GAMEPLAY" }
                    SettingsSubTab("КАСТОМІЗАЦІЯ КНОПОК HUD", activeTab == "BUTTONS") { activeTab = "BUTTONS" }
                    SettingsSubTab("ПРИЦІЛ ТА ЗВУК", activeTab == "CROSSHAIR") { activeTab = "CROSSHAIR" }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Settings Content
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD141A24)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CS2Border),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (activeTab) {
                        "GAMEPLAY" -> {
                            // FOV (Поле зору)
                            Text(
                                text = "КУТ ОГЛЯДУ / ПОЛЕ ЗОРУ (FOV): ${hud.fov.toInt()}°",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CS2Gold
                            )
                            Text("Збільшує огляд карти Dust 2 та периферійний зір", fontSize = 10.sp, color = CS2TextSecondary)
                            Slider(
                                value = hud.fov,
                                onValueChange = { gameEngine.hudSettings = hud.copy(fov = it) },
                                valueRange = 60f..110f,
                                modifier = Modifier.testTag("fov_slider"),
                                colors = SliderDefaults.colors(thumbColor = CS2Gold, activeTrackColor = CS2Gold)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Camera Sensitivity
                            Text(
                                text = "ЧУТЛИВІСТЬ ОГЛЯДУ (SENSITIVITY): ${String.format("%.2f", hud.sensitivity)}x",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CS2Gold
                            )
                            Text("Швидкість обертання камери при свайпі по екрану", fontSize = 10.sp, color = CS2TextSecondary)
                            Slider(
                                value = hud.sensitivity,
                                onValueChange = { gameEngine.hudSettings = hud.copy(sensitivity = it) },
                                valueRange = 0.2f..3.0f,
                                modifier = Modifier.testTag("sensitivity_slider"),
                                colors = SliderDefaults.colors(thumbColor = CS2Cyan, activeTrackColor = CS2Cyan)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Master Sound
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ЗВУКИ ПОСТРІЛІВ ТА С4", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("Автентичні звуки пострілів АК-47, AWP, ножа та бомби", fontSize = 10.sp, color = CS2TextSecondary)
                                }
                                Switch(
                                    checked = CS2SoundManager.soundEnabled,
                                    onCheckedChange = { CS2SoundManager.soundEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CS2Gold, checkedTrackColor = Color(0x44DE9B35))
                                )
                            }
                        }

                        "BUTTONS" -> {
                            // User requested: "і додай налоштованя з зміни пелу і зміну кнопок"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ЗМІНА ПОЗИЦІЇ ТА РОЗМІРУ КНОПОК УПРАВЛІННЯ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CS2Gold)
                                Button(
                                    onClick = { gameEngine.hudSettings = HudSettings() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFFFFF)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Скинути за замовчуванням", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Joystick Customization
                            Text("1. Лівий Джойстик руху", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CS2Cyan)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Позиція X: ${hud.joystickXPercent.toInt()}%", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.joystickXPercent,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(joystickXPercent = it) },
                                        valueRange = 8f..40f
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Позиція Y: ${hud.joystickYPercent.toInt()}%", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.joystickYPercent,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(joystickYPercent = it) },
                                        valueRange = 50f..90f
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Розмір: ${String.format("%.1f", hud.joystickScale)}x", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.joystickScale,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(joystickScale = it) },
                                        valueRange = 0.7f..1.5f
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fire Button Customization
                            Text("2. Кнопка стрільби (FIRE)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CS2Red)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Позиція X: ${hud.fireButtonXPercent.toInt()}%", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.fireButtonXPercent,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(fireButtonXPercent = it) },
                                        valueRange = 60f..95f
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Позиція Y: ${hud.fireButtonYPercent.toInt()}%", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.fireButtonYPercent,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(fireButtonYPercent = it) },
                                        valueRange = 40f..90f
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Розмір: ${String.format("%.1f", hud.fireButtonScale)}x", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.fireButtonScale,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(fireButtonScale = it) },
                                        valueRange = 0.8f..1.8f
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Jump & Crouch Buttons
                            Text("3. Кнопки стрибка (JUMP) та присідання (DUCK)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Стрибок Y: ${hud.jumpButtonYPercent.toInt()}%", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.jumpButtonYPercent,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(jumpButtonYPercent = it) },
                                        valueRange = 30f..80f
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Присідання Y: ${hud.crouchButtonYPercent.toInt()}%", fontSize = 10.sp, color = CS2TextSecondary)
                                    Slider(
                                        value = hud.crouchButtonYPercent,
                                        onValueChange = { gameEngine.hudSettings = hud.copy(crouchButtonYPercent = it) },
                                        valueRange = 50f..95f
                                    )
                                }
                            }
                        }

                        "CROSSHAIR" -> {
                            Text("НАЛАШТУВАННЯ ТАКТИЧНОГО ПРИЦІЛУ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CS2Gold)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Crosshair Color
                            Text("Колір прицілу", fontSize = 11.sp, color = CS2TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                val colors = listOf(Color(0xFF00FF66), Color(0xFF00E5FF), Color(0xFFFF1744), Color(0xFFFFD700), Color.White)
                                colors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (hud.crosshairColor == color) 3.dp else 1.dp,
                                                color = if (hud.crosshairColor == color) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { gameEngine.hudSettings = hud.copy(crosshairColor = color) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Розмір ліній: ${hud.crosshairSize.toInt()}px", fontSize = 11.sp, color = CS2TextSecondary)
                            Slider(
                                value = hud.crosshairSize,
                                onValueChange = { gameEngine.hudSettings = hud.copy(crosshairSize = it) },
                                valueRange = 6f..26f
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Зазор прицілу (Gap): ${hud.crosshairGap.toInt()}px", fontSize = 11.sp, color = CS2TextSecondary)
                            Slider(
                                value = hud.crosshairGap,
                                onValueChange = { gameEngine.hudSettings = hud.copy(crosshairGap = it) },
                                valueRange = 2f..16f
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Точка в центрі (Center Dot)", fontSize = 12.sp, color = Color.White)
                                Switch(
                                    checked = hud.crosshairDot,
                                    onCheckedChange = { gameEngine.hudSettings = hud.copy(crosshairDot = it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSubTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0x44DE9B35) else Color.Transparent)
            .border(1.dp, if (selected) CS2Gold else Color.Transparent, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) CS2Gold else CS2TextSecondary
        )
    }
}
