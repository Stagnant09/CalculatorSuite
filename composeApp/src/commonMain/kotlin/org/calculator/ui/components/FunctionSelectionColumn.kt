package org.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Define a sealed class or enum for cleaner function handling (Reused from previous code)
sealed class GraphFunction(val label: String) {
    object ShortestPath : GraphFunction("Shortest Path (Dijkstra/BFS)")
    object MaximumFlow : GraphFunction("Maximum Flow (Ford-Fulkerson)")
    object MinimumSpanningTree : GraphFunction("Minimum Spanning Tree (Prim/Kruskal)")
    object Connectivity : GraphFunction("Connectivity (DFS/BFS)")
    object TopologicalSort : GraphFunction("Topological Sort (DAG)")
}

// List of all functions to display (Reused from previous code)
val graphFunctions = listOf(
    GraphFunction.ShortestPath,
    GraphFunction.MaximumFlow,
    GraphFunction.MinimumSpanningTree,
    GraphFunction.Connectivity,
    GraphFunction.TopologicalSort
)

/**
 * This composable displays a column of buttons for selecting graph-related functions
 * such as Shortest Path, Maximum Flow, etc.
 *
 * @param onFunctionSelected A lambda function to handle the selection of a graph function.
 * It receives the selected GraphFunction object.
 */
@Composable
fun FunctionSelectionColumn(
    onFunctionSelected: (GraphFunction) -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        graphFunctions.forEach { function ->
            Button(
                onClick = {
                    onFunctionSelected(function)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(function.label)
            }
        }
    }
}


