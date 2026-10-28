package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.logic.CS2GameEngine
import com.example.model.GameMode
import com.example.model.Team
import com.example.ui.theme.CS2Background
import com.example.ui.theme.CS2Border
import com.example.ui.theme.CS2CardBg
import com.example.ui.theme.CS2CtBlue
import com.example.ui.theme.CS2Cyan
import com.example.ui.theme.CS2Gold
import com.example.ui.theme.CS2Red
import com.example.ui.theme.CS2Surface
import com.example.ui.theme.CS2TAmber
import com.example.ui.theme.CS2TextPrimary
import com.example.ui.theme.CS2TextSecondary

@Composable
fun CS2LobbyScreen(
    gameEngine: CS2GameEngine,
    onStartGame: () -> Unit,
    onNavigateInventory: () -> Unit,
    onNavigateCases: () -> Unit,
    onNavigateCheats: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CS2Background)
    ) {
        // Dust 2 Cinematic Background
        Image(
            painter = painterResource(id = R.drawable.cs2_lobby_bg),
            contentDescription = "Dust 2 Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xEE0B0E14),
                            Color(0xCC0B0E14),
                            Color(0x880B0E14)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // TOP CS2 NAVIGATION BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0x99141A24), RoundedCornerShape(8.dp))
                    .border(1.dp, CS2Border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CS2 Logo & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Brush.linearGradient(listOf(CS2Gold, Color(0xFFFF6600))), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CS2",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MOBILE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CS2Cyan,
                        letterSpacing = 2.sp
                    )
                }

                // Main Nav Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTab(
                        title = "ГРАТИ",
                        selected = true,
                        onClick = { }
                    )
                    NavTab(
                        title = "ІНВЕНТАР",
                        selected = false,
                        onClick = onNavigateInventory
                    )
                    NavTab(
                        title = "КЕЙСИ",
                        selected = false,
                        onClick = onNavigateCases
                    )
                    NavTab(
                        title = "ЧІТИ (HACKS)",
                        selected = false,
                        accentColor = CS2Red,
                        onClick = onNavigateCheats
                    )
                    NavTab(
                        title = "НАЛАШТУВАННЯ",
                        selected = false,
                        onClick = onNavigateSettings
                    )
                }

                // Player Profile Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0x55000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(CS2Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rank",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Premier 24,530 ★ Global",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CS2Gold
                        )
                        Text(
                            text = "Баланс: $${gameEngine.playerMoney}",
                            fontSize = 10.sp,
                            color = CS2Cyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // MAIN LOBBY CONTENT AREA
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT COLUMN: Game Mode & Team Selection
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .background(Color(0xDD141A24), RoundedCornerShape(10.dp))
                        .border(1.dp, CS2Border, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "РЕЖИМ ГРИ (GAME MODE)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CS2TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Cards
                    GameMode.values().forEach { mode ->
                        val isSelected = gameEngine.gameMode == mode
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { gameEngine.gameMode = mode }
                                .testTag("mode_${mode.name.lowercase()}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0x33DE9B35) else Color(0x441A2230)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CS2Gold else CS2Border
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (isSelected) CS2Gold else Color.Gray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = mode.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CS2Gold else CS2TextPrimary
                                    )
                                    Text(
                                        text = mode.subtitle,
                                        fontSize = 10.sp,
                                        color = CS2TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // TEAM SELECTOR (CT vs T)
                    Text(
                        text = "ВИБІР КОМАНДИ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CS2TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { gameEngine.playerTeam = Team.CT },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("select_team_ct"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gameEngine.playerTeam == Team.CT) CS2CtBlue else Color(0x441A2230)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "СПЕЦНАЗ (CT)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = { gameEngine.playerTeam = Team.T },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("select_team_t"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gameEngine.playerTeam == Team.T) CS2TAmber else Color(0x441A2230)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ТЕРОРИСТИ (T)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // RIGHT COLUMN: Quick Cheats Panel & Equipped Loadout
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .background(Color(0xDD141A24), RoundedCornerShape(10.dp))
                        .border(1.dp, CS2Border, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ШВИДКІ ЧІТИ (CS2 HACKS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CS2Red
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0x44EF4444), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("STATUS: UNDETECTED", fontSize = 9.sp, color = CS2Red, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickCheatToggle(
                        title = "Бехоб (Auto Bunnyhop)",
                        subtitle = "Розгін швидкості при затисканні стрибка",
                        checked = gameEngine.cheatSettings.bhopEnabled,
                        onCheckedChange = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(bhopEnabled = it) }
                    )
                    QuickCheatToggle(
                        title = "Аімбот (Aimbot Headlock)",
                        subtitle = "Автонаведення в голову ворога",
                        checked = gameEngine.cheatSettings.aimbotEnabled,
                        onCheckedChange = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(aimbotEnabled = it) }
                    )
                    QuickCheatToggle(
                        title = "Бачити крізь стіни (Wallhack ESP)",
                        subtitle = "3D рамки ворогів, дистанція та HP",
                        checked = gameEngine.cheatSettings.wallhackEsp,
                        onCheckedChange = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(wallhackEsp = it) }
                    )
                    QuickCheatToggle(
                        title = "Нескінченні патрони (Infinite Ammo)",
                        subtitle = "Патрони та гранати не закінчуються",
                        checked = gameEngine.cheatSettings.infiniteAmmo,
                        onCheckedChange = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(infiniteAmmo = it) }
                    )
                    QuickCheatToggle(
                        title = "Скін Хак (Skin Changer)",
                        subtitle = "Розблокувати всі ножі та скіни",
                        checked = gameEngine.cheatSettings.skinHackUnlocked,
                        onCheckedChange = { gameEngine.cheatSettings = gameEngine.cheatSettings.copy(skinHackUnlocked = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BOTTOM BAR: MATCH LAUNCH & MAP INFO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xEE141A24), RoundedCornerShape(8.dp))
                    .border(1.dp, CS2Border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Map Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x33DE9B35), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("КАРТА", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CS2Gold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("de_dust2 (Dust II 3D)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CS2TextPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Пінг: 18ms", fontSize = 11.sp, color = Color(0xFF00FF66))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Боти: 5v5 Tactical AI", fontSize = 11.sp, color = CS2TextSecondary)
                }

                // Big Green CS2 GO Button
                Button(
                    onClick = {
                        gameEngine.resetMatch()
                        onStartGame()
                    },
                    modifier = Modifier
                        .height(44.dp)
                        .width(180.dp)
                        .testTag("start_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ПОЧАТИ ГРУ",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavTab(
    title: String,
    selected: Boolean,
    accentColor: Color = CS2Gold,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0x33DE9B35) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accentColor else CS2TextSecondary
        )
    }
}

@Composable
fun QuickCheatToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (checked) CS2Cyan else CS2TextPrimary)
            Text(subtitle, fontSize = 9.sp, color = CS2TextSecondary, maxLines = 1)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = 38.dp, height = 24.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = CS2Cyan,
                checkedTrackColor = Color(0x4400E5FF),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}
