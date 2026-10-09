package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.grakovne.lissen.domain.LibraryType
import org.grakovne.lissen.domain.PlayingChapter
import org.grakovne.lissen.ui.components.withScrollbar
import org.grakovne.lissen.ui.screens.player.composable.common.provideNowPlayingTitle
import org.grakovne.lissen.viewmodel.CachingModelView
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun PlayingQueueComposable(
  libraryType: LibraryType,
  cachingModelView: CachingModelView,
  viewModel: PlayerViewModel,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val listState = rememberLazyListState()

  val showScrollbar by remember {
    derivedStateOf {
      listState.isScrollInProgress
    }
  }

  val scrollbarAlpha by animateFloatAsState(
    targetValue = if (showScrollbar) 1f else 0f,
    animationSpec = tween(durationMillis = 300),
  )

  val fontSize by animateFloatAsState(
    targetValue = typography.titleMedium.fontSize.value * 1.25f,
    animationSpec = tween(durationMillis = 500),
    label = "playing_queue_font_size",
  )

  val showingChapters = rememberFilteredChapters(viewModel)

  Column(
    modifier =
      modifier
        .testTag("chapterList")
        .fillMaxSize()
        .withScrollbar(
          state = listState,
          color = colorScheme.onBackground.copy(alpha = scrollbarAlpha),
          totalItems = showingChapters.size,
          ignoreItems = emptyList(),
        ).padding(horizontal = 16.dp),
  ) {
    Text(
      text = provideNowPlayingTitle(libraryType, context),
      fontSize = fontSize.sp,
      fontWeight = FontWeight.SemiBold,
      color = colorScheme.primary,
      modifier = Modifier.padding(horizontal = 6.dp),
    )

    Spacer(modifier = Modifier.height(12.dp))

    PlayingQueueList(
      cachingModelView = cachingModelView,
      viewModel = viewModel,
      listState = listState,
      modifier = Modifier.fillMaxHeight(),
      contentPadding = PaddingValues(bottom = 12.dp),
    )
  }
}

@Composable
internal fun PlayingQueueList(
  cachingModelView: CachingModelView,
  viewModel: PlayerViewModel,
  listState: LazyListState,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(bottom = 12.dp),
) {
  val book by viewModel.book.collectAsState()
  val showingChapters = rememberFilteredChapters(viewModel)
  val currentTrackIndex by viewModel.currentChapterIndex.collectAsState()
  val playbackReady by viewModel.isPlaybackReady.collectAsState()

  val currentTrackId by remember {
    derivedStateOf {
      book?.chapters?.getOrNull(currentTrackIndex)
    }
  }

  val bookId = book?.id ?: ""
  val cachedChapterIdsFlow =
    remember(bookId) {
      when (bookId.isEmpty()) {
        true -> flowOf(emptySet())
        false -> cachingModelView.provideCachedChapterIds(bookId).map { it.toSet() }
      }
    }
  val cachedChapterIds by cachedChapterIdsFlow.collectAsState(initial = emptySet())

  LaunchedEffect(currentTrackIndex) {
    awaitFrame()
    scrollPlayingQueue(
      currentTrackIndex = currentTrackIndex,
      listState = listState,
      playbackReady = playbackReady,
      animate = true,
    )
  }

  LazyColumn(
    contentPadding = contentPadding,
    modifier = modifier.testTag("chapterListItems"),
    state = listState,
  ) {
    val maxDuration = showingChapters.maxOfOrNull { it.duration } ?: 0.0

    itemsIndexed(
      showingChapters,
      key = { _, chapter -> chapter.id },
    ) { index, chapter ->
      PlaylistItemComposable(
        track = chapter,
        onClick = { viewModel.setChapter(chapter) },
        isSelected = chapter.id == currentTrackId?.id,
        modifier = Modifier.wrapContentWidth(),
        maxDuration = maxDuration,
        isCached = chapter.id in cachedChapterIds,
      )

      if (index < showingChapters.size - 1) {
        HorizontalDivider(
          thickness = 1.dp,
          modifier =
            Modifier
              .padding(start = 24.dp)
              .padding(vertical = 8.dp),
        )
      }
    }
  }
}

private suspend fun scrollPlayingQueue(
  currentTrackIndex: Int,
  listState: LazyListState,
  playbackReady: Boolean,
  animate: Boolean,
) {
  val targetIndex =
    when (currentTrackIndex > 0) {
      true -> currentTrackIndex - 1
      false -> 0
    }

  when (animate && playbackReady) {
    true -> listState.animateScrollToItem(targetIndex)
    false -> listState.scrollToItem(targetIndex)
  }
}

@Composable
private fun rememberFilteredChapters(viewModel: PlayerViewModel): List<PlayingChapter> {
  val book by viewModel.book.collectAsState()
  val searchToken by viewModel.searchToken.collectAsState()

  return remember(book, searchToken) {
    when (searchToken.isEmpty()) {
      true -> {
        book?.chapters ?: emptyList()
      }

      false -> {
        book
          ?.chapters
          ?.filter { it.title.lowercase().contains(searchToken.lowercase()) }
          ?: emptyList()
      }
    }
  }
}
