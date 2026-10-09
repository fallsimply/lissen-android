@file:OptIn(ExperimentalMaterial3Api::class)

package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.domain.LibraryType
import org.grakovne.lissen.ui.components.LissenModalBottomSheet
import org.grakovne.lissen.ui.screens.player.ChapterSearchActionComposable
import org.grakovne.lissen.ui.screens.player.composable.common.provideNowPlayingTitle
import org.grakovne.lissen.viewmodel.CachingModelView
import org.grakovne.lissen.viewmodel.PlayerViewModel

private val ChapterSearchBarHeight = SearchBarDefaults.InputFieldHeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingQueueBottomSheetComposable(
  libraryType: LibraryType,
  cachingModelView: CachingModelView,
  viewModel: PlayerViewModel,
  onDismissRequest: () -> Unit,
) {
  val context = LocalContext.current
  val listState = rememberLazyListState()
  val sheetListMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.55f

  LissenModalBottomSheet(
    containerColor = colorScheme.background,
    scrollable = false,
    onDismissRequest = onDismissRequest,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
          .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = provideNowPlayingTitle(libraryType, context),
        style = typography.bodyLarge,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(max = sheetListMaxHeight),
      ) {
        PlayingQueueList(
          cachingModelView = cachingModelView,
          viewModel = viewModel,
          listState = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = ChapterSearchBarHeight),
        )

        ChapterSearchActionComposable(
          onSearchRequested = { viewModel.updateSearch(it) },
          modifier =
            Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .imePadding()
              .background(colorScheme.background)
              .padding(top = 8.dp),
          requestFocusOnLaunch = false,
        )
      }
    }
  }
}
