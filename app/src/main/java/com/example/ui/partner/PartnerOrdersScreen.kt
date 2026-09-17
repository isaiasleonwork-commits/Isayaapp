package com.example.ui.partner

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
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
    var viewingFullscreenReceiptOrder by remember { mutableStateOf<Order?>(null) }
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
        // Vibrant Monitor Bar with Simulator Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = DarkCardElevated,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SushiRed.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SushiRed.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "En vivo",
                            tint = SushiRedLight,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "COMANDAS EN VIVO",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = SushiRed,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${activeOrders.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }

                // Quick Simulate Button
                Button(
                    onClick = onSimulateOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("simulate_order_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(13.dp))
                        Text("Probar Alerta", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Compact Vibrant Status Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("Todos (${activeOrders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = DeepBlack,
                    containerColor = DarkCard,
                    labelColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == null,
                    borderColor = DarkBorder,
                    selectedBorderColor = GoldPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(30.dp)
            )

            FilterChip(
                selected = selectedFilter == OrderStatus.PENDING_PAYMENT,
                onClick = {
                    selectedFilter = if (selectedFilter == OrderStatus.PENDING_PAYMENT) null else OrderStatus.PENDING_PAYMENT
                },
                label = { Text("⏳ Pago ($pendingCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusOrange,
                    selectedLabelColor = DeepBlack,
                    containerColor = DarkCard,
                    labelColor = StatusOrange
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == OrderStatus.PENDING_PAYMENT,
                    borderColor = StatusOrange.copy(alpha = 0.4f),
                    selectedBorderColor = StatusOrange
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(30.dp)
            )

            FilterChip(
                selected = selectedFilter == OrderStatus.IN_KITCHEN,
                onClick = {
                    selectedFilter = if (selectedFilter == OrderStatus.IN_KITCHEN) null else OrderStatus.IN_KITCHEN
                },
                label = { Text("🍳 Cocina ($kitchenCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SushiRed,
                    selectedLabelColor = Color.White,
                    containerColor = DarkCard,
                    labelColor = SushiRedLight
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == OrderStatus.IN_KITCHEN,
                    borderColor = SushiRed.copy(alpha = 0.4f),
                    selectedBorderColor = SushiRed
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(30.dp)
            )

            FilterChip(
                selected = selectedFilter == OrderStatus.DISPATCHED_OR_READY,
                onClick = {
                    selectedFilter = if (selectedFilter == OrderStatus.DISPATCHED_OR_READY) null else OrderStatus.DISPATCHED_OR_READY
                },
                label = { Text("🛵 En Ruta ($dispatchedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusGreen,
                    selectedLabelColor = DeepBlack,
                    containerColor = DarkCard,
                    labelColor = StatusGreen
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == OrderStatus.DISPATCHED_OR_READY,
                    borderColor = StatusGreen.copy(alpha = 0.4f),
                    selectedBorderColor = StatusGreen
                ),
                shape = RoundedCornerShape(10.dp),
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
                    Box(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(durationMillis = 300),
                            fadeOutSpec = tween(durationMillis = 300),
                            placementSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                dampingRatio = Spring.DampingRatioLowBouncy
                            )
                        )
                    ) {
                        CompactPartnerOrderCard(
                            order = order,
                            onUpdateStatus = { newStatus -> onUpdateStatus(order.id, newStatus) },
                            onAssignDriver = { driverName -> onAssignDriver(order.id, driverName) },
                            onViewReceipt = { viewingReceiptOrder = order },
                            onViewMap = { viewingMapOrder = order }
                        )
                    }
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

                    // REAL CAPTURE IMAGE DISPLAY & INTERACTIVE VIEWER
                    InteractiveReceiptViewer(
                        order = order,
                        onExpandFullscreen = {
                            viewingFullscreenReceiptOrder = order
                        }
                    )

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

    viewingFullscreenReceiptOrder?.let { order ->
        FullscreenReceiptLightboxDialog(
            order = order,
            onDismiss = { viewingFullscreenReceiptOrder = null }
        )
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

    // Fluid Border Color Animation based on order state
    val targetBorderColor = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> StatusOrange.copy(alpha = 0.5f)
        OrderStatus.PAYMENT_CONFIRMED -> StatusBlue.copy(alpha = 0.6f)
        OrderStatus.IN_KITCHEN -> SushiRed.copy(alpha = 0.85f)
        OrderStatus.DISPATCHED_OR_READY -> StatusGreen.copy(alpha = 0.7f)
        OrderStatus.DELIVERED -> StatusGreen.copy(alpha = 0.4f)
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardBorderColor"
    )

    // Fluid Background Container Color Animation
    val targetContainerColor = when (order.status) {
        OrderStatus.IN_KITCHEN -> DarkCardElevated
        OrderStatus.PENDING_PAYMENT -> DarkCard
        else -> DarkCard
    }
    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(durationMillis = 350),
        label = "cardContainerColor"
    )

    // Fluid Progress Stepper across top of card
    val targetProgress = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> 0.25f
        OrderStatus.PAYMENT_CONFIRMED -> 0.50f
        OrderStatus.IN_KITCHEN -> 0.75f
        OrderStatus.DISPATCHED_OR_READY -> 1.0f
        OrderStatus.DELIVERED -> 1.0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "cardProgress"
    )
    val animatedProgressColor by animateColorAsState(
        targetValue = when (order.status) {
            OrderStatus.PENDING_PAYMENT -> StatusOrange
            OrderStatus.PAYMENT_CONFIRMED -> StatusBlue
            OrderStatus.IN_KITCHEN -> SushiRed
            OrderStatus.DISPATCHED_OR_READY -> StatusGreen
            OrderStatus.DELIVERED -> StatusGreen
        },
        animationSpec = tween(350),
        label = "progressColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, animatedBorderColor, RoundedCornerShape(16.dp))
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            )
            .testTag("partner_order_${order.orderNumber}"),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated Status Progress Bar (Layout Animation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(DarkBorder.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(animatedProgressColor)
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ROW 1 (Header Flex): #Order + Time + Mode Pill + Spacer + Total + Animated Status Badge
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

                    // Compact Status Pill with AnimatedContent
                    AnimatedContent(
                        targetState = order.status,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn(tween(250)))
                                .togetherWith(slideOutVertically { height -> -height } + fadeOut(tween(200)))
                        },
                        label = "statusBadgeAnimation"
                    ) { targetStatus ->
                        val badgeBg = when (targetStatus) {
                            OrderStatus.PENDING_PAYMENT -> StatusOrange.copy(alpha = 0.2f)
                            OrderStatus.PAYMENT_CONFIRMED -> StatusBlue.copy(alpha = 0.2f)
                            OrderStatus.IN_KITCHEN -> SushiRed.copy(alpha = 0.25f)
                            OrderStatus.DISPATCHED_OR_READY -> StatusGreen.copy(alpha = 0.2f)
                            OrderStatus.DELIVERED -> StatusGreen.copy(alpha = 0.3f)
                        }
                        val badgeBorder = when (targetStatus) {
                            OrderStatus.PENDING_PAYMENT -> StatusOrange.copy(alpha = 0.5f)
                            OrderStatus.PAYMENT_CONFIRMED -> StatusBlue.copy(alpha = 0.5f)
                            OrderStatus.IN_KITCHEN -> SushiRed.copy(alpha = 0.8f)
                            OrderStatus.DISPATCHED_OR_READY -> StatusGreen.copy(alpha = 0.5f)
                            OrderStatus.DELIVERED -> StatusGreen.copy(alpha = 0.5f)
                        }
                        val badgeTextColor = when (targetStatus) {
                            OrderStatus.PENDING_PAYMENT -> StatusOrange
                            OrderStatus.PAYMENT_CONFIRMED -> StatusBlue
                            OrderStatus.IN_KITCHEN -> SushiRedLight
                            OrderStatus.DISPATCHED_OR_READY -> StatusGreen
                            OrderStatus.DELIVERED -> StatusGreen
                        }

                        Surface(
                            color = badgeBg,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, badgeBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                if (targetStatus == OrderStatus.IN_KITCHEN) {
                                    Icon(
                                        imageVector = Icons.Default.OutdoorGrill,
                                        contentDescription = null,
                                        tint = SushiRedLight,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                                Text(
                                    text = targetStatus.label,
                                    color = badgeTextColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                        val hasProof = order.paymentProof.receiptAttached && (!order.paymentProof.receiptImageUrl.isNullOrBlank() || order.paymentProof.receiptImageRes != 0)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkCard),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!order.paymentProof.receiptImageUrl.isNullOrBlank()) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(order.paymentProof.receiptImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Comprobante",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(14.dp), color = GoldPrimary)
                                        }
                                    },
                                    error = {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                )
                            } else if (order.paymentProof.receiptAttached && order.paymentProof.receiptImageRes != 0) {
                                Image(
                                    painter = painterResource(id = order.paymentProof.receiptImageRes),
                                    contentDescription = "Comprobante",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Sin comprobante",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (hasProof) "Ref:" else "Sin foto",
                                color = TextSecondary,
                                fontSize = 8.sp
                            )
                            Text(
                                text = "••••${order.paymentProof.referenceDigits.takeLast(4).ifBlank { "4892" }}",
                                color = if (hasProof) GoldPrimary else TextMuted,
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

                // One-Touch Action Button with AnimatedContent transition
                AnimatedContent(
                    targetState = order.status,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.88f) + fadeIn(tween(250)))
                            .togetherWith(scaleOut(targetScale = 0.88f) + fadeOut(tween(200)))
                    },
                    label = "actionButtonAnimation"
                ) { targetStatus ->
                    when (targetStatus) {
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

/**
 * INTERACTIVE REAL RECEIPT VIEWER
 * Loads the authentic payment capture URL from Firebase / local device.
 * Features pinch-to-zoom, pan, zoom control buttons, and full lightbox expansion.
 * Renders a clean "Sin comprobante adjunto" notice if no photo was uploaded.
 */
@Composable
fun InteractiveReceiptViewer(
    order: Order,
    onExpandFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val proof = order.paymentProof
    val hasImage = proof.receiptAttached && (!proof.receiptImageUrl.isNullOrBlank() || proof.receiptImageRes != 0)

    if (!hasImage) {
        // Flat placeholder when no image is uploaded
        Surface(
            color = DarkCard,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Sin comprobante adjunto",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "El cliente no adjuntó una foto de captura para esta orden.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Capture Real del Cliente:",
                color = GoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Fullscreen lightbox button
            Surface(
                color = DarkCardElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, GoldDark),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onExpandFullscreen() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Ampliar",
                        tint = GoldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Pantalla Completa",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Zoomable Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepBlack)
                .border(1.dp, GoldDark.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .transformable(state = transformState),
            contentAlignment = Alignment.Center
        ) {
            if (!proof.receiptImageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(proof.receiptImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Capture de Pago Móvil",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Text("Cargando comprobante...", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    },
                    error = {
                        if (proof.receiptImageRes != 0) {
                            Image(
                                painter = painterResource(id = proof.receiptImageRes),
                                contentDescription = "Capture de Pago Móvil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.BrokenImage, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                Text("No se pudo cargar la imagen", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                )
            } else if (proof.receiptImageRes != 0) {
                Image(
                    painter = painterResource(id = proof.receiptImageRes),
                    contentDescription = "Capture de Pago Móvil",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Quick Zoom Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pellizca para zoom (${String.format(Locale.US, "%.1fx", scale)})",
                color = TextMuted,
                fontSize = 10.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Zoom Out
                IconButton(
                    onClick = {
                        scale = (scale - 0.5f).coerceAtLeast(1f)
                        if (scale == 1f) offset = Offset.Zero
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Alejar", tint = TextSecondary, modifier = Modifier.size(13.dp))
                }

                // Reset
                if (scale > 1f || offset != Offset.Zero) {
                    TextButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("1.0x", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Zoom In
                IconButton(
                    onClick = {
                        scale = (scale + 0.5f).coerceAtMost(4f)
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Acercar", tint = TextSecondary, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

/**
 * FULLSCREEN LIGHTBOX MODAL
 * Dedicated inspection modal allowing high-zoom validation of reference number, issuing bank, date & amount.
 */
@Composable
fun FullscreenReceiptLightboxDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val proof = order.paymentProof
    var scale by remember { mutableFloatStateOf(1.2f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Comprobante #${order.orderNumber}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ref: •••• ${proof.referenceDigits.ifBlank { "4892" }} • $${String.format(Locale.US, "%.2f", order.total)} USD",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Interactive Fullscreen Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 80.dp)
                    .transformable(state = transformState),
                contentAlignment = Alignment.Center
            ) {
                if (!proof.receiptImageUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(proof.receiptImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Capture Completo",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(36.dp))
                            }
                        },
                        error = {
                            if (proof.receiptImageRes != 0) {
                                Image(
                                    painter = painterResource(id = proof.receiptImageRes),
                                    contentDescription = "Capture Completo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x,
                                            translationY = offset.y
                                        ),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    )
                } else if (proof.receiptImageRes != 0) {
                    Image(
                        painter = painterResource(id = proof.receiptImageRes),
                        contentDescription = "Capture Completo",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Bottom Quick Controls Bar
            Surface(
                color = DarkSurface.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zoom: ${String.format(Locale.US, "%.1fx", scale)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                scale = (scale - 0.5f).coerceAtLeast(1f)
                                if (scale == 1f) offset = Offset.Zero
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkCard)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Alejar", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        Button(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated, contentColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Restablecer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                scale = (scale + 0.5f).coerceAtMost(5f)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkCard)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Acercar", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}


