package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.logic.CS2GameEngine
import com.example.ui.screens.CS2CaseOpeningScreen
import com.example.ui.screens.CS2CheatsScreen
import com.example.ui.screens.CS2GameScreen
import com.example.ui.screens.CS2InventoryScreen
import com.example.ui.screens.CS2LobbyScreen
import com.example.ui.screens.CS2SettingsScreen
import com.example.ui.theme.CS2Background
import com.example.ui.theme.MyApplicationTheme

enum class CS2Screen {
    LOBBY,
    GAME,
    INVENTORY,
    CASES,
    CHEATS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system bars for full-screen immersive CS2 gameplay
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CS2Background
                ) {
                    CS2App()
                }
            }
        }
    }
}

@Composable
fun CS2App() {
    val gameEngine = remember { CS2GameEngine() }
    var currentScreen by remember { mutableStateOf(CS2Screen.LOBBY) }

    // Android back button handling
    BackHandler(enabled = currentScreen != CS2Screen.LOBBY) {
        currentScreen = CS2Screen.LOBBY
    }

    when (currentScreen) {
        CS2Screen.LOBBY -> {
            CS2LobbyScreen(
                gameEngine = gameEngine,
                onStartGame = { currentScreen = CS2Screen.GAME },
                onNavigateInventory = { currentScreen = CS2Screen.INVENTORY },
                onNavigateCases = { currentScreen = CS2Screen.CASES },
                onNavigateCheats = { currentScreen = CS2Screen.CHEATS },
                onNavigateSettings = { currentScreen = CS2Screen.SETTINGS }
            )
        }
        CS2Screen.GAME -> {
            CS2GameScreen(
                gameEngine = gameEngine,
                onExitToLobby = { currentScreen = CS2Screen.LOBBY }
            )
        }
        CS2Screen.INVENTORY -> {
            CS2InventoryScreen(
                gameEngine = gameEngine,
                onBack = { currentScreen = CS2Screen.LOBBY }
            )
        }
        CS2Screen.CASES -> {
            CS2CaseOpeningScreen(
                gameEngine = gameEngine,
                onBack = { currentScreen = CS2Screen.LOBBY }
            )
        }
        CS2Screen.CHEATS -> {
            CS2CheatsScreen(
                gameEngine = gameEngine,
                onBack = { currentScreen = CS2Screen.LOBBY }
            )
        }
        CS2Screen.SETTINGS -> {
            CS2SettingsScreen(
                gameEngine = gameEngine,
                onBack = { currentScreen = CS2Screen.LOBBY }
            )
        }
    }
}
