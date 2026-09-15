package com.example.rectlistreproducer

import android.os.Bundle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import coil3.compose.ConstraintsSizeResolver
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RectListReproducerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    var query by remember { mutableStateOf("sample") }
    var contentState by remember { mutableStateOf(SearchContent.Suggestions) }
    var transitionToken by remember { mutableStateOf(0) }

    LaunchedEffect(query, transitionToken) {
        contentState = SearchContent.Suggestions
        delay(700)
        contentState = SearchContent.Results
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            SearchHeader(
                query = query,
                onQueryChanged = { query = it },
                onRepeatTransition = { transitionToken++ }
            )
            AnimatedContent(
                targetState = contentState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Search content transition"
            ) { state ->
                SearchContent(state = state)
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChanged: (String) -> Unit,
    onRepeatTransition: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures {
                    onQueryChanged("sample")
                    onRepeatTransition()
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Search: $query",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun SearchContent(state: SearchContent) {
    val items = remember(state) {
        when (state) {
            SearchContent.Suggestions -> List(18) { SearchRow.Suggestion(it) }
            SearchContent.Results -> List(100) { index ->
                when (index % 4) {
                    0 -> SearchRow.Tagged(index)
                    1 -> SearchRow.Badged(index)
                    2 -> SearchRow.Image(index)
                    else -> SearchRow.Text(index)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state) { detectTapGestures {} },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = items) { item ->
            SearchItem(item)
        }
    }
}

private sealed interface SearchRow {
    data class Suggestion(val index: Int) : SearchRow
    data class Tagged(val index: Int) : SearchRow
    data class Badged(val index: Int) : SearchRow
    data class Image(val index: Int) : SearchRow
    data class Text(val index: Int) : SearchRow
}

private enum class SearchContent {
    Suggestions,
    Results
}

@Composable
private fun SearchItem(item: SearchRow) {
    when (item) {
        is SearchRow.Suggestion -> TextRow(text = "Suggestion ${item.index} for sample")
        is SearchRow.Tagged -> BaselineOverlayItem(
            index = item.index,
            layout = OverlayLayoutVariant.Tagged
        )
        is SearchRow.Badged -> BaselineOverlayItem(
            index = item.index,
            layout = OverlayLayoutVariant.Badged
        )
        is SearchRow.Image -> BaselineOverlayItem(
            index = item.index,
            layout = OverlayLayoutVariant.Image
        )
        is SearchRow.Text -> TextRow(text = "Result ${item.index} for sample")
    }
}

@Composable
private fun TextRow(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

private enum class OverlayLayoutVariant {
    Tagged,
    Badged,
    Image
}

@Composable
private fun BaselineOverlayItem(
    index: Int,
    layout: OverlayLayoutVariant
) {
    val sizeResolver = rememberConstraintsSizeResolver()

    OverlayLayout(
        variant = layout,
        modifier = Modifier
            .fillMaxWidth()
            .then(sizeResolver)
            .padding(horizontal = 16.dp),
        anchor = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (layout == OverlayLayoutVariant.Image) {
                    AnchorImage(sizeResolver = sizeResolver)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Item $index",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Generated content for a scrolling alignment-line sample. " +
                            "This text intentionally wraps across multiple lines.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        marker = {
            Marker(modifier = Modifier.size(if (layout == OverlayLayoutVariant.Badged) 32.dp else 24.dp))
        }
    )
}

@Composable
private fun AnchorImage(
    sizeResolver: ConstraintsSizeResolver,
    modifier: Modifier = Modifier
) {
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(R.drawable.placeholder_image)
        .size(sizeResolver)
        .build()

    Box(
        modifier = modifier
            .size(64.dp)
            .background(Color(0xFF4F46E5))
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageRequest),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        )
    }
}

@Composable
private fun Marker(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF4F46E5))
        )
    }
}

@Composable
private fun OverlayLayout(
    anchor: @Composable () -> Unit,
    marker: @Composable () -> Unit,
    variant: OverlayLayoutVariant,
    modifier: Modifier = Modifier
) {
    Layout(
        content = {
            Box(modifier = Modifier.layoutId(AnchorId)) {
                anchor()
            }
            Box(modifier = Modifier.layoutId(MarkerId)) {
                marker()
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val anchorPlaceable = measurables
            .first { it.layoutId == AnchorId }
            .measure(constraints)
        val markerConstraints = if (variant == OverlayLayoutVariant.Badged) {
            constraints.copy(minHeight = 0)
        } else {
            constraints
        }
        val markerPlaceable = measurables
            .first { it.layoutId == MarkerId }
            .measure(markerConstraints)

        val firstBaseline = anchorPlaceable[FirstBaseline]
        val lastBaseline = anchorPlaceable[LastBaseline]

        layout(
            width = anchorPlaceable.width,
            height = anchorPlaceable.height,
            alignmentLines = mapOf(
                FirstBaseline to firstBaseline,
                LastBaseline to lastBaseline
            )
        ) {
            anchorPlaceable.placeRelative(0, 0)
            markerPlaceable.placeRelative(
                x = anchorPlaceable.width - markerPlaceable.width + 4.dp.roundToPx(),
                y = -4.dp.roundToPx()
            )
        }
    }
}

private const val AnchorId = "anchor"
private const val MarkerId = "marker"

@Composable
private fun RectListReproducerTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
