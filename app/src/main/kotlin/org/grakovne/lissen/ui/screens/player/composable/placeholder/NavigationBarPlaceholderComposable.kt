package org.grakovne.lissen.ui.screens.player.composable.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.R
import org.grakovne.lissen.domain.LibraryType
import org.grakovne.lissen.ui.icons.TimerPlay

@Composable
fun NavigationBarPlaceholderComposable(
  modifier: Modifier = Modifier,
  libraryType: LibraryType,
) {
  val chapterListLabel =
    when (libraryType) {
      LibraryType.LIBRARY -> stringResource(R.string.player_screen_chapter_list_navigation_library)
      LibraryType.PODCAST -> stringResource(R.string.player_screen_chapter_list_navigation_podcast)
    }
  val downloadsLabel = stringResource(R.string.player_screen_downloads_navigation)
  val bookmarksLabel = stringResource(R.string.bookmarks_title)
  val playbackSpeedLabel = stringResource(R.string.player_screen_playback_speed_navigation)
  val timerLabel = stringResource(R.string.player_screen_timer_navigation)

  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center,
  ) {
    HorizontalFloatingToolbar(
      expanded = false,
      modifier =
        Modifier
          .padding(horizontal = 16.dp, vertical = 8.dp),
      colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
    ) {
      val iconSize = 24.dp

      AppBarRow {
        clickableItem(
          enabled = false,
          onClick = {},
          icon = {
            Icon(
              Icons.AutoMirrored.Rounded.QueueMusic,
              contentDescription = chapterListLabel,
              modifier = Modifier.size(iconSize),
            )
          },
          label = chapterListLabel,
        )

        clickableItem(
          enabled = false,
          onClick = {},
          icon = {
            Icon(
              Icons.Outlined.Bookmarks,
              contentDescription = bookmarksLabel,
              modifier = Modifier.size(iconSize),
            )
          },
          label = bookmarksLabel,
        )

        clickableItem(
          enabled = false,
          onClick = {},
          icon = {
            Icon(
              imageVector = Icons.Outlined.CloudDownload,
              contentDescription = downloadsLabel,
              modifier = Modifier.size(iconSize),
            )
          },
          label = downloadsLabel,
        )

        clickableItem(
          enabled = false,
          onClick = {},
          icon = {
            Icon(
              Icons.Outlined.SlowMotionVideo,
              contentDescription = playbackSpeedLabel,
              modifier = Modifier.size(iconSize),
            )
          },
          label = playbackSpeedLabel,
        )

        clickableItem(
          enabled = false,
          onClick = {},
          icon = {
            Icon(
              TimerPlay,
              contentDescription = timerLabel,
              modifier = Modifier.size(iconSize),
            )
          },
          label = timerLabel,
        )
      }
    }
  }
}
