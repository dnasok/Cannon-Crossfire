package com.baabearianstudios.cannoncrossfire.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baabearianstudios.cannoncrossfire.GameState
import com.baabearianstudios.cannoncrossfire.GameViewModel
import com.baabearianstudios.cannoncrossfire.R
import com.baabearianstudios.cannoncrossfire.ui.components.GameArenaCanvas
import com.baabearianstudios.cannoncrossfire.ui.components.MenuCannonballBackground
import com.baabearianstudios.cannoncrossfire.ui.components.WoodenGameButton
import com.baabearianstudios.cannoncrossfire.ui.screens.GameOverOverlay
import com.baabearianstudios.cannoncrossfire.ui.screens.PauseMenuOverlay
import com.baabearianstudios.cannoncrossfire.ui.screens.StartMenuOverlay
import com.baabearianstudios.cannoncrossfire.ui.theme.AmberBright
import com.baabearianstudios.cannoncrossfire.ui.theme.AmberGold
import com.baabearianstudios.cannoncrossfire.ui.theme.CrimsonDanger

@Composable
fun CannonCrossfireApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Drive real-time 60-120fps physics synced to display vsync while Running
    LaunchedEffect(uiState.gameState) {
        if (uiState.gameState is GameState.Running) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { nanos ->
                    if (lastNanos == 0L) {
                        lastNanos = nanos
                        return@withFrameNanos
                    }
                    val dtMs = ((nanos - lastNanos) / 1_000_000L).coerceIn(1L, 50L)
                    lastNanos = nanos
                    viewModel.stepFrame(dtMs)
                }
            }
        }
    }

    // Handle system Back gesture gracefully across game states
    BackHandler(enabled = uiState.gameState !is GameState.Ready) {
        when (uiState.gameState) {
            is GameState.WaitingForFlat -> viewModel.quitToMenu()
            is GameState.Running, is GameState.Countdown -> viewModel.pauseGame()
            is GameState.Paused -> viewModel.resumeGame()
            is GameState.GameOver -> viewModel.quitToMenu()
            is GameState.Ready -> Unit
        }
    }

    val backgroundRes = when (uiState.gameState) {
        is GameState.Ready -> R.drawable.menu_background
        is GameState.GameOver -> R.drawable.gameover_background
        is GameState.Paused -> R.drawable.pause_background
        is GameState.WaitingForFlat, is GameState.Countdown, is GameState.Running -> R.drawable.game_background
    }

    val scrimAlpha = when (uiState.gameState) {
        is GameState.Ready, is GameState.GameOver -> 0.40f
        is GameState.WaitingForFlat -> 0.35f
        else -> 0.15f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Screen Background Artwork
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Contrast Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
        )

        // 3. Active Gameplay Arena OR Menu Background Cannonballs
        if (uiState.gameState is GameState.Ready) {
            MenuCannonballBackground()
            StartMenuOverlay(
                highScore = uiState.highScore,
                onStartClick = { viewModel.startGame() },
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
            )
        } else if (uiState.gameState is GameState.WaitingForFlat ||
            uiState.gameState is GameState.Running ||
            uiState.gameState is GameState.Countdown ||
            uiState.gameState is GameState.Paused
        ) {
            GameArenaCanvas(
                uiState = uiState,
                onArenaSizeChanged = { w, h -> viewModel.updateArenaSize(w, h) }
            )
        }

        // 4. Top Safe-Area HUD Bar (Mute, Score, Pause — no Center Tilt pill)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Left: Mute Toggle
            Surface(
                modifier = Modifier.align(Alignment.CenterStart),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                shadowElevation = 6.dp
            ) {
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .testTag("mute_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) {
                            Icons.AutoMirrored.Filled.VolumeOff
                        } else {
                            Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = stringResource(
                            id = if (uiState.isMuted) R.string.unmute_sound else R.string.mute_sound
                        ),
                        tint = if (uiState.isMuted) CrimsonDanger else AmberGold
                    )
                }
            }

            // Center: Live Score Pill
            if (uiState.gameState is GameState.Running ||
                uiState.gameState is GameState.Countdown ||
                uiState.gameState is GameState.Paused
            ) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.88f),
                    border = BorderStroke(1.5.dp, AmberGold.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = stringResource(id = R.string.score, uiState.score),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            color = Color(0xFF18181B)
                        ),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }
            }

            // Right: Pause Button
            if (uiState.gameState is GameState.Running) {
                Surface(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                    shadowElevation = 6.dp
                ) {
                    IconButton(
                        onClick = { viewModel.pauseGame() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = stringResource(id = R.string.pause_game),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 5. Waiting For Flat Starting Position Dialog
        if (uiState.gameState is GameState.WaitingForFlat) {
            FlatPositionDialog(
                tiltNormX = uiState.tiltNormX,
                tiltNormY = uiState.tiltNormY,
                isFlat = uiState.isPhoneFlat,
                onCancel = { viewModel.quitToMenu() },
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
            )
        }

        // 6. Countdown Overlay (3..2..1)
        val countdownState = uiState.gameState as? GameState.Countdown
        if (countdownState != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdownState.count.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 112.sp,
                        color = Color(0xFF27272A),
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.85f),
                            offset = Offset(0f, 0f),
                            blurRadius = 24f
                        )
                    )
                )
            }
        }

        // 7. Pause Menu Overlay
        if (uiState.gameState is GameState.Paused) {
            PauseMenuOverlay(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.startGame() },
                onQuit = { viewModel.quitToMenu() },
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
            )
        }

        // 8. Game Over Overlay
        val gameOverState = uiState.gameState as? GameState.GameOver
        if (gameOverState != null) {
            GameOverOverlay(
                score = gameOverState.score,
                highScore = uiState.highScore,
                isNewHighScore = gameOverState.isNewHighScore,
                leaderboard = uiState.leaderboard,
                onRestartClick = { viewModel.quitToMenu() },
                onSubmitScore = { name -> viewModel.submitScore(name) },
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun FlatPositionDialog(
    tiltNormX: Float,
    tiltNormY: Float,
    isFlat: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .testTag("flat_position_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.82f),
            border = BorderStroke(1.5.dp, AmberGold.copy(alpha = 0.55f)),
            shadowElevation = 20.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.hold_flat_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        color = AmberGold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.hold_flat_message),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.92f)
                    ),
                    textAlign = TextAlign.Center
                )

                // Live 2D bubble-level target so user sees how to level phone flat
                Canvas(
                    modifier = Modifier
                        .size(116.dp)
                        .padding(4.dp)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension / 2f - 4.dp.toPx()
                    val targetRadius = outerRadius * 0.28f

                    drawCircle(
                        color = Color(0xFF18181B),
                        radius = outerRadius,
                        center = center
                    )
                    drawCircle(
                        color = AmberGold.copy(alpha = 0.45f),
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Center target ring
                    drawCircle(
                        color = if (isFlat) Color(0xFF22C55E) else AmberBright.copy(alpha = 0.7f),
                        radius = targetRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Moving tilt dot
                    val maxTravel = outerRadius - 10.dp.toPx()
                    val dotCenter = Offset(
                        x = (center.x + tiltNormX * maxTravel).coerceIn(
                            center.x - maxTravel,
                            center.x + maxTravel
                        ),
                        y = (center.y + tiltNormY * maxTravel).coerceIn(
                            center.y - maxTravel,
                            center.y + maxTravel
                        )
                    )
                    drawCircle(
                        color = if (isFlat) Color(0xFF22C55E) else AmberGold,
                        radius = 10.dp.toPx(),
                        center = dotCenter
                    )
                }

                WoodenGameButton(
                    text = stringResource(id = R.string.hold_flat_cancel),
                    onClick = onCancel,
                    testTag = "cancel_flat_dialog_button",
                    width = 160.dp,
                    height = 52.dp,
                    fontSize = 16.sp
                )
            }
        }
    }
}
