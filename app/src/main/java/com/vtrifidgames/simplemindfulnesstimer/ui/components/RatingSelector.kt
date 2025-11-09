package com.vtrifidgames.simplemindfulnesstimer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating

fun ratingToStars(rating: Rating): Int = when (rating) {
    Rating.VERY_POOR -> 1
    Rating.POOR -> 2
    Rating.AVERAGE -> 3
    Rating.GOOD -> 4
    Rating.EXCELLENT -> 5
}

fun starsToRating(stars: Int): Rating = when (stars) {
    1 -> Rating.VERY_POOR
    2 -> Rating.POOR
    3 -> Rating.AVERAGE
    4 -> Rating.GOOD
    5 -> Rating.EXCELLENT
    else -> Rating.AVERAGE
}

@Composable
fun StarRatingSelector(
    selectedStars: Int,
    onStarSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    iconPadding: Dp = 4.dp,
    inactiveAlpha: Float = 0.25f
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        for (star in 1..5) {
            val icon = if (star <= selectedStars) Icons.Filled.Star else Icons.Outlined.Star
            Icon(
                imageVector = icon,
                contentDescription = "Star $star",
                modifier = Modifier
                    .size(iconSize)
                    .padding(iconPadding)
                    .clickable { onStarSelected(star) }
                    .then(if (star > selectedStars) Modifier.alpha(inactiveAlpha) else Modifier),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
