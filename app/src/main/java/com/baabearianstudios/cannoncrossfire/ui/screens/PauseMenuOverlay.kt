package com.baabearianstudios.cannoncrossfire.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baabearianstudios.cannoncrossfire.R
import com.baabearianstudios.cannoncrossfire.ui.components.WoodenGameButton

@Composable
fun PauseMenuOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 340.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.paused),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 40.sp,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(3f, 5f),
                            blurRadius = 8f
                        )
                    )
                )

                WoodenGameButton(
                    text = stringResource(id = R.string.resume_game),
                    onClick = onResume,
                    testTag = "resume_button",
                    width = 196.dp,
                    height = 60.dp,
                    fontSize = 19.sp
                )

                WoodenGameButton(
                    text = stringResource(id = R.string.restart_game),
                    onClick = onRestart,
                    testTag = "restart_pause_button",
                    width = 196.dp,
                    height = 60.dp,
                    fontSize = 19.sp
                )

                WoodenGameButton(
                    text = stringResource(id = R.string.quit_to_menu),
                    onClick = onQuit,
                    testTag = "quit_button",
                    width = 196.dp,
                    height = 60.dp,
                    fontSize = 17.sp
                )
            }
        }
    }
}
