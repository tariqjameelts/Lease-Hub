package com.mindblowers.leasehub.ui.sc.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontSize = 13.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes", color = Color(0xFF00796B))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No", color = Color.Gray)
            }
        }
    )
}