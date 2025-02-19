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
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.*
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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
            data = entryModelOf(*(analyticsData.totalMeditationTime.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            yAxisTitle = "Seconds"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Total Time (Incl. Paused)",
            data = entryModelOf(*(analyticsData.totalTime.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            yAxisTitle = "Seconds"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Number of Sessions",
            data = entryModelOf(*(analyticsData.sessions.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            yAxisTitle = "Count"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Number of Pauses",
            data = entryModelOf(*(analyticsData.pauses.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
            yAxisTitle = "Count"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VerticalChartCard(
            title = "Average Rating",
            data = entryModelOf(*(analyticsData.rating.toTypedArray())),
            xAxisLabels = analyticsData.xAxisLabels,
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
    data: ChartEntryModel,
    xAxisLabels: List<String>,
    yAxisTitle: String
) {
    // Create an axis label component for the axes.
    val label = axisLabelComponent()

    // Compute maxY and minY from the data.
    val maxYData = data.maxY
    val minY = 0f // For all charts, starting from 0

    // Determine tickStep and valueFormatter based on yAxisTitle and maxYData
    val (tickStep, valueFormatter) = when (yAxisTitle) {
        "Rating" -> {
            // Ratings from 1 to 5
            Pair(1f, AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
                value.toInt().toString()
            })
        }
        "Count" -> {
            // For counts (e.g., pauses, sessions), compute nice tick step
            val tickStep = calculateNiceTickStep(maxYData, 5).coerceAtLeast(1f)
            Pair(tickStep, AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
                value.toInt().toString()
            })
        }
        "Seconds" -> {
            // For times, compute nice time-based tick step
            val tickStep = calculateNiceTimeTickStep(maxYData, 5)
            Pair(tickStep, AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
                val totalSeconds = value.toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                when {
                    totalSeconds < 60 -> "${totalSeconds}s"
                    seconds == 0 -> "${minutes}m"
                    else -> "${minutes}m ${seconds}s"
                }
            })
        }
        else -> {
            // Default, compute tick step
            val tickStep = calculateNiceTickStep(maxYData, 5)
            Pair(tickStep, AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
                value.toString()
            })
        }
    }

    // Adjusted maxY to the next multiple of tickStep
    val adjustedMaxY = ((ceil(maxYData / tickStep) * tickStep)).toFloat()

    // Implement a custom AxisItemPlacer.Vertical
    val itemPlacer = object : AxisItemPlacer.Vertical {
        override fun getLabelValues(
            context: ChartDrawContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical
        ): List<Float> {
            return generateTickPositions()
        }

        override fun getHeightMeasurementLabelValues(
            context: MeasureContext,
            position: AxisPosition.Vertical
        ): List<Float> {
            return generateTickPositions()
        }

        override fun getWidthMeasurementLabelValues(
            context: MeasureContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical
        ): List<Float> {
            return generateTickPositions()
        }

        override fun getLineValues(
            context: ChartDrawContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical
        ): List<Float>? {
            // Return null to use label positions for lines
            return null
        }

        override fun getTopVerticalAxisInset(
            verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
            maxLabelHeight: Float,
            maxLineThickness: Float
        ): Float {
            // Return any required top inset (adjust if necessary)
            return 0f
        }

        override fun getBottomVerticalAxisInset(
            verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
            maxLabelHeight: Float,
            maxLineThickness: Float
        ): Float {
            // Return any required bottom inset (adjust if necessary)
            return 0f
        }

        // Helper function to generate tick positions
        private fun generateTickPositions(): List<Float> {
            val positions = mutableListOf<Float>()
            var currentValue = minY
            while (currentValue <= adjustedMaxY) {
                positions.add(currentValue)
                currentValue += tickStep
            }
            return positions
        }
    }

    // Configure the start axis with the custom item placer and value formatter
    val startAxis = rememberStartAxis(
        label = label,
        valueFormatter = valueFormatter,
        itemPlacer = itemPlacer,
        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside
    )

    // X-axis formatter remains the same.
    val xAxisValueFormatter = object : AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
        override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
            val index = value.toInt()
            return xAxisLabels.getOrElse(index) { "" }
        }
    }
    val bottomAxis = rememberBottomAxis(
        label = label,
        valueFormatter = xAxisValueFormatter
    )

    // Configure the chart with axisValuesOverrider to fix minY and maxY
    val chart = columnChart().apply {
        axisValuesOverrider = AxisValuesOverrider.fixed(
            minY = minY,
            maxY = adjustedMaxY
        )
    }

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
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Render the chart with the custom axes.
            Chart(
                chart = chart,
                model = data,
                startAxis = startAxis,
                bottomAxis = bottomAxis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = yAxisTitle,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}

// Adjusted calculateNiceTickStep function
fun calculateNiceTickStep(maxValue: Float, tickCount: Int): Float {
    if (maxValue <= 0f) return 1f
    val rawStep = maxValue / tickCount
    val exponent = floor(log10(rawStep.toDouble())).toInt()
    val base = 10f.pow(exponent.toFloat())
    // Choose from a set of multipliers.
    val possibleSteps = listOf(1f, 2f, 5f, 10f, 20f, 50f).map { it * base }
    val tickStep = possibleSteps.firstOrNull { it >= rawStep } ?: (10f * base)
    return tickStep.coerceAtLeast(1f) // Ensure tickStep is at least 1
}

// New calculateNiceTimeTickStep function
fun calculateNiceTimeTickStep(maxValue: Float, tickCount: Int): Float {
    if (maxValue <= 0f) return 30f // Default to 30 seconds
    val rawStep = maxValue / tickCount
    val possibleSteps = listOf(
        15f, 30f, 45f,
        60f, 90f, 120f, 150f, 180f, 210f, 240f, 270f, 300f,
        600f, 900f, 1200f, 1500f, 1800f, 3600f
    )
    return possibleSteps.firstOrNull { it >= rawStep } ?: possibleSteps.last()
}