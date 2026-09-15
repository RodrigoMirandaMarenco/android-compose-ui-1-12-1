package com.example.rectlistreproducer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp

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
    Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = List(100) { it },
                key = { it }
            ) { index ->
                ListItem(index = index)
            }
        }
    }
}

@Composable
private fun ListItem(index: Int) {
    OverlayLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        anchor = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF4F46E5))
            )
        }
    )
}

@Composable
private fun OverlayLayout(
    anchor: @Composable () -> Unit,
    marker: @Composable () -> Unit,
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
        val markerPlaceable = measurables
            .first { it.layoutId == MarkerId }
            .measure(constraints)

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
