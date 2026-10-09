package org.grakovne.lissen.ui.screens.player.composable

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.R
import org.grakovne.lissen.common.withHaptic
import org.grakovne.lissen.domain.CurrentEpisodeTimerOption
import org.grakovne.lissen.domain.DurationTimerOption
import org.grakovne.lissen.domain.LibraryType
import org.grakovne.lissen.domain.TimerOption
import org.grakovne.lissen.ui.components.LissenModalBottomSheet
import org.grakovne.lissen.ui.components.slider.SleepTimerSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerComposable(
  currentOption: TimerOption?,
  libraryType: LibraryType,
  onOptionSelected: (TimerOption?) -> Unit,
  onDismissRequest: () -> Unit,
) {
  val view = LocalView.current
  val context = LocalContext.current
  var selectedTimerOption by remember { mutableStateOf(currentOption) }

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
        Text(
          text = stringResource(R.string.timer_title),
          style = typography.bodyLarge,
        )

        SleepTimerSlider(
          libraryType = libraryType,
          context = context,
          option = selectedTimerOption,
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
          onUpdate = {
            selectedTimerOption = it
            onOptionSelected(it)
          },
        )

        ButtonGroup(
          overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
          },
          horizontalArrangement = ButtonGroupDefaults.HorizontalArrangement,
          modifier = Modifier.height(ButtonDefaults.MediumContainerHeight),
        ) {
          timerPresets.forEach { value ->
            toggleableItem(
              onCheckedChange = { _ ->
                withHaptic(view) {
                  selectedTimerOption = value
                  onOptionSelected(value)
                }
              },
              checked = selectedTimerOption.isSame(value),
              label = value.toPresetLabel(context),
            )
          }
        }
      }
    },
  )
}

private fun TimerOption?.isSame(that: TimerOption?) =
  when (this) {
    CurrentEpisodeTimerOption -> that == CurrentEpisodeTimerOption
    is DurationTimerOption -> that is DurationTimerOption && that.duration == this.duration
    null -> that == null
  }

private fun TimerOption?.toPresetLabel(context: Context): String =
  when (this) {
    null -> context.getString(R.string.timer_option_disabled)
    is DurationTimerOption -> duration.toString()
    CurrentEpisodeTimerOption -> context.getString(R.string.player_screen_timer_navigation)
  }

private val timerPresets =
  listOf(
    null,
    DurationTimerOption(10),
    DurationTimerOption(15),
    DurationTimerOption(30),
    DurationTimerOption(60),
  )
