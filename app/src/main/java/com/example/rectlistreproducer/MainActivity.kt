package com.example.rectlistreproducer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Compose alignment placement reproducer",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Compose UI 1.12.1 / BOM 2026.09.00",
            style = MaterialTheme.typography.bodyMedium
        )
        StableSearchFlowCase()
    }
}

private sealed interface OverlayState {
    data object Suggestions : OverlayState
    data object Loading : OverlayState
    data object Results : OverlayState
}

@Composable
private fun BaselineOverlay(
    modifier: Modifier,
    index: Int,
    state: OverlayState
) {
    BaselineOverlayLayout(
        modifier = modifier,
        anchor = {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicText(
                    text = when (state) {
                        OverlayState.Suggestions -> "Suggestion $index"
                        OverlayState.Loading -> "Loading"
                        OverlayState.Results -> "Result $index with changing content"
                    },
                    style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
                )
            }
        },
    )
}

@Composable
private fun BaselineOverlayLayout(
    modifier: Modifier,
    anchor: @Composable () -> Unit,
) {
    Layout(
        content = {
            Box(modifier = Modifier.layoutId("anchor")) { anchor() }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val anchorPlaceable = measurables.first { it.layoutId == "anchor" }.measure(constraints)
        val firstBaseline = anchorPlaceable[FirstBaseline]
        val lastBaseline = anchorPlaceable[LastBaseline]
        layout(
            width = anchorPlaceable.width,
            height = anchorPlaceable.height,
            alignmentLines = mapOf(
                FirstBaseline to firstBaseline,
                LastBaseline to lastBaseline,
            ),
        ) {
            anchorPlaceable.placeRelative(0, 0)
        }
    }
}

@Composable
private fun StableSearchFlowCase() {
    val listState = rememberLazyListState()

    Text(
        text = "Search: sample",
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = "Results loaded. Perform fast downward flings.",
        style = MaterialTheme.typography.bodyMedium
    )
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(GenericSearchResults) { index ->
            GenericSearchRow(index = index)
        }
    }
}

private val GenericSearchResults = List(160) { it }

@Composable
private fun GenericSearchRow(index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BaselineOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .dynamicSquareLayout(),
            index = index,
            state = OverlayState.Results,
        )
    }
}

private fun Modifier.dynamicSquareLayout(): Modifier =
    fillMaxSize().layout { measurable, constraints ->
        val fallback = 64.dp.roundToPx()
        val availableWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else fallback
        val availableHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else fallback
        val side = minOf(availableWidth, availableHeight)
        val placeable = measurable.measure(
            constraints.copy(minWidth = 0, maxWidth = side, minHeight = 0, maxHeight = side)
        )
        layout(side, side) {
            placeable.placeRelative(
                x = (side - placeable.width) / 2,
                y = (side - placeable.height) / 2
            )
        }
    }

@Composable
private fun RectListReproducerTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
