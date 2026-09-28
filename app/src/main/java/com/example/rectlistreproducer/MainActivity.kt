package com.example.rectlistreproducer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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

private enum class ReproducerCase {
    Minimal,
    ExactAlignmentStress,
    AnimatedText,
    PositionedOverlay,
    LazyListStress,
    StableSearchFlow
}

@Composable
private fun MainScreen() {
    var selectedCase by remember { mutableStateOf(ReproducerCase.StableSearchFlow) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { selectedCase = ReproducerCase.Minimal }) {
                Text("Minimal")
            }
            Button(onClick = { selectedCase = ReproducerCase.ExactAlignmentStress }) {
                Text("Exact stress")
            }
            Button(onClick = { selectedCase = ReproducerCase.AnimatedText }) {
                Text("Animated text")
            }
            Button(onClick = { selectedCase = ReproducerCase.PositionedOverlay }) {
                Text("Positioned overlay")
            }
            Button(onClick = { selectedCase = ReproducerCase.LazyListStress }) {
                Text("Lazy stress")
            }
            Button(onClick = { selectedCase = ReproducerCase.StableSearchFlow }) {
                Text("Stable search")
            }
        }
        when (selectedCase) {
            ReproducerCase.Minimal -> MinimalAlignmentCase()
            ReproducerCase.ExactAlignmentStress -> ExactAlignmentStressCase()
            ReproducerCase.AnimatedText -> AnimatedTextCase()
            ReproducerCase.PositionedOverlay -> PositionedOverlayCase()
            ReproducerCase.LazyListStress -> LazyListStressCase()
            ReproducerCase.StableSearchFlow -> StableSearchFlowCase()
        }
    }
}

@Composable
private fun MinimalAlignmentCase() {
    Text(
        text = "Repeatedly enter and leave this screen or rotate the device while testing.",
        style = MaterialTheme.typography.bodyMedium
    )
    Row(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.alignByBaseline()) {
            BasicText(
                text = "text",
                modifier = Modifier
                    .size(10.dp)
                    .alignByBaseline(),
                style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
            )
        }
    }
}

@Composable
private fun ExactAlignmentStressCase() {
    var toggle by remember { mutableStateOf(false) }
    var updates by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            toggle = !toggle
            updates++
            delay(16)
        }
    }

    Text(
        text = "Upstream tree with tracked aligned nodes. Updates: $updates",
        style = MaterialTheme.typography.bodyMedium
    )
    Row(
        modifier = Modifier
            .padding(10.dp)
            .onGloballyPositioned { }
    ) {
        Row(
            modifier = Modifier
                .alignByBaseline()
                .onGloballyPositioned { }
        ) {
            if (toggle) {
                BasicText(
                    text = "text",
                    modifier = Modifier
                        .size(10.dp)
                        .alignByBaseline()
                        .onGloballyPositioned { },
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                )
            } else {
                BasicText(
                    text = "longer text",
                    modifier = Modifier
                        .size(20.dp)
                        .alignByBaseline()
                        .onGloballyPositioned { },
                    style = androidx.compose.ui.text.TextStyle(fontSize = 20.sp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedTextCase() {
    var value by remember { mutableStateOf("1000.00") }
    var updates by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(120)
            value = if (value == "1000.00") "10000.00" else "1000.00"
            updates++
        }
    }

    Text(
        text = "The value changes length continuously. Updates: $updates",
        style = MaterialTheme.typography.bodyMedium
    )
    Row(modifier = Modifier.padding(10.dp)) {
        value.forEachIndexed { index, character ->
            key(index) {
                AnimatedContent(
                    targetState = character,
                    modifier = Modifier.alignByBaseline(),
                    label = "Character $index"
                ) { animatedCharacter ->
                    BasicText(
                        text = animatedCharacter.toString(),
                        modifier = Modifier.alignByBaseline(),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 32.sp)
                    )
                }
            }
        }
    }
}

private sealed interface OverlayState {
    data object Suggestions : OverlayState
    data object Loading : OverlayState
    data object Results : OverlayState
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PositionedOverlayCase() {
    var state by remember { mutableStateOf<OverlayState>(OverlayState.Suggestions) }
    var updates by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            state = when (state) {
                OverlayState.Suggestions -> OverlayState.Loading
                OverlayState.Loading -> OverlayState.Results
                OverlayState.Results -> OverlayState.Suggestions
            }
            updates++
            delay(80)
        }
    }

    Text(
        text = "Custom overlay layout with coordinate tracking. Updates: $updates",
        style = MaterialTheme.typography.bodyMedium
    )
    AnimatedContent(
        targetState = state,
        contentKey = { animatedState -> animatedState.javaClass },
        label = "Positioned overlay content"
    ) { animatedState ->
        val itemCount = when (animatedState) {
            OverlayState.Suggestions -> 4
            OverlayState.Loading -> 1
            OverlayState.Results -> 8
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(itemCount) { index ->
                BaselineOverlay(
                    modifier = Modifier,
                    index = index,
                    state = animatedState
                )
            }
        }
    }
}

@Composable
private fun BaselineOverlay(
    modifier: Modifier,
    index: Int,
    state: OverlayState
) {
    var absoluteLeft by remember { mutableIntStateOf(0) }
    var absoluteTop by remember { mutableIntStateOf(0) }
    var parentRight by remember { mutableIntStateOf(Int.MAX_VALUE) }
    var parentTop by remember { mutableIntStateOf(Int.MIN_VALUE) }

    BaselineOverlayLayout(
        modifier = modifier
            // Semantics and pointer input make these nodes RectList participants, matching the
            // Nodes involved in the integration test and scrollable content.
            .semantics { contentDescription = "RectList item $index" }
            .pointerInput(index) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent()
                }
            },
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
        marker = {
            Box(modifier = Modifier.size(if (state == OverlayState.Results) 32.dp else 24.dp))
        },
        onPositioned = { coordinates ->
            val bounds = coordinates.boundsInWindow()
            absoluteLeft = bounds.left.roundToInt()
            absoluteTop = bounds.top.roundToInt()
            coordinates.parentLayoutCoordinates
                ?.parentLayoutCoordinates
                ?.parentCoordinates
                ?.boundsInWindow()
                ?.let {
                    parentRight = it.right.roundToInt()
                    parentTop = it.top.roundToInt()
                }
        },
        placementOffset = (parentRight - absoluteLeft - 1)
            .coerceAtMost(0)
            .coerceAtLeast(parentTop - absoluteTop)
    )
}

