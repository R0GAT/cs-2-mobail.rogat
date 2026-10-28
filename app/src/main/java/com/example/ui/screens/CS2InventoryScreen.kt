package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CS2SoundManager
import com.example.logic.CS2GameEngine
import com.example.model.CS2SkinCatalog
import com.example.model.KnifeType
import com.example.model.SkinRarity
import com.example.model.WeaponSkin
import com.example.model.WeaponType
import com.example.ui.theme.CS2Background
import com.example.ui.theme.CS2Border
import com.example.ui.theme.CS2CardBg
import com.example.ui.theme.CS2Cyan
import com.example.ui.theme.CS2Gold
import com.example.ui.theme.CS2GoldRare
import com.example.ui.theme.CS2Green
import com.example.ui.theme.CS2Surface
import com.example.ui.theme.CS2TextPrimary
import com.example.ui.theme.CS2TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CS2InventoryScreen(
    gameEngine: CS2GameEngine,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("KNIVES") }
    var inspectedSkin by remember { mutableStateOf(CS2SkinCatalog.KNIVES[0]) }

    val skins = when (selectedCategory) {
        "KNIVES" -> CS2SkinCatalog.KNIVES
        "RIFLES" -> CS2SkinCatalog.RIFLES
        "SNIPERS" -> CS2SkinCatalog.SNIPERS
        "PISTOLS" -> CS2SkinCatalog.PISTOLS
        else -> CS2SkinCatalog.ALL_SKINS
    }

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
                        text = "ІНВЕНТАР ТА СКІНИ (INVENTORY)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CS2Gold
                    )
                }

                // Category Tabs
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CategoryTab("НОЖІ (KNIVES)", selectedCategory == "KNIVES") { selectedCategory = "KNIVES" }
                    CategoryTab("ГВИНТІВКИ", selectedCategory == "RIFLES") { selectedCategory = "RIFLES" }
                    CategoryTab("СНАЙПЕРКИ (AWP)", selectedCategory == "SNIPERS") { selectedCategory = "SNIPERS" }
                    CategoryTab("ПІСТОЛЕТИ", selectedCategory == "PISTOLS") { selectedCategory = "PISTOLS" }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Split: Grid on left, 3D Interactive Inspect on right
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left: Skins Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1.3f)
                        .background(Color(0x88141A24), RoundedCornerShape(10.dp))
                        .border(1.dp, CS2Border, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(skins) { skin ->
                        val isInspected = inspectedSkin.id == skin.id
                        val isEquipped = isSkinEquipped(skin, gameEngine)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clickable {
                                    inspectedSkin = skin
                                    CS2SoundManager.playKnifeSlash()
                                }
                                .testTag("skin_item_${skin.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isInspected) Color(0x55DE9B35) else Color(0x661A2230)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isInspected) 2.dp else 1.dp,
                                color = if (isInspected) CS2Gold else skin.rarity.color
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(skin.rarity.color, RoundedCornerShape(2.dp))
                                    )
                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0x4400FF66), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("EQUIPPED", fontSize = 8.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Text(
                                    text = skin.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    maxLines = 2
                                )

                                Text(
                                    text = skin.rarity.title.take(18),
                                    fontSize = 9.sp,
                                    color = skin.rarity.color,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Right: 3D Animated Inspect Showcase Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xDD141A24), RoundedCornerShape(10.dp))
                        .border(1.5.dp, inspectedSkin.rarity.color, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD141A24)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title & Rarity
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = inspectedSkin.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = inspectedSkin.rarity.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = inspectedSkin.rarity.color
                            )
                            Text(
                                text = inspectedSkin.wear,
                                fontSize = 10.sp,
                                color = CS2TextSecondary
                            )
                        }

                        // 3D Animated Rotating Inspect Preview
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedInspectModel(skin = inspectedSkin)
                        }

                        // Skin Description & Lore
                        Text(
                            text = inspectedSkin.description,
                            fontSize = 11.sp,
                            color = CS2TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Equip Button
                        val isEquipped = isSkinEquipped(inspectedSkin, gameEngine)
                        Button(
                            onClick = {
                                equipSkin(inspectedSkin, gameEngine)
                                CS2SoundManager.playKnifeSlash()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("equip_skin_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEquipped) Color(0x4400FF66) else CS2Gold
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEquipped) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("СПОРЯДЖЕНО (EQUIPPED)", fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                                } else {
                                    Text("СПОРЯДИТИ (EQUIP)", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTab(
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

@Composable
fun AnimatedInspectModel(skin: WeaponSkin) {
    val infiniteTransition = rememberInfiniteTransition(label = "inspect_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rad = Math.toRadians(angle.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        if (skin.weaponType == WeaponType.KNIFE) {
            // Rotating 3D Karambit / Butterfly Blade
            val bladePath = Path().apply {
                if (skin.knifeType == KnifeType.KARAMBIT) {
                    moveTo(cx, cy + 30f)
                    cubicTo(
                        cx + 35f * cosA, cy - 10f,
                        cx - 40f * cosA, cy - 60f,
                        cx - 70f * cosA, cy - 40f
                    )
                    cubicTo(
                        cx - 30f * cosA, cy - 30f,
                        cx + 10f * cosA, cy - 10f,
                        cx + 15f, cy + 30f
                    )
                    close()
                } else {
                    moveTo(cx - 10f * cosA, cy + 35f)
                    lineTo(cx, cy - 65f)
                    lineTo(cx + 25f * cosA, cy - 50f)
                    lineTo(cx + 10f * cosA, cy + 35f)
                    close()
                }
            }
            drawPath(bladePath, skin.primaryColor)
            drawPath(bladePath, skin.accentColor, style = Stroke(width = 2.5f))
            // Ring / handle
            drawCircle(skin.secondaryColor, radius = 14f, center = Offset(cx, cy + 42f))
        } else {
            // Rotating Rifle / Gun Body
            val w = 90f * cosA
            val h = 32f
            drawRoundRect(
                color = skin.primaryColor,
                topLeft = Offset(cx - w / 2f, cy - h / 2f),
                size = androidx.compose.ui.geometry.Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Barrel
            drawRect(
                color = skin.secondaryColor,
                topLeft = Offset(cx - (w / 2f) - (30f * cosA), cy - 6f),
                size = androidx.compose.ui.geometry.Size(30f * cosA, 10f)
            )
            // Accent stripe
            drawLine(skin.accentColor, Offset(cx - w / 2f, cy), Offset(cx + w / 2f, cy), strokeWidth = 3f)
        }
    }
}

private fun isSkinEquipped(skin: WeaponSkin, gameEngine: CS2GameEngine): Boolean {
    return when (skin.weaponType) {
        WeaponType.KNIFE -> gameEngine.equippedKnifeSkin.id == skin.id
        WeaponType.AK47 -> gameEngine.equippedAkSkin.id == skin.id
        WeaponType.AWP -> gameEngine.equippedAwpSkin.id == skin.id
        WeaponType.M4A4 -> gameEngine.equippedM4Skin.id == skin.id
        WeaponType.DEAGLE -> gameEngine.equippedDeagleSkin.id == skin.id
        else -> false
    }
}

private fun equipSkin(skin: WeaponSkin, gameEngine: CS2GameEngine) {
    when (skin.weaponType) {
        WeaponType.KNIFE -> gameEngine.equippedKnifeSkin = skin
        WeaponType.AK47 -> gameEngine.equippedAkSkin = skin
        WeaponType.AWP -> gameEngine.equippedAwpSkin = skin
        WeaponType.M4A4 -> gameEngine.equippedM4Skin = skin
        WeaponType.DEAGLE -> gameEngine.equippedDeagleSkin = skin
        else -> {}
    }
}
