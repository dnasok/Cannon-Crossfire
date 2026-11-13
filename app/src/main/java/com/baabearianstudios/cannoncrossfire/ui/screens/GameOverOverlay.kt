package com.baabearianstudios.cannoncrossfire.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baabearianstudios.cannoncrossfire.OnlineScore
import com.baabearianstudios.cannoncrossfire.R
import com.baabearianstudios.cannoncrossfire.ui.components.WoodenGameButton
import com.baabearianstudios.cannoncrossfire.ui.theme.AmberBright
import com.baabearianstudios.cannoncrossfire.ui.theme.AmberGold
import com.baabearianstudios.cannoncrossfire.ui.theme.CrimsonDanger

@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    leaderboard: List<OnlineScore>,
    onRestartClick: () -> Unit,
    onSubmitScore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var playerName by remember { mutableStateOf("Player") }
    var submitted by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, CrimsonDanger.copy(alpha = 0.4f)),
            shadowElevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.game_over),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 44.sp,
                        color = CrimsonDanger,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(4f, 6f),
                            blurRadius = 10f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                if (leaderboard.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.local_high_score),
                            style = MaterialTheme.typography.titleLarge.copy(color = AmberGold)
                        )
                        Text(
                            text = highScore.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(color = Color.White)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF18181B).copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.top_5_players),
                                style = MaterialTheme.typography.titleLarge.copy(color = AmberGold)
                            )

                            leaderboard.forEachIndexed { idx, entry ->
                                if (idx > 0) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${entry.playerName}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            color = Color(0xFFE4E4E7)
                                        )
                                    )
                                    Text(
                                        text = entry.score.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 16.sp,
                                            color = AmberGold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White)) {
                                append(stringResource(id = R.string.your_score))
                                append(" ")
                            }
                            withStyle(SpanStyle(color = AmberGold)) {
                                append(score.toString())
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp)
                    )

                    if (isNewHighScore) {
                        Text(
                            text = stringResource(id = R.string.new_high_score),
                            style = MaterialTheme.typography.titleLarge.copy(color = AmberBright),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (isNewHighScore && !submitted) {
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { if (it.length <= 15) playerName = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.enter_your_name),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val trimmed = playerName.trim()
                                if (trimmed.isNotEmpty()) {
                                    onSubmitScore(trimmed)
                                    submitted = true
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = AmberGold.copy(alpha = 0.6f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.75f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.75f),
                            cursorColor = AmberGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_name_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                    ) {
                        WoodenGameButton(
                            text = stringResource(id = R.string.submit_score),
                            onClick = {
                                val trimmed = playerName.trim()
                                if (trimmed.isNotEmpty()) {
                                    onSubmitScore(trimmed)
                                    submitted = true
                                }
                            },
                            testTag = "submit_score_button",
                            width = 148.dp,
                            height = 54.dp,
                            fontSize = 14.sp
                        )
                        WoodenGameButton(
                            text = stringResource(id = R.string.back_to_menu),
                            onClick = onRestartClick,
                            testTag = "back_to_menu_button",
                            width = 148.dp,
                            height = 54.dp,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    WoodenGameButton(
                        text = stringResource(id = R.string.restart_full),
                        onClick = onRestartClick,
                        testTag = "restart_game_button",
                        width = 210.dp,
                        height = 64.dp,
                        fontSize = 19.sp
                    )
                }
            }
        }
    }
}
