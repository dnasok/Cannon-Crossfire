package com.baabearianstudios.cannoncrossfire.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baabearianstudios.cannoncrossfire.R
import com.baabearianstudios.cannoncrossfire.ui.components.WoodenGameButton
import com.baabearianstudios.cannoncrossfire.ui.theme.AmberBright
import com.baabearianstudios.cannoncrossfire.ui.theme.AmberGold

@Composable
fun StartMenuOverlay(
    highScore: Int,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 44.sp,
                color = AmberGold,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(4f, 6f),
                    blurRadius = 10f
                )
            ),
            textAlign = TextAlign.Center
        )

        if (highScore > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.high_score, highScore),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = AmberBright,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 4f),
                        blurRadius = 6f
                    )
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        WoodenGameButton(
            text = stringResource(id = R.string.start_game),
            onClick = onStartClick,
            testTag = "start_game_button",
            width = 240.dp,
            height = 72.dp,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.35f)),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.controls_title),
                    style = MaterialTheme.typography.titleMedium.copy(color = AmberBright)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.controls_tilt),
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = stringResource(id = R.string.controls_objective),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AmberBright.copy(alpha = 0.9f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
