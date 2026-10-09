@file:OptIn(ExperimentalMaterial3Api::class)

package org.grakovne.lissen.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.R
import org.grakovne.lissen.ui.icons.Search

@Composable
fun ChapterSearchActionComposable(
  onSearchRequested: (String) -> Unit,
  modifier: Modifier = Modifier,
  requestFocusOnLaunch: Boolean = true,
) {
  var query by remember { mutableStateOf("") }
  var expanded by remember { mutableStateOf(requestFocusOnLaunch) }

  SearchBarDefaults.InputField(
    query = query,
    onQueryChange = {
      query = it
      onSearchRequested(it)
    },
    onSearch = onSearchRequested,
    expanded = expanded,
    onExpandedChange = { expanded = it },
    modifier = modifier.clip(SearchBarDefaults.inputFieldShape).background(colorScheme.surfaceContainer),
    placeholder = {
      Text(
        text = stringResource(R.string.chapter_search_hint),
        color = colorScheme.onSurfaceVariant,
      )
    },
    leadingIcon = {
      Icon(
        imageVector = Search,
        contentDescription = null,
      )
    },
    trailingIcon =
      if (query.isNotEmpty()) {
        {
          IconButton(
            onClick = {
              query = ""
              onSearchRequested("")
            },
          ) {
            Icon(
              imageVector = Icons.Outlined.Clear,
              contentDescription = stringResource(R.string.a11y_clear),
            )
          }
        }
      } else {
        null
      },
  )
}
