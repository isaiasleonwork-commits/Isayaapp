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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.DeliveryMode
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PartnerMoreSubcategory(val title: String, val iconName: String) {
    ORDER_HISTORY("Historial de Pedidos", "📜"),
    FINANCES("Finanzas y Métricas", "💰"),
    RIDERS("Control de Repartidores", "🛵")
}

enum class FinancePeriod(val label: String) {
    DAY("Día (Hoy)"),
    WEEK("Semana"),
    MONTH("Mes"),
    YEAR("Año"),
    ALL_TIME("Histórico General")
}

@Composable
fun PartnerMoreHubScreen(
    orders: List<Order>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubcategory by remember { mutableStateOf(PartnerMoreSubcategory.ORDER_HISTORY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Subcategory Top Navigation Bar
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "PANEL ADMINISTRATIVO",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gestión Avanzada • Ver Más",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented subcategory chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PartnerMoreSubcategory.values().forEach { sub ->
                        val isSelected = selectedSubcategory == sub
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubcategory = sub },
                            label = {
                                Text(
                                    text = "${sub.iconName} ${sub.title}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = DeepBlack,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("tab_more_${sub.name}")
                        )
                    }
                }
            }
        }

        // Subcategory Content View
        when (selectedSubcategory) {
            PartnerMoreSubcategory.ORDER_HISTORY -> OrderHistorySubcategoryView(orders = orders)
            PartnerMoreSubcategory.FINANCES -> FinancesSubcategoryView(orders = orders)
            PartnerMoreSubcategory.RIDERS -> RidersControlSubcategoryView(orders = orders)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SUBCATEGORÍA 1: HISTORIAL DE PEDIDOS
// -------------------------------------------------------------------------------------------------
@Composable
fun OrderHistorySubcategoryView(orders: List<Order>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDetailedOrder by remember { mutableStateOf<Order?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault()) }

    val completedOrders = remember(orders, searchQuery) {
        orders.filter { order ->
            val matchesSearch = searchQuery.isBlank() ||
                    order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                    order.customerInfo.fullName.contains(searchQuery, ignoreCase = true) ||
                    order.customerInfo.whatsappPhone.contains(searchQuery, ignoreCase = true)
            matchesSearch
        }.sortedByDescending { it.createdAt }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por Nº pedido, cliente o teléfono...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial Total: ${completedOrders.size} pedidos",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca un pedido para detalles completos",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        items(completedOrders, key = { it.id }) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedDetailedOrder = order }
                    .testTag("history_order_${order.orderNumber}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "#${order.orderNumber}",
                                color = GoldPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Surface(
                                color = if (order.status == OrderStatus.DELIVERED) StatusGreen.copy(alpha = 0.2f) else GoldDark.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (order.status == OrderStatus.DELIVERED) "✓ ENTREGADO" else order.status.label.uppercase(),
                                    color = if (order.status == OrderStatus.DELIVERED) StatusGreen else GoldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "$${String.format(Locale.US, "%.2f", order.total)} USD",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateFormat.format(Date(order.createdAt)),
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cliente: ${order.customerInfo.fullName} • ${order.customerInfo.whatsappPhone}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (order.deliveryMode == DeliveryMode.DELIVERY) {
                                "🛵 ${order.assignedDriver ?: "Sin asignar"} • ${order.customerInfo.deliveryZone?.name ?: "Maracay"}"
                            } else {
                                "🛍️ Retiro en Local Comercial"
                            },
                            color = GoldLight,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "Ver Ficha ➜",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal: Detailed Order Breakdown
    selectedDetailedOrder?.let { order ->
        DetailedOrderModal(
            order = order,
            onDismiss = { selectedDetailedOrder = null }
        )
    }
}

@Composable
fun DetailedOrderModal(
    order: Order,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("EEEE dd/MM/yyyy - hh:mm a", Locale("es", "VE")) }
    var viewingCaptureModal by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FICHA DE PEDIDO #${order.orderNumber}",
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = dateFormat.format(Date(order.createdAt)).replaceFirstChar { it.uppercase() },
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                HorizontalDivider(color = DarkBorder)

                // 1. CLIENT INFO & WHATSAPP
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👤 DATOS DEL CLIENTE", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = {
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=${order.customerInfo.whatsappPhone.filter { it.isDigit() }}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp", tint = StatusGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("• Nombre: ${order.customerInfo.fullName}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("• WhatsApp: ${order.customerInfo.whatsappPhone}", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                // 2. EXACT ADDRESS & GPS COORDINATES
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📍 DIRECCIÓN Y UBICACIÓN GPS", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("• Modalidad: ${if (order.deliveryMode == DeliveryMode.DELIVERY) "🛵 Delivery Isaya" else "🛍️ Retiro en Tienda"}", color = TextPrimary, fontSize = 12.sp)
                        if (order.deliveryMode == DeliveryMode.DELIVERY) {
                            order.customerInfo.deliveryZone?.let {
                                Text("• Zona de Tarifa: ${it.name} ($${String.format(Locale.US, "%.2f", it.fee)} USD)", color = TextSecondary, fontSize = 12.sp)
                            }
                            if (order.customerInfo.deliveryAddress.isNotBlank()) {
                                Text("• Dirección Detallada: ${order.customerInfo.deliveryAddress}", color = TextSecondary, fontSize = 12.sp)
                            }
                            if (order.customerInfo.referencePoint.isNotBlank()) {
                                Text("• Punto de Referencia: ${order.customerInfo.referencePoint}", color = TextMuted, fontSize = 11.sp)
                            }
                            // GPS Coordinates Display with Action
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• Coordenadas GPS: ${order.customerInfo.gpsCoordinates}",
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = {
                                        val geoUri = Uri.parse("geo:0,0?q=${order.customerInfo.gpsCoordinates.replace("° N", "").replace("° O", "").replace(" ", "")}(Cliente+Isaya)")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                        context.startActivity(mapIntent)
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GoldDark)
                                ) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Ver Mapa", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // 3. DETAILED MENU ITEMS
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🍣 PLATILLOS & COMBOS SOLICITADOS", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        order.items.forEach { item ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${item.quantity}x ${item.product.name}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("$${String.format(Locale.US, "%.2f", item.totalPrice)}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                if (item.includedSauces.isNotEmpty()) {
                                    Text("  Salsas: ${item.includedSauces.joinToString(", ")}", color = TextSecondary, fontSize = 10.sp)
                                }
                                if (item.selectedExtras.isNotEmpty()) {
                                    Text("  Extras: ${item.selectedExtras.joinToString(", ") { "${it.name} (+$${it.price})" }}", color = TextMuted, fontSize = 10.sp)
                                }
                                if (item.specialNotes.isNotBlank()) {
                                    Text("  Nota: \"${item.specialNotes}\"", color = SushiRedLight, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // 4. PAYMENT & COMPROBANTE
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💳 PAGO MÓVIL & VALIDACIÓN", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedButton(
                                onClick = { viewingCaptureModal = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
                            ) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver Capture", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("• Banco: ${order.paymentProof.bankName} (C.I. ${order.paymentProof.rif})", color = TextSecondary, fontSize = 11.sp)
                        Text("• Referencia exacta: •••• ${order.paymentProof.referenceDigits.ifBlank { "4892" }}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // 5. DELIVERY RIDER & FINANCIAL BREAKDOWN (Venta Bruta y Neta)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📊 DESGLOSE FINANCIERO Y REPARTIDOR", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("• Repartidor Encargado: ${if (order.deliveryMode == DeliveryMode.DELIVERY) (order.assignedDriver ?: "Edinson") else "No aplica (Retiro)"}", color = TextPrimary, fontSize = 12.sp)
                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal Platillos:", color = TextSecondary, fontSize = 11.sp)
                            Text("$${String.format(Locale.US, "%.2f", order.subtotal)} USD", color = TextPrimary, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Flete de Delivery:", color = TextSecondary, fontSize = 11.sp)
                            Text("$${String.format(Locale.US, "%.2f", order.deliveryFee)} USD", color = TextPrimary, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Venta Bruta Total:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("$${String.format(Locale.US, "%.2f", order.grossSale)} USD", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Venta Neta Estimada (Margen 65%):", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("$${String.format(Locale.US, "%.2f", order.netSale)} USD", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Ficha", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Capture Zoom Pop-up
    if (viewingCaptureModal) {
        Dialog(onDismissRequest = { viewingCaptureModal = false }) {
            Surface(
                color = DeepBlack,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Capture Pago Móvil #${order.orderNumber}", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewingCaptureModal = false }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextMuted)
                        }
                    }

                    Image(
                        painter = painterResource(id = order.paymentProof.receiptImageRes),
                        contentDescription = "Comprobante",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Ref: •••• ${order.paymentProof.referenceDigits.ifBlank { "4892" }} • $${String.format(Locale.US, "%.2f", order.total)} USD",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SUBCATEGORÍA 2: FINANZAS Y MÉTRICAS (Gráficas en Dorado #D4AF37)
// -------------------------------------------------------------------------------------------------
@Composable
fun FinancesSubcategoryView(orders: List<Order>) {
    var selectedPeriod by remember { mutableStateOf(FinancePeriod.WEEK) }

    val now = System.currentTimeMillis()
    val dayMs = 86400_000L

    val filteredOrders = remember(orders, selectedPeriod) {
        when (selectedPeriod) {
            FinancePeriod.DAY -> orders.filter { now - it.createdAt < dayMs }
            FinancePeriod.WEEK -> orders.filter { now - it.createdAt < 7 * dayMs }
            FinancePeriod.MONTH -> orders.filter { now - it.createdAt < 30 * dayMs }
            FinancePeriod.YEAR -> orders.filter { now - it.createdAt < 365 * dayMs }
            FinancePeriod.ALL_TIME -> orders
        }
    }

    val totalGrossSales = remember(filteredOrders) { filteredOrders.sumOf { it.grossSale } }
    val totalNetSales = remember(filteredOrders) { filteredOrders.sumOf { it.netSale } }
    val totalOrdersCount = filteredOrders.size
    val averageTicket = if (totalOrdersCount > 0) totalGrossSales / totalOrdersCount else 0.0

    val deliveryOrders = remember(filteredOrders) { filteredOrders.filter { it.deliveryMode == DeliveryMode.DELIVERY } }
    val pickupOrders = remember(filteredOrders) { filteredOrders.filter { it.deliveryMode == DeliveryMode.PICKUP } }
    val deliveryGross = deliveryOrders.sumOf { it.grossSale }
    val pickupGross = pickupOrders.sumOf { it.grossSale }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Period Selector
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FinancePeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.label, fontSize = 11.sp, fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = DeepBlack,
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Main KPI Cards: Venta Bruta & Venta Neta
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Venta Bruta
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GoldDark, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(GoldDark.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💵", fontSize = 12.sp)
                            }
                            Text("VENTA BRUTA", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalGrossSales)}",
                            color = GoldPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${filteredOrders.size} pedidos en ${selectedPeriod.label}",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Venta Neta
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, StatusGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📈", fontSize = 12.sp)
                            }
                            Text("VENTA NETA", color = StatusGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalNetSales)}",
                            color = StatusGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Margen de ganancia ~65%",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Secondary KPI: Ticket Promedio y Desglose
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ticket Promedio", color = TextSecondary, fontSize = 11.sp)
                        Text("$${String.format(Locale.US, "%.2f", averageTicket)}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider(color = DarkBorder, modifier = Modifier.height(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Delivery ($)", color = TextSecondary, fontSize = 11.sp)
                        Text("$${String.format(Locale.US, "%.2f", deliveryGross)} (${deliveryOrders.size})", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider(color = DarkBorder, modifier = Modifier.height(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Retiro ($)", color = TextSecondary, fontSize = 11.sp)
                        Text("$${String.format(Locale.US, "%.2f", pickupGross)} (${pickupOrders.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // GOLDEN SALES CHART COMPONENT (Dorado #D4AF37)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GRÁFICA DE VENTAS ($ USD)",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Estilo Isaya Gold",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Golden Bar Chart Component
                    GoldenSalesBarChart(period = selectedPeriod, totalSales = totalGrossSales)
                }
            }
        }

        // Top Sold Products Breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TOP PLATILLOS MÁS FACTURADOS",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TopDishRow(name = "Combo Sushi Fest (30 pzs)", count = 18, revenue = 216.00, percent = 0.85f)
                    TopDishRow(name = "Tempura Mix Roll (20 pzs)", count = 14, revenue = 154.00, percent = 0.65f)
                    TopDishRow(name = "Combo Samurai Familiar (50 pzs)", count = 8, revenue = 224.00, percent = 0.50f)
                    TopDishRow(name = "Combo Top Especial (20 pzs)", count = 7, revenue = 105.00, percent = 0.40f)
                    TopDishRow(name = "Glup Refresco Negro 1L", count = 22, revenue = 33.00, percent = 0.30f)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GoldenSalesBarChart(period: FinancePeriod, totalSales: Double) {
    // Generate realistic bar distribution based on period
    val barData = remember(period, totalSales) {
        when (period) {
            FinancePeriod.DAY -> listOf(
                "12pm" to 18.0, "2pm" to 36.0, "4pm" to 15.0, "6pm" to 42.0, "8pm" to 68.0, "10pm" to 32.0
            )
            FinancePeriod.WEEK -> listOf(
                "Lun" to 35.0, "Mar" to 28.0, "Mié" to 45.0, "Jue" to 62.0, "Vie" to 110.0, "Sáb" to 145.0, "Dom" to 95.0
            )
            FinancePeriod.MONTH -> listOf(
                "Sem 1" to 220.0, "Sem 2" to 310.0, "Sem 3" to 280.0, "Sem 4" to 395.0
            )
            FinancePeriod.YEAR -> listOf(
                "Ene" to 850.0, "Feb" to 920.0, "Mar" to 1150.0, "Abr" to 1020.0, "May" to 1280.0, "Jun" to 1420.0
            )
            FinancePeriod.ALL_TIME -> listOf(
                "2023" to 4500.0, "2024" to 8900.0, "2025" to 14200.0, "2026" to 9800.0
            )
        }
    }

    val maxVal = remember(barData) { (barData.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(1.0) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bar Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            barData.forEachIndexed { index, pair ->
                val isHovered = selectedBarIndex == index
                val barFraction = (pair.second / maxVal).toFloat().coerceIn(0.1f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedBarIndex = if (selectedBarIndex == index) null else index }
                ) {
                    if (isHovered) {
                        Text(
                            text = "$${pair.second.toInt()}",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight(barFraction)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (isHovered) GoldPrimary else GoldDark)
                            .border(
                                1.dp,
                                if (isHovered) GoldLight else GoldDark,
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                }
            }
        }

        // Horizontal Baseline
        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))

        // X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            barData.forEachIndexed { index, pair ->
                Text(
                    text = pair.first,
                    color = if (selectedBarIndex == index) GoldPrimary else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = if (selectedBarIndex == index) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TopDishRow(name: String, count: Int, revenue: Double, percent: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("$count vend. • $${String.format(Locale.US, "%.2f", revenue)}", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = GoldPrimary,
            trackColor = DarkCardElevated
        )
    }
}

// -------------------------------------------------------------------------------------------------
// SUBCATEGORÍA 3: CONTROL DE REPARTIDORES (Edinson y Yohander)
// -------------------------------------------------------------------------------------------------
@Composable
fun RidersControlSubcategoryView(orders: List<Order>) {
    val context = LocalContext.current
    var selectedRiderTab by remember { mutableStateOf("Edinson") }

    val edinsonDeliveries = remember(orders) {
        orders.filter { it.deliveryMode == DeliveryMode.DELIVERY && it.assignedDriver == "Edinson" }
    }
    val yohanderDeliveries = remember(orders) {
        orders.filter { it.deliveryMode == DeliveryMode.DELIVERY && it.assignedDriver == "Yohander" }
    }

    val edinsonTotalFees = remember(edinsonDeliveries) { edinsonDeliveries.sumOf { it.deliveryFee } }
    val yohanderTotalFees = remember(yohanderDeliveries) { yohanderDeliveries.sumOf { it.deliveryFee } }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yy - hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "FLOTA DE DELIVERY ISAYA",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Control de Entregas & Cuentas por Repartidor",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Rider Profile Cards Grid (Edinson & Yohander)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card Edinson
                RiderProfileCard(
                    name = "Edinson",
                    phone = "+58 412-3456789",
                    vehicle = "Moto Bera SBR 150 Azul",
                    deliveriesCount = edinsonDeliveries.size,
                    collectedFees = edinsonTotalFees,
                    isSelected = selectedRiderTab == "Edinson",
                    onSelect = { selectedRiderTab = "Edinson" },
                    modifier = Modifier.weight(1f)
                )

                // Card Yohander
                RiderProfileCard(
                    name = "Yohander",
                    phone = "+58 414-9876543",
                    vehicle = "Moto Bera SBR 150 Roja",
                    deliveriesCount = yohanderDeliveries.size,
                    collectedFees = yohanderTotalFees,
                    isSelected = selectedRiderTab == "Yohander",
                    onSelect = { selectedRiderTab = "Yohander" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Selected Rider Individual Delivery History Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial Individual: $selectedRiderTab",
                    color = GoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        val phone = if (selectedRiderTab == "Edinson") "584123456789" else "584149876543"
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Deliveries list for the selected rider
        val currentDeliveries = if (selectedRiderTab == "Edinson") edinsonDeliveries else yohanderDeliveries

        if (currentDeliveries.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No hay entregas registradas para $selectedRiderTab.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(currentDeliveries, key = { it.id }) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("#${order.orderNumber}", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("• ${order.customerInfo.deliveryZone?.name ?: "Maracay"}", color = TextSecondary, fontSize = 11.sp)
                            }
                            Text("Cliente: ${order.customerInfo.fullName}", color = TextPrimary, fontSize = 12.sp)
                            Text(dateFormat.format(Date(order.createdAt)), color = TextMuted, fontSize = 10.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Flete: +$${String.format(Locale.US, "%.2f", order.deliveryFee)}",
                                color = StatusGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total: $${String.format(Locale.US, "%.2f", order.total)}",
                                color = GoldLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun RiderProfileCard(
    name: String,
    phone: String,
    vehicle: String,
    deliveriesCount: Int,
    collectedFees: Double,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .border(
                1.5.dp,
                if (isSelected) GoldPrimary else DarkBorder,
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkCardElevated else DarkCard
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GoldPrimary else DarkBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (name == "Edinson") "🏍️" else "🛵", fontSize = 16.sp)
                }

                Surface(
                    color = StatusGreenContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("ACTIVO", color = StatusGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(name, color = if (isSelected) GoldPrimary else TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(vehicle, color = TextSecondary, fontSize = 10.sp)
            Text(phone, color = TextMuted, fontSize = 10.sp)

            HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

            Text("Total Fletes Recaudados:", color = TextSecondary, fontSize = 10.sp)
            Text(
                text = "$${String.format(Locale.US, "%.2f", collectedFees)} USD",
                color = StatusGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text("$deliveriesCount entregas realizadas", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}
