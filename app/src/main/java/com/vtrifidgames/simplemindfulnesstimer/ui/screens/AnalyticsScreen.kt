package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.*
import java.util.Locale

@Composable
fun AnalyticsScreen(navController: NavController) {
    // Obtain the ViewModel
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModelFactory(repository))

    // Observe the selected interval and analytics data
    val selectedInterval by viewModel.selectedInterval.collectAsState()
    val analyticsData by viewModel.analyticsData.collectAsState()

    // Main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Top row: interval selection.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { viewModel.setSelectedInterval(AnalyticsInterval.DAYS) }) {
                Text("Days")
            }
            Button(onClick = { viewModel.setSelectedInterval(AnalyticsInterval.WEEKS) }) {
                Text("Weeks")
            }
            Button(onClick = { viewModel.setSelectedInterval(AnalyticsInterval.MONTHS) }) {
                Text("Months")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Helper function to format durations.
        fun formatHumanDuration(seconds: Double): String {
            return when {
                seconds.isNaN() || seconds == 0.0 -> ""
                seconds < 60.0 -> String.format(Locale.getDefault(), "%.0f seconds", seconds)
                seconds < 3600.0 -> {
                    val minutes = seconds / 60.0
                    if (seconds % 60.0 < 15.0) String.format(Locale.getDefault(), "%.0f minutes", minutes)
                    else String.format(Locale.getDefault(), "%.1f minutes", minutes)
                }
                else -> {
                    val hours = seconds / 3600.0
                    String.format(Locale.getDefault(), "%.1f hours", hours)
                }
            }
        }

        // ChartCards for each metric.
        VerticalChartCard(
            title = "Total Time Meditated",
            valueDisplay = formatHumanDuration(analyticsData.totalMeditationTime.average().takeIf { !it.isNaN() } ?: 0.0),
            data = entryModelOf(*(analyticsData.totalMeditationTime.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            xAxisTitle = "Time",
            yAxisTitle = "Seconds"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Total Time (Incl. Paused)",
            valueDisplay = formatHumanDuration(analyticsData.totalTime.average().takeIf { !it.isNaN() } ?: 0.0),
            data = entryModelOf(*(analyticsData.totalTime.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            xAxisTitle = "Time",
            yAxisTitle = "Seconds"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Number of Sessions",
            valueDisplay = if (analyticsData.sessions.average().isNaN() || analyticsData.sessions.average() == 0.0) "" else String.format(Locale.getDefault(), "%.1f", analyticsData.sessions.average()),
            data = entryModelOf(*(analyticsData.sessions.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            xAxisTitle = "Time",
            yAxisTitle = "Count"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Number of Pauses",
            valueDisplay = if (analyticsData.pauses.average().isNaN() || analyticsData.pauses.average() == 0.0) "" else String.format(Locale.getDefault(), "%.1f", analyticsData.pauses.average()),
            data = entryModelOf(*(analyticsData.pauses.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            xAxisTitle = "Time",
            yAxisTitle = "Count"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Average Rating",
            valueDisplay = if (analyticsData.rating.average().isNaN() || analyticsData.rating.average() == 0.0) "" else String.format(Locale.getDefault(), "%.1f", analyticsData.rating.average()),
            data = entryModelOf(*(analyticsData.rating.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            xAxisTitle = "Time",
            yAxisTitle = "Rating"
        )

        // Add spacer before the button
        Spacer(modifier = Modifier.height(16.dp))

        // Add the button to navigate to SessionHistoryScreen
        Button(
            onClick = { navController.navigate("session_history") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Session Data")
        }
    }
}

@Composable
fun VerticalChartCard(
    title: String,
    valueDisplay: String,
    data: com.patrykandpatrick.vico.core.entry.ChartEntryModel,
    xAxisLabels: List<String>,
    xAxisTitle: String,
    yAxisTitle: String
) {
    // Create an axis label component for the axes.
    val label = com.patrykandpatrick.vico.compose.axis.axisLabelComponent()
    // Create a vertical axis (start) with default parameters.
    val startAxis = rememberStartAxis(
        label = label,
        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside
    )
    // Create an AxisValueFormatter for custom x-axis labels.
    val xAxisValueFormatter = object : AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
        override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
            val index = value.toInt()
            return xAxisLabels.getOrElse(index) { "" }
        }
    }
    // Create a horizontal axis (bottom) with custom labels.
    val bottomAxis = rememberBottomAxis(
        label = label,
        valueFormatter = xAxisValueFormatter
    )
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = androidx.compose.material3.CardDefaults.outlinedCardColors(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (valueDisplay.isNotEmpty()) {
                    Text(text = valueDisplay, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Render the chart with axes.
            Chart(
                chart = columnChart(),
                model = data,
                startAxis = startAxis,
                bottomAxis = bottomAxis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = xAxisTitle,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = yAxisTitle,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}