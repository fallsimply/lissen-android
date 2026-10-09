package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.grakovne.lissen.R
import org.grakovne.lissen.content.cache.persistent.CacheState
import org.grakovne.lissen.domain.CacheStatus
import org.grakovne.lissen.domain.CurrentEpisodeTimerOption
import org.grakovne.lissen.domain.DetailedItem
import org.grakovne.lissen.domain.DurationTimerOption
import org.grakovne.lissen.domain.LibraryType
import org.grakovne.lissen.playback.service.calculateChapterIndex
import org.grakovne.lissen.ui.extensions.formatPlaybackSpeed
import org.grakovne.lissen.ui.extensions.formatTime
import org.grakovne.lissen.ui.icons.TimerPlay
import org.grakovne.lissen.ui.navigation.AppNavigationService
import org.grakovne.lissen.viewmodel.CachingModelView
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun NavigationBarComposable(
  book: DetailedItem,
  playerViewModel: PlayerViewModel,
  contentCachingModelView: CachingModelView,
  navController: AppNavigationService,
  modifier: Modifier = Modifier,
  libraryType: LibraryType,
) {
  val cacheProgress: CacheState by contentCachingModelView.getProgress(book.id).collectAsState()
  val timerOption by playerViewModel.timerOption.collectAsState()
  val timerRemaining by playerViewModel.timerRemaining.collectAsState()
  val playbackSpeed by playerViewModel.playbackSpeed.collectAsState()
  val playingQueueExpanded by playerViewModel.playingQueueExpanded.collectAsState()
  val hasEpisodes = book.chapters.isNotEmpty()

  val isMetadataCached by remember(book.id) { contentCachingModelView.provideCacheState(book.id) }.collectAsState(initial = false)
  val totalPosition by playerViewModel.totalPosition.collectAsState()
  val remainingChapters = (book.chapters.size - calculateChapterIndex(book, totalPosition)).coerceAtLeast(1)

  var playbackSpeedExpanded by remember { mutableStateOf(false) }
  var timerExpanded by remember { mutableStateOf(false) }
  var downloadsExpanded by remember { mutableStateOf(false) }
  var bookmarksExpanded by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()

  val chapterListLabel =
    when (libraryType) {
      LibraryType.LIBRARY -> stringResource(R.string.player_screen_chapter_list_navigation_library)
      LibraryType.PODCAST -> stringResource(R.string.player_screen_chapter_list_navigation_podcast)
    }
  val downloadsLabel = stringResource(R.string.player_screen_downloads_navigation)
  val bookmarksLabel = stringResource(R.string.bookmarks_title)
  val playbackSpeedLabel = stringResource(R.string.player_screen_playback_speed_navigation)
  val timerLabel =
    when (timerOption) {
      is DurationTimerOption, CurrentEpisodeTimerOption -> {
        timerRemaining
          ?.toInt()
          ?.formatTime(false)
          ?: stringResource(R.string.player_screen_timer_navigation)
      }

      null -> {
        stringResource(R.string.player_screen_timer_navigation)
      }
    }

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
          enabled = hasEpisodes,
          onClick = { playerViewModel.togglePlayingQueue() },
          icon = {
            Icon(
              Icons.AutoMirrored.Rounded.QueueMusic,
              contentDescription = chapterListLabel,
              modifier = Modifier.size(iconSize),
              tint =
                if (playingQueueExpanded) {
                  colorScheme.primary
                } else {
                  LocalContentColor.current
                },
            )
          },
          label = chapterListLabel,
        )

        clickableItem(
          onClick = {
            playerViewModel.updateBookmarks()
            bookmarksExpanded = true
          },
          icon = {
            Icon(
              Icons.Outlined.Bookmarks,
              contentDescription = bookmarksLabel,
              modifier =
                Modifier
                  .size(iconSize)
                  .testTag("playerBookmarksButton"),
            )
          },
          label = bookmarksLabel,
        )

        clickableItem(
          enabled = hasEpisodes,
          onClick = { downloadsExpanded = true },
          icon = {
            DownloadProgressIcon(
              cacheState = cacheProgress,
              size = iconSize,
            )
          },
          label = downloadsLabel,
        )

        clickableItem(
          enabled = hasEpisodes,
          onClick = { playbackSpeedExpanded = true },
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
          enabled = hasEpisodes,
          onClick = { timerExpanded = true },
          icon = {
            Icon(
              when (timerOption) {
                null -> Icons.Outlined.Timer
                else -> TimerPlay
              },
              contentDescription = timerLabel,
              modifier = Modifier.size(iconSize),
            )
          },
          label = timerLabel,
        )
      }
    }
  }

  if (bookmarksExpanded) {
    BookmarksComposable(
      playerViewModel = playerViewModel,
      onDismissRequest = { bookmarksExpanded = false },
    )
  }

  if (playbackSpeedExpanded) {
    PlaybackSpeedComposable(
      currentSpeed = playbackSpeed,
      onSpeedChange = { playerViewModel.setPlaybackSpeed(it) },
      onDismissRequest = { playbackSpeedExpanded = false },
    )
  }

  if (timerExpanded) {
    TimerComposable(
      libraryType = libraryType,
      currentOption = timerOption,
      onOptionSelected = { playerViewModel.setTimer(it) },
      onDismissRequest = { timerExpanded = false },
    )
  }

  if (downloadsExpanded) {
    DownloadsComposable(
      libraryType = libraryType,
      hasCachedEpisodes = isMetadataCached,
      isForceCache = contentCachingModelView.localCacheUsing(),
      cachingInProgress = cacheProgress.status is CacheStatus.Caching,
      chaptersCount = contentCachingModelView.getDownloadChaptersCount(),
      maxChaptersCount = remainingChapters,
      onChaptersCountChanged = { contentCachingModelView.saveDownloadChaptersCount(it) },
      onRequestedDownload = { option ->
        playerViewModel.book.value?.let {
          contentCachingModelView
            .cache(
              mediaItem = it,
              currentPosition = playerViewModel.totalPosition.value,
              option = option,
            )
        }
      },
      onRequestedDrop = {
        playerViewModel
          .book
          .value
          ?.let {
            scope.launch {
              contentCachingModelView.dropCache(it.id)

              playerViewModel.clearPlayingBook()
              navController.showLibrary(true)
            }
          }
      },
      onRequestedStop = {
        playerViewModel
          .book
          .value
          ?.let {
            scope.launch {
              contentCachingModelView.stopCaching(it)
            }
          }
      },
      onDismissRequest = { downloadsExpanded = false },
    )
  }
}

/**
 * The Speed tab label: the playback speed while it differs from normal, so a changed speed shows
 * without opening the sheet (as the Timer tab shows its countdown). Null at normal speed, where
 * the tab keeps its plain label.
 */
internal fun provideSpeedLabel(speed: Float): String? =
  speed
    .formatPlaybackSpeed()
    .takeIf { it != 1f.formatPlaybackSpeed() }

@Composable
private fun DownloadProgressIcon(
  cacheState: CacheState,
  size: Dp,
) {
  if (cacheState.status is CacheStatus.Caching) {
    val iconSize = size - 2.dp
    CircularProgressIndicator(
      progress = { cacheState.progress.coerceIn(0.0, 1.0).toFloat() },
      modifier = Modifier.size(iconSize),
      strokeWidth = iconSize * 0.1f,
      color = colorScheme.primary,
      trackColor = LocalContentColor.current,
      strokeCap = StrokeCap.Butt,
      gapSize = 2.dp,
    )
  } else {
    Icon(
      imageVector = Icons.Outlined.CloudDownload,
      contentDescription = stringResource(R.string.player_screen_downloads_navigation),
      modifier = Modifier.size(size),
    )
  }
}
