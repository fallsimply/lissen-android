package org.grakovne.lissen.ui.screens.player.composable

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.R
import org.grakovne.lissen.common.withHaptic
import org.grakovne.lissen.playback.PlaybackGeometry
import org.grakovne.lissen.ui.components.LissenModalBottomSheet
import org.grakovne.lissen.ui.components.slider.PlaybackSpeedSlider
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedComposable(
  currentSpeed: Float,
  onSpeedChange: (Float) -> Unit,
  onDismissRequest: () -> Unit,
) {
  val view: View = LocalView.current

  LissenModalBottomSheet(
    containerColor = colorScheme.background,
    onDismissRequest = onDismissRequest,
    content = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(
            imageVector = Icons.Outlined.SlowMotionVideo,
            contentDescription = null,
            tint = colorScheme.onBackground,
            modifier = Modifier.size(24.dp),
          )
          Text(
            text = stringResource(R.string.playback_speed_title),
            style = typography.bodyLarge,
          )
        }

        PlaybackSpeedSlider(
          speed = currentSpeed,
          speedRange = (PlaybackGeometry.MIN_PLAYBACK_SPEED..PlaybackGeometry.MAX_PLAYBACK_SPEED),
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
          onSpeedUpdate = onSpeedChange,
        )

        ButtonGroup(
          overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
          },
          horizontalArrangement = ButtonGroupDefaults.HorizontalArrangement,
          modifier = Modifier.height(ButtonDefaults.MediumContainerHeight),
        ) {
          playbackSpeedPresets.forEach { value ->
            toggleableItem(
              onCheckedChange = { _ ->
                withHaptic(view) {
                  onSpeedChange(value)
                }
              },
              checked = currentSpeed == value,
              label = String.format(Locale.US, "%.1f", value),
            )
          }
        }
      }
    },
  )
}

private val playbackSpeedPresets = listOf(1f, 1.2f, 1.5f, 2f, 3f)
