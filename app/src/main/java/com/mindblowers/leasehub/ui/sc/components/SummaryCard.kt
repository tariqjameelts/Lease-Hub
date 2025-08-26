package com.mindblowers.leasehub.ui.sc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportSummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun ReportsTable(headers: List<String>, rows: List<List<String>>) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
            headers.forEach { header ->
                Text(
                    header,
                    Modifier.weight(1f).padding(8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach {
                    Text(it, Modifier.weight(1f).padding(8.dp))
                }
            }
        }
    }
}
