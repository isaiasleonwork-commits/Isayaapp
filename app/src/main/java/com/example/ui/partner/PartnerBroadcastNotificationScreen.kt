package com.example.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PushNotificationMessage
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerBroadcastNotificationScreen(
    notifications: List<PushNotificationMessage>,
    onBroadcast: (title: String, body: String, promoTag: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var promoTag by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val quickTemplates = listOf(
        Triple("🍣 2x1 en Rollos Tempura", "¡Pide tu Tempura Roll o Maracay Roll y lleva el segundo a mitad de precio hoy!", "PROMO2X1"),
        Triple("⚡ Cocina Abierta & Activa", "¡Nuestros cocineros están listos! Ordena tus rollos favoritos con entrega rápida.", "COCINA_OPEN"),
        Triple("🛵 Delivery Gratis Isaya", "Disfruta de envío a domicilio 100% gratuito en órdenes superiores a $20 USD.", "ENVIOGRATIS"),
        Triple("🍱 Combo Especial 30 Piezas", "30 piezas crujientes con relleno completo y salsas de la casa con 15% OFF.", "ISAYA15")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SushiRed.copy(alpha = 0.2f))
                            .border(1.dp, SushiRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DISPARADOR PUSH MASIVO",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Emite notificaciones a todos los clientes conectados a Firebase Firestore en tiempo real.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Quick Templates Row
        item {
            Text(
                text = "Plantillas Rápidas:",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickTemplates.forEach { template ->
                    FilterChip(
                        selected = title == template.first,
                        onClick = {
                            title = template.first
                            body = template.second
                            promoTag = template.third
                            feedbackMessage = null
                        },
                        label = {
                            Text(
                                text = template.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = DeepBlack,
                            containerColor = DarkCardElevated,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = DarkCardBorder,
                            selectedBorderColor = GoldPrimary,
                            enabled = true,
                            selected = title == template.first
                        )
                    )
                }
            }
        }

        // Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Redactar Notificación",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la Notificación", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("Ej. 🍣 2x1 en Rolls Tempura hoy", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkCardElevated,
                            unfocusedContainerColor = DarkCardElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("push_input_title")
                    )

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Mensaje a Clientes", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("Describe la promoción o aviso para tus clientes...", color = TextMuted, fontSize = 12.sp) },
                        minLines = 3,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkCardElevated,
                            unfocusedContainerColor = DarkCardElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("push_input_body")
                    )

                    OutlinedTextField(
                        value = promoTag,
                        onValueChange = { promoTag = it },
                        label = { Text("Código de Descuento / Tag (Opcional)", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("Ej. PROMO2X1", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkCardElevated,
                            unfocusedContainerColor = DarkCardElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("push_input_tag")
                    )

                    feedbackMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && body.isNotBlank()) {
                                onBroadcast(title, body, promoTag.ifBlank { null })
                                feedbackMessage = "✅ ¡Notificación Push disparada con éxito a todos los clientes!"
                                title = ""
                                body = ""
                                promoTag = ""
                            }
                        },
                        enabled = title.isNotBlank() && body.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SushiRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("push_submit_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Disparar Push a Clientes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Broadcast History List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Notificaciones Disparadas",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${notifications.size} enviadas",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (notifications.isEmpty()) {
            item {
                Text(
                    text = "No has disparado notificaciones aún.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            items(notifications, key = { it.id }) { item ->
                val timeFormatted = remember(item.timestamp) {
                    SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            item.promoTag?.let { tag ->
                                Surface(
                                    color = SushiRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SushiRed)
                                ) {
                                    Text(
                                        text = tag,
                                        color = SushiRedLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.body,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Text(
                            text = timeFormatted,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
