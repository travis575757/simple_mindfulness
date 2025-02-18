package com.vtrifidgames.simplemindfulnesstimer.ui.components

import android.widget.LinearLayout
import android.widget.NumberPicker
import android.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun DurationPickerDialog(
    initialMinutes: Int,
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (durationInSeconds: Long) -> Unit
) {
    val context = LocalContext.current
    var pickedMinutes = initialMinutes
    var pickedSeconds = initialSeconds

    // Launch the dialog as a side-effect so it shows once.
    LaunchedEffect(Unit) {
        // Create a horizontal LinearLayout container.
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 16, 32, 16)
        }
        // Minutes NumberPicker with 2-digit formatting.
        val minutesPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            value = initialMinutes
            setFormatter { value -> String.format("%02d", value) }
            setOnValueChangedListener { _, _, newVal -> pickedMinutes = newVal }
        }
        // Seconds NumberPicker with 2-digit formatting.
        val secondsPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            value = initialSeconds
            setFormatter { value -> String.format("%02d", value) }
            setOnValueChangedListener { _, _, newVal -> pickedSeconds = newVal }
        }
        container.addView(minutesPicker)
        container.addView(secondsPicker)

        AlertDialog.Builder(context)
            .setTitle("Choose Duration")
            .setView(container)
            .setPositiveButton("OK") { _, _ ->
                onConfirm(pickedMinutes * 60L + pickedSeconds)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onDismiss()
            }
            .setOnCancelListener {
                onDismiss()
            }
            .create()
            .show()
    }
}
