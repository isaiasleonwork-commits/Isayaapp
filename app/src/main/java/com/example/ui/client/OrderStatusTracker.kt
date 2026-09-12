package com.example.ui.client

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeliveryMode
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.*
import java.net.URLEncoder
import java.util.Locale

@Composable
fun OrderStatusTracker(
    order: Order,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val context = LocalContext.current
    val currentStep = order.status.step

    // Dynamic Color Tokens
    val bg = if (isDarkMode) DeepBlack else IosLightBg
    val surfaceBg = if (isDarkMode) DarkSurface else IosLightSurface
    val cardSecondaryBg = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorder
    val borderSubtle = if (isDarkMode) DarkBorder.copy(alpha = 0.6f) else IosLightBorderSubtle
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val textSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val textMuted = if (isDarkMode) TextMuted else IosTextMuted
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldBorder = if (isDarkMode) GoldDark else IosGoldBorder

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Order Header Card with 24dp rounded corners
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SEGUIMIENTO EN VIVO",
                            color = goldColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Orden #${order.orderNumber}",
                            color = textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        color = when (order.status) {
                            OrderStatus.PENDING_PAYMENT -> StatusOrange.copy(alpha = 0.15f)
                            OrderStatus.PAYMENT_CONFIRMED -> StatusBlue.copy(alpha = 0.15f)
                            OrderStatus.IN_KITCHEN -> (if (isDarkMode) SushiRed else IosRedPrimary).copy(alpha = 0.15f)
                            OrderStatus.DISPATCHED_OR_READY -> StatusGreen.copy(alpha = 0.15f)
                            OrderStatus.DELIVERED -> StatusGreen.copy(alpha = 0.25f)
                        },
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (order.status) {
                                OrderStatus.PENDING_PAYMENT -> StatusOrange
                                OrderStatus.PAYMENT_CONFIRMED -> StatusBlue
                                OrderStatus.IN_KITCHEN -> if (isDarkMode) SushiRed else IosRedPrimary
                                OrderStatus.DISPATCHED_OR_READY -> StatusGreen
                                OrderStatus.DELIVERED -> StatusGreen
                            }
                        )
                    ) {
                        Text(
                            text = order.status.label,
                            color = when (order.status) {
                                OrderStatus.PENDING_PAYMENT -> StatusOrange
                                OrderStatus.PAYMENT_CONFIRMED -> StatusBlue
                                OrderStatus.IN_KITCHEN -> if (isDarkMode) SushiRedLight else IosRedPrimary
                                OrderStatus.DISPATCHED_OR_READY -> StatusGreen
                                OrderStatus.DELIVERED -> StatusGreen
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = order.status.description,
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4-STAGE PROGRESS BAR
        Text(
            text = "Estatus de Preparación",
            color = goldColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val stages = listOf(
                    Triple(1, "Pedido Recibido", Icons.Default.Receipt),
                    Triple(2, "Pago Confirmado", Icons.Default.CheckCircle),
                    Triple(3, "En Cocina", Icons.Default.OutdoorGrill),
                    Triple(
                        4,
                        if (order.deliveryMode == DeliveryMode.DELIVERY) "En Camino" else "Listo para Retirar",
                        if (order.deliveryMode == DeliveryMode.DELIVERY) Icons.Default.DeliveryDining else Icons.Default.Storefront
                    )
                )

                stages.forEachIndexed { index, (step, label, icon) ->
                    val isCompleted = currentStep >= step
                    val isCurrent = currentStep == step

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon circle
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isCompleted -> goldColor
                                else -> cardSecondaryBg
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCompleted) goldColor else borderSubtle
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isCompleted) Color.White else textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                color = if (isCompleted) textPrimary else textMuted,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium
                            )
                            if (isCurrent) {
                                Text(
                                    text = "Paso activo",
                                    color = goldColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completado",
                                tint = StatusGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Connecting vertical line between steps
                    if (index < stages.size - 1) {
                        Box(
                            modifier = Modifier
                                .padding(start = 18.dp, top = 2.dp, bottom = 2.dp)
                                .width(2.dp)
                                .height(24.dp)
                                .background(if (currentStep > step) goldColor else borderColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WHATSAPP DIRECT ACTION BUTTON with 24dp rounded corners
        Button(
            onClick = {
                try {
                    val message = "Hola Isaya Sushi! Quisiera consultar sobre el estatus de mi pedido #${order.orderNumber} a nombre de ${order.customerInfo.fullName}."
                    val encoded = URLEncoder.encode(message, "UTF-8")
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=584141234567&text=$encoded")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("contact_whatsapp_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "WhatsApp",
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Contactar por WhatsApp (#${order.orderNumber})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Summary Details Card with 24dp rounded corners
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Detalles de la Entrega",
                    color = goldColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cliente: ${order.customerInfo.fullName}", color = textPrimary, fontSize = 13.sp)
                Text("WhatsApp: ${order.customerInfo.whatsappPhone}", color = textSecondary, fontSize = 12.sp)
                if (order.deliveryMode == DeliveryMode.DELIVERY) {
                    order.customerInfo.deliveryZone?.let { zone ->
                        Text("Zona: ${zone.name} (Flete: $${String.format(Locale.US, "%.2f", order.deliveryFee)} USD)", color = goldColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Dirección: ${order.customerInfo.deliveryAddress}", color = textSecondary, fontSize = 12.sp)
                    if (order.customerInfo.referencePoint.isNotBlank()) {
                        Text("Ref: ${order.customerInfo.referencePoint}", color = textMuted, fontSize = 11.sp)
                    }
                } else {
                    Text("Modalidad: Retiro en Restaurante Isaya ($0.00 USD flete)", color = textSecondary, fontSize = 12.sp)
                }

                HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 10.dp))

                Text(
                    text = "Pago Móvil: Ref. final •••• ${order.paymentProof.referenceDigits}",
                    color = goldColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Total Pagado: $${String.format(Locale.US, "%.2f", order.total)} USD",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Return to Menu Button with 24dp rounded corners
        OutlinedButton(
            onClick = onBackToMenu,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("back_to_menu_button"),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = surfaceBg,
                contentColor = goldColor
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, goldColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Volver al Menú / Hacer Otro Pedido", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
