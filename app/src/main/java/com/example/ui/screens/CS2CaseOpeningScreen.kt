package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.CS2SoundManager
import com.example.logic.CS2GameEngine
import com.example.model.CS2Case
import com.example.model.CS2SkinCatalog
import com.example.model.SkinRarity
import com.example.model.WeaponSkin
import com.example.model.WeaponType
import com.example.ui.theme.CS2Background
import com.example.ui.theme.CS2Border
import com.example.ui.theme.CS2Cyan
import com.example.ui.theme.CS2Gold
import com.example.ui.theme.CS2GoldRare
import com.example.ui.theme.CS2TextPrimary
import com.example.ui.theme.CS2TextSecondary
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CS2CaseOpeningScreen(
    gameEngine: CS2GameEngine,
    onBack: () -> Unit
) {
    var selectedCase by remember { mutableStateOf(CS2SkinCatalog.CASES[0]) }
    var isSpinning by remember { mutableStateOf(false) }
    var winningSkin by remember { mutableStateOf<WeaponSkin?>(null) }
    var showWinningDialog by remember { mutableStateOf(false) }

    // Horizontal reel items (generate 45 items)
    val reelItems = remember(selectedCase) {
        val list = mutableListOf<WeaponSkin>()
        for (i in 0 until 45) {
            val isGoldChance = Random.nextInt(100) < 12
            if (isGoldChance) {
                list.add(CS2SkinCatalog.KNIVES.random())
            } else {
                list.add(selectedCase.items.random())
            }
        }
        list
    }

    val reelOffsetAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

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
                        text = "ВІДКРИТТЯ КЕЙСІВ CS2 (CASE OPENING)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CS2Gold
                    )
                }

                Text(
                    text = "Баланс: $${gameEngine.playerMoney}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CS2Cyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Case Selection Carousel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CS2SkinCatalog.CASES.forEach { cs2Case ->
                    val isSelected = selectedCase.id == cs2Case.id
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .clickable(enabled = !isSpinning) {
                                selectedCase = cs2Case
                            }
                            .testTag("select_case_${cs2Case.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0x55DE9B35) else Color(0x661A2230)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CS2Gold else CS2Border
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = cs2Case.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ціна: $${cs2Case.price}",
                                fontSize = 10.sp,
                                color = cs2Case.badgeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // REEL SPINNING VIEWFINDER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD10141D)),
                border = androidx.compose.foundation.BorderStroke(2.dp, CS2Gold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    // Scrolling Reel Strip
                    Row(
                        modifier = Modifier
                            .offset { IntOffset(-reelOffsetAnim.value.toInt(), 0) }
                            .padding(horizontal = 250.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        reelItems.forEachIndexed { index, skin ->
                            ReelItemCard(skin)
                        }
                    }

                    // CS2 Center Selector Needle (The gold/red vertical pointer line)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        // Vertical needle
                        drawLine(
                            color = CS2Gold,
                            start = Offset(cx, 0f),
                            end = Offset(cx, size.height),
                            strokeWidth = 3f
                        )
                        // Top triangle
                        drawCircle(CS2Gold, radius = 6f, center = Offset(cx, 8f))
                        // Bottom triangle
                        drawCircle(CS2Gold, radius = 6f, center = Offset(cx, size.height - 8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spin Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (!isSpinning) {
                            isSpinning = true
                            scope.launch {
                                // Reset position
                                reelOffsetAnim.snapTo(0f)
                                val itemWidthPx = 320f
                                val targetItemIndex = 32
                                val jitter = Random.nextFloat() * 80f - 40f
                                val targetDistance = (targetItemIndex * itemWidthPx) + jitter

                                // Tick sound periodically during spin
                                launch {
                                    for (step in 0 until 35) {
                                        CS2SoundManager.playCaseTick()
                                        kotlinx.coroutines.delay((50 + step * 14).toLong())
                                    }
                                }

                                reelOffsetAnim.animateTo(
                                    targetValue = targetDistance,
                                    animationSpec = tween(durationMillis = 5500, easing = FastOutSlowInEasing)
                                )

                                val won = reelItems[targetItemIndex]
                                winningSkin = won
                                isSpinning = false
                                showWinningDialog = true

                                if (won.rarity == SkinRarity.EXTRAORDINARY) {
                                    CS2SoundManager.playCaseGoldFanfare()
                                } else {
                                    CS2SoundManager.playKnifeSlash()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .width(260.dp)
                        .testTag("open_case_button"),
                    enabled = !isSpinning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CS2Gold
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isSpinning) "ВІДКРИВАЄТЬСЯ..." else "ВІДКРИТИ КЕЙС ($${selectedCase.price})",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // Winning Item Dialog Celebration
        if (showWinningDialog && winningSkin != null) {
            val won = winningSkin!!
            Dialog(onDismissRequest = { showWinningDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFA141A24)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, won.rarity.color),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (won.rarity == SkinRarity.EXTRAORDINARY) "★ ВІТАЄМО! РІДКІСНИЙ НІЖ! ★" else "НОВИЙ ПРЕДМЕТ В ІНВЕНТАРІ!",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = won.rarity.color
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedInspectModel(skin = won)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(won.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(won.rarity.title, fontSize = 11.sp, color = won.rarity.color)
                        Text(won.wear, fontSize = 10.sp, color = CS2TextSecondary)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Equip won skin
                                    when (won.weaponType) {
                                        WeaponType.KNIFE -> gameEngine.equippedKnifeSkin = won
                                        WeaponType.AK47 -> gameEngine.equippedAkSkin = won
                                        WeaponType.AWP -> gameEngine.equippedAwpSkin = won
                                        WeaponType.M4A4 -> gameEngine.equippedM4Skin = won
                                        WeaponType.DEAGLE -> gameEngine.equippedDeagleSkin = won
                                        else -> {}
                                    }
                                    showWinningDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CS2Gold)
                            ) {
                                Text("СПОРЯДИТИ (EQUIP)", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showWinningDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFFFFF))
                            ) {
                                Text("ЗАКРИТИ", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReelItemCard(skin: WeaponSkin) {
    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 115.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF19202C))
            .border(2.dp, skin.rarity.color, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 30.dp)
                    .background(skin.primaryColor, RoundedCornerShape(4.dp))
            )
            Text(
                text = skin.name,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                maxLines = 2
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(skin.rarity.color)
            )
        }
    }
}