@Composable
private fun BaselineOverlayLayout(
    modifier: Modifier,
    anchor: @Composable () -> Unit,
    marker: @Composable () -> Unit,
    onPositioned: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit = {},
    placementOffset: Int = 0
) {
    Layout(
        content = {
            Box(modifier = Modifier.layoutId("anchor")) { anchor() }
            Box(modifier = Modifier.layoutId("marker")) { marker() }
        },
        modifier = modifier.onGloballyPositioned(onPositioned)
    ) { measurables, constraints ->
        val anchorPlaceable = measurables.first { it.layoutId == "anchor" }.measure(constraints)
        val markerPlaceable = measurables.first { it.layoutId == "marker" }.measure(
            constraints.copy(minHeight = 0)
        )
        layout(
            width = anchorPlaceable.width,
            height = anchorPlaceable.height,
        ) {
            anchorPlaceable.placeRelative(0, 0)
            markerPlaceable.placeRelative(
                x = anchorPlaceable.width - markerPlaceable.width,
                y = -4.dp.roundToPx() + placementOffset
            )
        }
    }
}

private sealed interface LazyStressState {
    data object Suggestions : LazyStressState
    data object Loading : LazyStressState
    data object Results : LazyStressState
}

@Composable
private fun LazyListStressCase() {
    val listState = rememberLazyListState()
    var state by remember { mutableStateOf<LazyStressState>(LazyStressState.Suggestions) }
    var updates by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            state = LazyStressState.Suggestions
            delay(160)
            state = LazyStressState.Loading
            delay(40)
            state = LazyStressState.Results
            updates++
            delay(160)
        }
    }
    LaunchedEffect(listState) {
        while (true) {
            listState.scrollBy(24f)
            delay(16)
        }
    }

    Text(
        text = "Shared LazyListState + two AnimatedContent trees. Updates: $updates",
        style = MaterialTheme.typography.bodyMedium
    )
    AnimatedContent(
        targetState = state.headerItems(),
        contentKey = { animatedState -> animatedState.javaClass },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Lazy stress header"
    ) { headerItems ->
        Column {
            headerItems.forEachIndexed { index, itemState ->
                BaselineOverlay(
                    modifier = Modifier.fillMaxWidth(),
                    index = index,
                    state = itemState.state.toOverlayState()
                )
            }
        }
    }
    AnimatedContent(
        targetState = state.bodyItems(),
        contentKey = { animatedState -> animatedState.javaClass },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Lazy stress content"
    ) { bodyItems ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { },
            userScrollEnabled = true
        ) {
            items(bodyItems) { itemState ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(2) { column ->
                        BaselineOverlay(
                            modifier = Modifier
                                .weight(1f),
                            index = itemState.index * 2 + column,
                            state = itemState.state.toOverlayState()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StableSearchFlowCase() {
    val listState = rememberLazyListState()
    var showingResults by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(700)
        showingResults = true
    }

    Text(
        text = "Search: sample",
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = if (showingResults) {
            "Results loaded. Perform fast downward flings."
        } else {
            "Loading results..."
        },
        style = MaterialTheme.typography.bodyMedium
    )
    AnimatedContent(
        targetState = if (showingResults) GenericSearchContent.Results else GenericSearchContent.Suggestions,
        contentKey = { animatedState -> animatedState.javaClass },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Stable search transition"
    ) { content ->
        val items = when (content) {
            GenericSearchContent.Suggestions -> GenericSearchSuggestions
            GenericSearchContent.Results -> GenericSearchResults
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items) { item ->
                GenericSearchRow(item = item)
            }
        }
    }
}

private enum class GenericSearchContent {
    Suggestions,
    Results
}

private data class GenericSearchItem(
    val index: Int,
    val variant: GenericSearchVariant
)

private enum class GenericSearchVariant {
    Tagged,
    Badged,
    Plain
}

private val GenericSearchSuggestions = List(18) { index ->
    GenericSearchItem(index, GenericSearchVariant.Plain)
}

private val GenericSearchResults = List(160) { index ->
    GenericSearchItem(
        index = index,
        variant = when (index % 3) {
            0 -> GenericSearchVariant.Tagged
            1 -> GenericSearchVariant.Badged
            else -> GenericSearchVariant.Plain
        }
    )
}

@Composable
private fun GenericSearchRow(item: GenericSearchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (item.variant) {
            GenericSearchVariant.Plain -> BasicText(
                text = "Search result ${item.index}",
                modifier = Modifier
                    .weight(1f),
                style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
            )
            GenericSearchVariant.Tagged -> BaselineOverlay(
                modifier = Modifier
                    .weight(1f)
                    .dynamicSquareLayout(),
                index = item.index,
                state = OverlayState.Results
            )
            GenericSearchVariant.Badged -> BaselineOverlay(
                modifier = Modifier
                    .weight(1f)
                    .dynamicSquareLayout(),
                index = item.index,
                state = OverlayState.Loading
            )
        }
        BasicText(
            text = if (item.index % 2 == 0) "1000.00" else "10000.00",
            modifier = Modifier
                .onGloballyPositioned { },
            style = androidx.compose.ui.text.TextStyle(
                fontSize = if (item.index % 2 == 0) 12.sp else 28.sp
            )
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

private data class StressItem(
    val index: Int,
    val state: LazyStressState
)

private class SuggestionsItems : AbstractList<StressItem>() {
    private val items = List(30) { StressItem(it, LazyStressState.Suggestions) }

    override val size: Int get() = items.size

    override fun get(index: Int): StressItem = items[index]
}

private class LoadingItems(
    private val state: LazyStressState
) : AbstractList<StressItem>() {
    override val size: Int get() = 1

    override fun get(index: Int): StressItem {
        check(index == 0)
        return StressItem(0, state)
    }
}

private class ResultsItems : AbstractList<StressItem>() {
    private val items = List(120) { StressItem(it, LazyStressState.Results) }

    override val size: Int get() = items.size

    override fun get(index: Int): StressItem = items[index]
}

private fun LazyStressState.headerItems(): List<StressItem> = when (this) {
    LazyStressState.Suggestions -> emptyList()
    LazyStressState.Loading -> LoadingItems(this)
    LazyStressState.Results -> ResultsItems().subList(0, 2)
}

private fun LazyStressState.bodyItems(): List<StressItem> = when (this) {
    LazyStressState.Suggestions -> SuggestionsItems()
    LazyStressState.Loading -> LoadingItems(this)
    LazyStressState.Results -> ResultsItems()
}

private fun LazyStressState.toOverlayState(): OverlayState = when (this) {
    LazyStressState.Suggestions -> OverlayState.Suggestions
    LazyStressState.Loading -> OverlayState.Loading
    LazyStressState.Results -> OverlayState.Results
}

@Composable
private fun RectListReproducerTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
