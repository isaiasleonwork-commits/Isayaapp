package com.example.ui.partner

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.DeliveryMode
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PartnerOrdersScreen(
    orders: List<Order>,
    onUpdateStatus: (String, OrderStatus) -> Unit,
    onAssignDriver: (String, String) -> Unit,
    onSimulateOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var viewingReceiptOrder by remember { mutableStateOf<Order?>(null) }
    var viewingMapOrder by remember { mutableStateOf<Order?>(null) }

    val activeOrders = remember(orders) {
        orders.filter { it.status != OrderStatus.DELIVERED }
    }

    val filteredOrders = remember(activeOrders, selectedFilter) {
        if (selectedFilter == null) activeOrders else activeOrders.filter { it.status == selectedFilter }
    }

    val pendingCount = activeOrders.count { it.status == OrderStatus.PENDING_PAYMENT }
    val kitchenCount = activeOrders.count { it.status == OrderStatus.IN_KITCHEN }
    val dispatchedCount = activeOrders.count { it.status == OrderStatus.DISPATCHED_OR_READY }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Compact Monitor Bar with Simulator Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "En vivo",
                    tint = SushiRed,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "COMANDAS EN VIVO",
                    color = SushiRedLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "(${activeOrders.size})",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Simulate Button
            OutlinedButton(
                onClick = onSimulateOrder,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                    .height(28.dp)
                    .testTag("simulate_order_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(13.dp))
                    Text("Probar Alerta", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Compact iOS Status Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("Todos (${activeOrders.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SushiRed,
                    selectedLabelColor = TextPrimary,
                    containerColor = DarkCard,
                    labelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(30.dp)
            )

            FilterChip(
                selected = selectedFilter == OrderStatus.PENDING_PAYMENT,
                onClick = {
                    selectedFilter = if (selectedFilter == OrderStatus.PENDING_PAYMENT) null else OrderStatus.PENDING_PAYMENT
                },
                label = { Text("⏳ Pago ($pendingCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusOrange,
                    selectedLabelColor = DeepBlack,
                    containerColor = DarkCard,
                    labelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(30.dp)
            )

            FilterChip(
                selected = selectedFilter == OrderStatus.IN_KITCHEN,
                onClick = {
                    selectedFilter = if (selectedFilter == OrderStatus.IN_KITCHEN) null else OrderStatus.IN_KITCHEN
                },
                label = { Text("🍳 Cocina ($kitchenCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = DeepBlack,
                    containerColor = DarkCard,
                    labelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(30.dp)
            )

            FilterChip(
                selected = selectedFilter == OrderStatus.DISPATCHED_OR_READY,
                onClick = {
                    selectedFilter = if (selectedFilter == OrderStatus.DISPATCHED_OR_READY) null else OrderStatus.DISPATCHED_OR_READY
                },
                label = { Text("🛵 En Ruta ($dispatchedCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusGreen,
                    selectedLabelColor = DeepBlack,
                    containerColor = DarkCard,
                    labelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(30.dp)
            )
        }

        // Compact Orders List (Fits 3-4 cards on screen)
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay comandas en este estado.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    CompactPartnerOrderCard(
                        order = order,
                        onUpdateStatus = { newStatus -> onUpdateStatus(order.id, newStatus) },
                        onAssignDriver = { driverName -> onAssignDriver(order.id, driverName) },
                        onViewReceipt = { viewingReceiptOrder = order },
                        onViewMap = { viewingMapOrder = order }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // GPS Navigation & Customer Map Modal
    viewingMapOrder?.let { order ->
        PartnerOrderGpsMapDialog(
            order = order,
            onDismiss = { viewingMapOrder = null },
            onAssignDriver = { driverName ->
                onAssignDriver(order.id, driverName)
            }
        )
    }
    viewingReceiptOrder?.let { order ->
        Dialog(
            onDismissRequest = { viewingReceiptOrder = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // iOS Drag Handle / Top Indicator
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DarkBorder)
                            .align(Alignment.CenterHorizontally)
                    )

                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Comprobante #${order.orderNumber}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { viewingReceiptOrder = null },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DarkCardElevated)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(color = DarkBorder)

                    // Reference & Amount Highlight Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCard)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Referencia Pago Móvil:", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = "•••• ${order.paymentProof.referenceDigits.ifBlank { "4892" }}",
                                color = GoldPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total a Verificar:", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", order.total)} USD",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // REAL CAPTURE IMAGE DISPLAY (Maximized & Un-distorted ContentScale.Fit)
                    Text("Capture Adjunto por el Cliente:", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepBlack)
                            .border(1.dp, GoldDark, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = order.paymentProof.receiptImageRes),
                            contentDescription = "Capture de Pago Móvil",
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Client & Bank Info Card
                    Surface(
                        color = DarkCardElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• Cliente: ${order.customerInfo.fullName}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                // WhatsApp Action Button
                                TextButton(
                                    onClick = {
                                        val cleanPhone = order.customerInfo.whatsappPhone.replace(Regex("[^0-9]"), "")
                                        val uri = Uri.parse("https://wa.me/$cleanPhone")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("💬 WhatsApp", color = StatusGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("• Teléfono: ${order.customerInfo.whatsappPhone}", color = TextSecondary, fontSize = 11.sp)
                            Text("• Banco Receptor: 0105 Mercantil (C.I. 17.016.897)", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Driver Assignment Row (if Delivery)
                    if (order.deliveryMode == DeliveryMode.DELIVERY) {
                        Text("Asignar Repartidor:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = order.assignedDriver == "Edinson",
                                onClick = { onAssignDriver(order.id, "Edinson") },
                                label = { Text("🛵 Edinson", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DeepBlack,
                                    containerColor = DarkCard,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            )
                            FilterChip(
                                selected = order.assignedDriver == "Yohander",
                                onClick = { onAssignDriver(order.id, "Yohander") },
                                label = { Text("🛵 Yohander", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DeepBlack,
                                    containerColor = DarkCard,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            )
                        }
                    }

                    // One-Touch Approve Button
                    Button(
                        onClick = {
                            onUpdateStatus(order.id, OrderStatus.PAYMENT_CONFIRMED)
                            viewingReceiptOrder = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verificado • Aprobar Pago Móvil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * COMPACT iOS-STYLE ORDER CARD
 * Displays dense, high-hierarchy horizontal rows allowing 3-4 cards to fit cleanly on screen.
 */
@Composable
fun CompactPartnerOrderCard(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
    onAssignDriver: (String) -> Unit,
    onViewReceipt: () -> Unit,
    onViewMap: () -> Unit = {}
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(order.createdAt) { timeFormat.format(Date(order.createdAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("partner_order_${order.orderNumber}"),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ROW 1 (Header Flex): #Order + Time + Mode Pill + Spacer + Total + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "#${order.orderNumber}",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = formattedTime,
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Surface(
                    color = DeepBlack,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder)
                ) {
                    Text(
                        text = if (order.deliveryMode == DeliveryMode.DELIVERY) "🛵 Delivery" else "🛍️ Retiro",
                        color = GoldLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "$${String.format(Locale.US, "%.2f", order.total)}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Compact Status Pill
                Surface(
                    color = when (order.status) {
                        OrderStatus.PENDING_PAYMENT -> StatusOrange.copy(alpha = 0.2f)
                        OrderStatus.PAYMENT_CONFIRMED -> StatusBlue.copy(alpha = 0.2f)
                        OrderStatus.IN_KITCHEN -> SushiRed.copy(alpha = 0.2f)
                        OrderStatus.DISPATCHED_OR_READY -> StatusGreen.copy(alpha = 0.2f)
                        OrderStatus.DELIVERED -> StatusGreen.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = order.status.label,
                        color = when (order.status) {
                            OrderStatus.PENDING_PAYMENT -> StatusOrange
                            OrderStatus.PAYMENT_CONFIRMED -> StatusBlue
                            OrderStatus.IN_KITCHEN -> SushiRedLight
                            OrderStatus.DISPATCHED_OR_READY -> StatusGreen
                            OrderStatus.DELIVERED -> StatusGreen
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // ROW 2 (Horizontal Flex Info & Capture):
            // Left: Client Name, Phone, Items inline summary, Address
            // Right: Miniature clickable Capture Thumbnail + Ref
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Customer line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = order.customerInfo.fullName,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "• ${order.customerInfo.whatsappPhone}",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Items inline summary
                    Text(
                        text = "🍣 " + order.items.joinToString(", ") { "${it.quantity}x ${it.product.name}" },
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Address / Zone (if delivery) - Prominent GPS Navigation Button
                    if (order.deliveryMode == DeliveryMode.DELIVERY) {
                        val zoneText = order.customerInfo.deliveryZone?.name ?: "Maracay"
                        val addressText = if (order.customerInfo.deliveryAddress.isNotBlank()) order.customerInfo.deliveryAddress else "Ubicación GPS Cliente"

                        Surface(
                            color = DarkCardElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onViewMap() }
                                .testTag("btn_order_address_gps_${order.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "GPS",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "📍 $zoneText",
                                            color = GoldLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "• Coords GPS",
                                            color = TextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Text(
                                        text = addressText,
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    color = GoldPrimary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = DeepBlack, modifier = Modifier.size(11.dp))
                                        Text("Ver Mapa", color = DeepBlack, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // Compact Capture Thumbnail & Reference Box
                Surface(
                    color = DeepBlack,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (order.paymentProof.referenceDigits.isNotBlank()) GoldDark else DarkBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onViewReceipt() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Image(
                                painter = painterResource(id = order.paymentProof.receiptImageRes),
                                contentDescription = "Capture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column {
                            Text(
                                text = "Ref:",
                                color = TextSecondary,
                                fontSize = 8.sp
                            )
                            Text(
                                text = "••••${order.paymentProof.referenceDigits.takeLast(4).ifBlank { "4892" }}",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ROW 3 (Inline Actions & Driver Selector Flex):
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Driver mini selector (if delivery)
                if (order.deliveryMode == DeliveryMode.DELIVERY) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (order.assignedDriver == "Edinson") GoldPrimary else DeepBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (order.assignedDriver == "Edinson") GoldPrimary else DarkBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onAssignDriver("Edinson") }
                        ) {
                            Text(
                                text = "🛵 Edinson",
                                color = if (order.assignedDriver == "Edinson") DeepBlack else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = if (order.assignedDriver == "Yohander") GoldPrimary else DeepBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (order.assignedDriver == "Yohander") GoldPrimary else DarkBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onAssignDriver("Yohander") }
                        ) {
                            Text(
                                text = "🛵 Yohander",
                                color = if (order.assignedDriver == "Yohander") DeepBlack else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // One-Touch Action Button (Dense 30dp height)
                when (order.status) {
                    OrderStatus.PENDING_PAYMENT -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PAYMENT_CONFIRMED) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_approve_payment_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aprobar Pago", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OrderStatus.PAYMENT_CONFIRMED -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.IN_KITCHEN) },
                            colors = ButtonDefaults.buttonColors(containerColor = SushiRed, contentColor = TextPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_move_to_kitchen_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("A Cocina", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OrderStatus.IN_KITCHEN -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.DISPATCHED_OR_READY) },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = DeepBlack),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_dispatch_${order.id}")
                        ) {
                            Icon(
                                imageVector = if (order.deliveryMode == DeliveryMode.DELIVERY) Icons.Default.DeliveryDining else Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (order.deliveryMode == DeliveryMode.DELIVERY) {
                                    if (order.assignedDriver != null) "Despachar (${order.assignedDriver})" else "Despachar"
                                } else {
                                    "Listo para Retiro"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OrderStatus.DISPATCHED_OR_READY -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated, contentColor = StatusGreen),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Finalizar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OrderStatus.DELIVERED -> {
                        Surface(
                            color = StatusGreenContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Completado",
                                color = StatusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * HIGH-CRAFT GPS NAVIGATION & CUSTOMER MAP MODAL
 * Displays detailed GPS coordinates, customer address, visual map simulation, and direct navigation links.
 */
@Composable
fun PartnerOrderGpsMapDialog(
    order: Order,
    onDismiss: () -> Unit,
    onAssignDriver: (String) -> Unit
) {
    val context = LocalContext.current
    val customer = order.customerInfo
    val zoneName = customer.deliveryZone?.name ?: "Maracay Centro"
    val address = if (customer.deliveryAddress.isNotBlank()) customer.deliveryAddress else "Ubicación cliente"
    val coords = if (customer.gpsCoordinates.isNotBlank()) customer.gpsCoordinates else "10.2469° N, 67.5958° O"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Drag Indicator
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DarkBorder)
                        .align(Alignment.CenterHorizontally)
                )

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                text = "Navegación GPS • Pedido #${order.orderNumber}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Zona: $zoneName",
                                color = GoldLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DarkCardElevated)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(color = DarkBorder)

                // Customer & Address Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = customer.fullName.ifBlank { "Cliente Isaya" },
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = customer.whatsappPhone.ifBlank { "Sin número" },
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            // Direct WhatsApp button
                            if (customer.whatsappPhone.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val cleanPhone = customer.whatsappPhone.replace(Regex("[^0-9]"), "")
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=Hola%20${Uri.encode(customer.fullName)},%20te%20escribimos%20de%20Isaya%20Sushi%20sobre%20tu%20pedido%20%23${order.orderNumber}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Handle exception gracefully
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        HorizontalDivider(color = DarkBorder)

                        // Exact address & reference
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Column {
                                Text(
                                    text = address,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (customer.referencePoint.isNotBlank()) {
                                    Text(
                                        text = "Punto de referencia: ${customer.referencePoint}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Visual Map Preview (Vector Simulation)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepBlack),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Custom Map Canvas Drawing
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle grid street lines
                            val gridColor = Color(0xFF1E262B)
                            val roadColor = Color(0xFF2C3840)
                            val routeColor = GoldPrimary

                            for (i in 0..6) {
                                val y = h * (i / 6f)
                                drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), strokeWidth = 1.5f)
                            }
                            for (j in 0..8) {
                                val x = w * (j / 8f)
                                drawLine(gridColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, h), strokeWidth = 1.5f)
                            }

                            // Draw main roads
                            drawLine(roadColor, androidx.compose.ui.geometry.Offset(0f, h * 0.45f), androidx.compose.ui.geometry.Offset(w, h * 0.45f), strokeWidth = 5f)
                            drawLine(roadColor, androidx.compose.ui.geometry.Offset(w * 0.35f, 0f), androidx.compose.ui.geometry.Offset(w * 0.35f, h), strokeWidth = 5f)
                            drawLine(roadColor, androidx.compose.ui.geometry.Offset(w * 0.72f, 0f), androidx.compose.ui.geometry.Offset(w * 0.72f, h), strokeWidth = 4f)

                            // Draw route dashed line from Restaurant to Client
                            val startP = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.68f)
                            val midP1 = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.68f)
                            val midP2 = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.35f)
                            val endP = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.35f)

                            drawLine(routeColor, startP, midP1, strokeWidth = 4f)
                            drawLine(routeColor, midP1, midP2, strokeWidth = 4f)
                            drawLine(routeColor, midP2, endP, strokeWidth = 4f)

                            // Origin Circle
                            drawCircle(SushiRed, radius = 6f, center = startP)

                            // Destination Pulse
                            drawCircle(GoldPrimary.copy(alpha = 0.25f), radius = 24f, center = endP)
                            drawCircle(GoldPrimary.copy(alpha = 0.5f), radius = 14f, center = endP)
                            drawCircle(GoldPrimary, radius = 7f, center = endP)
                        }

                        // Origin Badge Label
                        Surface(
                            color = SushiRedContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 12.dp, bottom = 12.dp)
                        ) {
                            Text(
                                text = "🍣 Isaya Gourmet HQ",
                                color = SushiRedLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Destination Badge Label
                        Surface(
                            color = GoldDark,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp, top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PersonPinCircle, contentDescription = null, tint = DeepBlack, modifier = Modifier.size(11.dp))
                                Text(
                                    text = "Destino Cliente",
                                    color = DeepBlack,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Floating GPS Coordinates Overlay
                        Surface(
                            color = DeepBlack.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 10.dp, bottom = 10.dp)
                        ) {
                            Text(
                                text = "🛰️ $coords",
                                color = GoldPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Driver Selector quick row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Repartidor Asignado:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = order.assignedDriver == "Edinson",
                            onClick = { onAssignDriver("Edinson") },
                            label = { Text("🛵 Edinson", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = DeepBlack,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        )
                        FilterChip(
                            selected = order.assignedDriver == "Yohander",
                            onClick = { onAssignDriver("Yohander") },
                            label = { Text("🛵 Yohander", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = DeepBlack,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Primary Action Button: Open external GPS App (Google Maps / Waze)
                Button(
                    onClick = {
                        try {
                            val cleanAddr = customer.deliveryAddress.ifBlank { zoneName }
                            val geoUri = Uri.parse("geo:10.2469,-67.5958?q=10.2469,-67.5958(${Uri.encode(cleanAddr)})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // Fallback to Google Maps URL
                            try {
                                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=10.2469,-67.5958")
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                            } catch (e2: Exception) {
                                // Ignore
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_open_external_gps_${order.id}")
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir en Google Maps / Waze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


