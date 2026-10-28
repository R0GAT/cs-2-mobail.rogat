package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.CS2GameEngine
import com.example.ui.theme.CS2Background
import com.example.ui.theme.CS2Border
import com.example.ui.theme.CS2Cyan
import com.example.ui.theme.CS2Gold
import com.example.ui.theme.CS2Red
import com.example.ui.theme.CS2TextPrimary
import com.example.ui.theme.CS2TextSecondary

@Composable
fun CS2CheatsScreen(
    gameEngine: CS2GameEngine,
    onBack: () -> Unit
) {
    val cheats = gameEngine.cheatSettings

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
                    .border(1.dp, CS2Red, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = CS2Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CS2 ЧІТ-МЕНЮ (INTERNAL MOD MENU)",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = CS2Red
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0x44EF4444), RoundedCornerShape(6.dp))
                        .border(1.dp, CS2Red, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("VAC BYPASS: ACTIVE", fontSize = 10.sp, color = CS2Red, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Cheat Sections
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column: Aimbot & Bhop
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD141A24)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CS2Border),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("1. АІМБОТ (AIMBOT & TRIGGERBOT)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CS2Red)
                        Spacer(modifier = Modifier.height(6.dp))

                        CheatSwitch(
                            title = "Увімкнути Аімбот (Aimbot)",
                            subtitle = "Автоматичне прицілювання у ворога",
                            checked = cheats.aimbotEnabled,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(aimbotEnabled = it) }
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Радіус FOV Аіма: ${cheats.aimbotFov.toInt()}°", fontSize = 11.sp, color = CS2TextSecondary)
                        Slider(
                            value = cheats.aimbotFov,
                            onValueChange = { gameEngine.cheatSettings = cheats.copy(aimbotFov = it) },
                            valueRange = 15f..120f,
                            colors = SliderDefaults.colors(thumbColor = CS2Red, activeTrackColor = CS2Red)
                        )

                        CheatSwitch(
                            title = "Ціль: Тільки в Голову (Headshot)",
                            subtitle = "Наводити приціл строго в голову для миттєвого вбивства",
                            checked = cheats.aimbotTargetHead,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(aimbotTargetHead = it) }
                        )

                        CheatSwitch(
                            title = "Автопостріл (Triggerbot)",
                            subtitle = "Автоматично відкриває вогонь при наведенні",
                            checked = cheats.triggerbotAutoFire,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(triggerbotAutoFire = it) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("2. БЕХОБ (AUTO BUNNYHOP)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CS2Cyan)
                        Spacer(modifier = Modifier.height(6.dp))

                        CheatSwitch(
                            title = "Автоматичний Бехоб (Bhop)",
                            subtitle = "Затисніть кнопку стрибка для нескінченного прискорення",
                            checked = cheats.bhopEnabled,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(bhopEnabled = it) }
                        )

                        CheatSwitch(
                            title = "NoClip (Політ крізь стіни)",
                            subtitle = "Дозволяє проходити крізь текстури карти Dust II",
                            checked = cheats.noClip,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(noClip = it) }
                        )
                    }
                }

                // Right Column: Wallhack ESP & Skin Hack
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD141A24)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CS2Border),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("3. ВАЛХАК / ESP (БАЧИТИ КРІЗЬ СТІНИ)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CS2Gold)
                        Spacer(modifier = Modifier.height(6.dp))

                        CheatSwitch(
                            title = "Головний перемикач ESP",
                            subtitle = "Візуалізація ворогів за стінами Dust 2",
                            checked = cheats.wallhackEsp,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(wallhackEsp = it) }
                        )

                        CheatSwitch(
                            title = "3D Рамки (Box ESP)",
                            subtitle = "Квадратні маркери навколо ворогів",
                            checked = cheats.wallhackBoxes,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(wallhackBoxes = it) }
                        )

                        CheatSwitch(
                            title = "Промені до ворогів (Snaplines)",
                            subtitle = "Лінії від прицілу до кожного супротивника",
                            checked = cheats.wallhackSnaplines,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(wallhackSnaplines = it) }
                        )

                        CheatSwitch(
                            title = "Смуга здоров'я (Health Bar)",
                            subtitle = "Показувати точну кількість HP ворогів",
                            checked = cheats.wallhackHealth,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(wallhackHealth = it) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("4. ПАТРОНИ ТА СКІН ХАК", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00FF66))
                        Spacer(modifier = Modifier.height(6.dp))

                        CheatSwitch(
                            title = "Нескінченні патрони (Infinite Ammo)",
                            subtitle = "Патрони в обоймі ніколи не закінчуються, без перезарядки",
                            checked = cheats.infiniteAmmo,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(infiniteAmmo = it) }
                        )

                        CheatSwitch(
                            title = "Скін Хак (Unlock All Skins)",
                            subtitle = "Миттєве розблокування всіх ножів та скінів в інвентарі",
                            checked = cheats.skinHackUnlocked,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(skinHackUnlocked = it) }
                        )

                        CheatSwitch(
                            title = "Каркасний режим (Wireframe)",
                            subtitle = "Відображати 3D полігони карти сіткою",
                            checked = cheats.wireframe,
                            onChecked = { gameEngine.cheatSettings = cheats.copy(wireframe = it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CheatSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (checked) CS2Cyan else Color.White)
            Text(subtitle, fontSize = 9.sp, color = CS2TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CS2Cyan,
                checkedTrackColor = Color(0x4400E5FF)
            )
        )
    }
}
