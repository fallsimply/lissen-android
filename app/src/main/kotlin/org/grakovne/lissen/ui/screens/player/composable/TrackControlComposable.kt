package org.grakovne.lissen.ui.screens.player.composable

import android.view.View
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.R
import org.grakovne.lissen.common.withHaptic
import org.grakovne.lissen.domain.SeekTime
import org.grakovne.lissen.ui.extensions.formatTime
import org.grakovne.lissen.ui.extensions.spokenDuration
import org.grakovne.lissen.ui.screens.player.composable.common.provideForwardIcon
import org.grakovne.lissen.ui.screens.player.composable.common.provideReplayIcon
import org.grakovne.lissen.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackControlComposable(
  viewModel: PlayerViewModel,
  seekTime: SeekTime,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val isPlaying by viewModel.isPlaying.collectAsState()
  val currentTrackIndex by viewModel.currentChapterIndex.collectAsState()
  val currentTrackPosition by viewModel.currentChapterPosition.collectAsState()
  val currentTrackDuration by viewModel.currentChapterDuration.collectAsState()

  val book by viewModel.book.collectAsState()
  val chapters = book?.chapters ?: emptyList()

  val view: View = LocalView.current

  var sliderPosition by remember { mutableDoubleStateOf(0.0) }
  var isDragging by remember { mutableStateOf(false) }

  LaunchedEffect(currentTrackPosition, currentTrackIndex, currentTrackDuration, enabled) {
    if (enabled && !isDragging) {
      sliderPosition = safeSliderPosition(currentTrackPosition, currentTrackDuration)
    }
    if (!enabled) {
      sliderPosition = 0.0
      isDragging = false
    }
  }

  val positionLabel = stringResource(R.string.a11y_playback_position)
  val spokenPosition =
    stringResource(
      R.string.a11y_position_of,
      spokenDuration(sliderPosition.toInt()),
      spokenDuration(currentTrackDuration.toInt()),
    )
  val previousLabel = stringResource(R.string.a11y_previous_track)
  val rewindLabel = stringResource(R.string.a11y_rewind_seconds, seekTime.rewind)
  val playLabel = stringResource(R.string.a11y_play)
  val pauseLabel = stringResource(R.string.a11y_pause)
  val forwardLabel = stringResource(R.string.a11y_fast_forward_seconds, seekTime.forward)
  val nextLabel = stringResource(R.string.a11y_next_track)
  val canSkipNext = enabled && currentTrackIndex < chapters.size - 1

  val sliderInteraction = remember { MutableInteractionSource() }
  val previousInteraction = remember { MutableInteractionSource() }
  val rewindInteraction = remember { MutableInteractionSource() }
  val playInteraction = remember { MutableInteractionSource() }
  val forwardInteraction = remember { MutableInteractionSource() }
  val nextInteraction = remember { MutableInteractionSource() }

  Column(
    modifier =
      modifier
        .testTag("trackControls")
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp),
  ) {
    Slider(
      value = sliderPosition.toFloat(),
      onValueChange = { newPosition ->
        if (!enabled) return@Slider
        isDragging = true
        sliderPosition = newPosition.toDouble()
      },
      onValueChangeFinished = {
        if (!enabled) return@Slider
        isDragging = false
        viewModel.seekTo(sliderPosition)
      },
      enabled = enabled,
      valueRange = 0f..safeSliderDuration(if (enabled) currentTrackDuration else 0.0),
      interactionSource = sliderInteraction,
      colors =
        SliderDefaults.colors(
          thumbColor = colorScheme.primary,
          activeTrackColor = colorScheme.primary,
        ),
      track = { sliderState ->
        LinearWavyProgressIndicator(
          progress = { sliderState.coercedValueAsFraction },
          modifier = Modifier.fillMaxWidth(),
          color = colorScheme.primary,
          trackColor = colorScheme.onBackground.copy(alpha = 0.2f),
          wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
          amplitude = { if (enabled && isPlaying && !isDragging) 1f else 0f },
        )
      },
      modifier =
        Modifier
          .fillMaxWidth()
          .semantics {
            contentDescription = positionLabel
            stateDescription = spokenPosition
          },
    )

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = sliderPosition.toInt().formatTime(true),
        style = typography.bodySmall,
        color = colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.clearAndSetSemantics {},
      )
      Text(
        text =
          maxOf(0.0, (if (enabled) currentTrackDuration else 0.0) - sliderPosition)
            .toInt()
            .formatTime(true),
        style = typography.bodySmall,
        color = colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.clearAndSetSemantics {},
      )
    }

    ButtonGroup(
      overflowIndicator = { menuState ->
        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
      },
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 16.dp)
          .height(IconButtonDefaults.mediumContainerSize().height),
      horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      trackControlPlayItem(
        interactionSource = playInteraction,
        label = if (isPlaying) pauseLabel else playLabel,
        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
        enabled = enabled,
        onClick = { withHaptic(view) { viewModel.togglePlayPause() } },
        weight = .3f,
      )

      trackControlItem(
        interactionSource = rewindInteraction,
        label = rewindLabel,
        imageVector = provideReplayIcon(seekTime),
        iconSize = 32.dp,
        enabled = enabled,
        onClick = { withHaptic(view) { viewModel.rewind() } },
        weight = .175f,
      )

      trackControlItem(
        interactionSource = forwardInteraction,
        label = forwardLabel,
        imageVector = provideForwardIcon(seekTime),
        iconSize = 32.dp,
        enabled = enabled,
        onClick = { withHaptic(view) { viewModel.forward() } },
        weight = .175f,
      )

      trackControlItem(
        interactionSource = previousInteraction,
        label = previousLabel,
        imageVector = Icons.Rounded.SkipPrevious,
        iconSize = 28.dp,
        enabled = enabled,
        onClick = { withHaptic(view) { viewModel.previousTrack() } },
        weight = .175f,
      )

      trackControlItem(
        interactionSource = nextInteraction,
        label = nextLabel,
        imageVector = Icons.Rounded.SkipNext,
        iconSize = 28.dp,
        enabled = canSkipNext,
        onClick = {
          if (canSkipNext) {
            withHaptic(view) { viewModel.nextTrack() }
          }
        },
        weight = .175f,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun ButtonGroupScope.trackControlItem(
  interactionSource: MutableInteractionSource,
  label: String,
  imageVector: ImageVector,
  iconSize: Dp,
  enabled: Boolean = true,
  onClick: () -> Unit,
  weight: Float = 1f,
) {
  customItem(
    buttonGroupContent = {
      IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shapes = IconButtonDefaults.shapes(),
        modifier =
          Modifier
            .weight(weight)
            .fillMaxSize()
            .animateWidth(interactionSource),
      ) {
        Icon(
          imageVector = imageVector,
          contentDescription = label,
          tint =
            if (enabled) {
              colorScheme.onBackground
            } else {
              colorScheme.onBackground.copy(alpha = 0.3f)
            },
          modifier = Modifier.size(iconSize),
        )
      }
    },
    menuContent = { state ->
      DropdownMenuItem(
        enabled = enabled,
        text = { Text(label) },
        onClick = {
          onClick()
          state.dismiss()
        },
        leadingIcon = {
          Icon(imageVector = imageVector, contentDescription = null)
        },
      )
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun ButtonGroupScope.trackControlPlayItem(
  interactionSource: MutableInteractionSource,
  label: String,
  imageVector: ImageVector,
  onClick: () -> Unit,
  enabled: Boolean = true,
  weight: Float = 1f,
) {
  customItem(
    buttonGroupContent = {
      FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shapes = IconButtonDefaults.shapes(),
        colors =
          IconButtonDefaults.filledIconButtonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
          ),
        modifier =
          Modifier
            .weight(weight)
            .fillMaxSize()
            .animateWidth(interactionSource),
      ) {
        Icon(
          imageVector = imageVector,
          contentDescription = label,
          modifier = Modifier.size(36.dp),
        )
      }
    },
    menuContent = { state ->
      DropdownMenuItem(
        enabled = enabled,
        text = { Text(label) },
        onClick = {
          onClick()
          state.dismiss()
        },
        leadingIcon = {
          Icon(imageVector = imageVector, contentDescription = null)
        },
      )
    },
  )
}

/** Corrupted metadata can report a negative or non-finite duration. An empty slider range crashes when coerced. */
internal fun safeSliderDuration(duration: Double): Float = if (duration.isFinite() && duration > 0) duration.toFloat() else 0f

internal fun safeSliderPosition(
  position: Double,
  duration: Double,
): Double {
  if (!position.isFinite()) return 0.0

  return position.coerceIn(0.0, safeSliderDuration(duration).toDouble())
}
