package com.example.compteur

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compteur.ui.theme.CompteurTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import android.os.SystemClock

data class Counter(val title: String, val startDate: LocalDate, val color: Color)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompteurTheme {
                MainUI()
            }
        }
    }
}

@Composable
fun MainUI() {
    val counters = remember {
        mutableStateListOf(
            Counter("Arrêt cigarette", LocalDate.of(2024, 6, 10), Color(0xFF4CAF50)),
            Counter("Méditation", LocalDate.of(2024, 7, 1), Color(0xFF2196F3)),
            Counter("Course à pied", LocalDate.of(2024, 5, 20), Color(0xFFFF9800))
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Compteur",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            counters.forEach { counter ->
                CounterCard(
                    counter = counter,
                    onDeleteClick = { counters.remove(counter) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAddDialog) {
        AddCounterDialog(
            onDismiss = { showAddDialog = false },
            onCounterAdded = { title, startDate, color ->
                counters.add(Counter(title, startDate, color))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CounterCard(
    counter: Counter,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val days = getDaysSince(counter.startDate)

    var lastClickTime by remember { mutableStateOf(0L) }
    val cooldown = 2000L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(counter.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = counter.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = counter.color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Commencé il y a $days jours",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Gray)
        }

        IconButton(
            onClick = {
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime > cooldown) {
                    lastClickTime = currentTime
                    shareCounter(context, counter.title, days)
                }
            }
        ) {
            Icon(Icons.Default.Share, contentDescription = "Partager", tint = Color.Gray)
        }
    }
}

@Composable
fun AddCounterDialog(
    onDismiss: () -> Unit,
    onCounterAdded: (String, LocalDate, Color) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedColor by remember { mutableStateOf(getRandomColor()) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un compteur") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre du compteur") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        ).show()
                    }
                ) {
                    Text("Date de départ: ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Couleur: ")
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(selectedColor, RoundedCornerShape(4.dp))
                            .clickable { selectedColor = getRandomColor() }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCounterAdded(title, selectedDate, selectedColor)
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Ajouter", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

fun getDaysSince(startDate: LocalDate): Long {
    return ChronoUnit.DAYS.between(startDate, LocalDate.now())
}

fun getRandomColor(): Color {
    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
        Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFF009688)
    )
    return colors[Random.nextInt(colors.size)]
}

fun shareCounter(context: Context, title: String, days: Long) {
    val shareText = "J'ai réussi à tenir $days jours pour mon objectif '$title' ! Rejoignez-moi sur Compteur."
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
}

@Preview(showBackground = true)
@Composable
fun PreviewUI() {
    CompteurTheme {
        MainUI()
    }
}