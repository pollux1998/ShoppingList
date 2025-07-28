package com.dedeandriansyah.shoppinglist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListApp()
        }
    }
}

data class ShoppingItem(
    val id: Int = System.currentTimeMillis().hashCode(),
    val name: String,
    var isChecked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFFEADDFF),
            surface = Color(0xFFFFFBFE),
            onSurface = Color(0xFF1C1B1F)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            val context = LocalContext.current
            var items by remember { mutableStateOf(loadItems(context)) }
            var showDialog by remember { mutableStateOf(false) }
            var newItemText by remember { mutableStateOf("") }

            LaunchedEffect(items) {
                saveItems(context, items)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Header
                    Text(
                        text = "Shopping List",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // List of items
                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items yet\nTap + to add",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                ShoppingItemCard(
                                    item = item,
                                    onCheckedChange = { isChecked ->
                                        items = items.map { currentItem ->
                                            if (currentItem.id == item.id) currentItem.copy(isChecked = isChecked)
                                            else currentItem
                                        }
                                    },
                                    onDelete = {
                                        items = items.filter { it.id != item.id }
                                    }
                                )
                            }
                        }
                    }
                }

                // Floating Action Button
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add item")
                }

                // Add Item Dialog
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Add New Item", style = MaterialTheme.typography.titleLarge) },
                        text = {
                            OutlinedTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                label = { Text("Item name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newItemText.isNotBlank()) {
                                        items = items + ShoppingItem(name = newItemText)
                                        newItemText = ""
                                        showDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Add", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDialog = false },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = item.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Data persistence functions
private fun saveItems(context: Context, items: List<ShoppingItem>) {
    val sharedPref = context.getSharedPreferences("SHOPPING_PREFS", Context.MODE_PRIVATE)
    val json = Gson().toJson(items)
    sharedPref.edit().putString("SHOPPING_ITEMS", json).apply()
}

private fun loadItems(context: Context): List<ShoppingItem> {
    val sharedPref = context.getSharedPreferences("SHOPPING_PREFS", Context.MODE_PRIVATE)
    val json = sharedPref.getString("SHOPPING_ITEMS", null)
    return if (json != null) {
        Gson().fromJson(json, object : TypeToken<List<ShoppingItem>>() {}.type)
    } else {
        emptyList()
    }
}