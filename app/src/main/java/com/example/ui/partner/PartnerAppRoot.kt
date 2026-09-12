package com.example.ui.partner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.IsayaRepository
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.SoundAlertHelper
import java.util.UUID

enum class PartnerSectionTab {
    KITCHEN_ORDERS,
    MENU_CMS,
    MORE_HUB,
    PUSH_BROADCAST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerAppRoot(
    repository: IsayaRepository,
    onSwitchToClient: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authenticatedPartner by repository.authenticatedPartner.collectAsStateWithLifecycle()
    val settings by repository.settings.collectAsStateWithLifecycle()
    val menuItems by repository.menu.collectAsStateWithLifecycle()
    val orders by repository.orders.collectAsStateWithLifecycle()
    val notifications by repository.notifications.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(PartnerSectionTab.KITCHEN_ORDERS) }
    val pendingCount = remember(orders) { orders.count { it.status == OrderStatus.PENDING_PAYMENT } }
    var isBottomBarVisible by remember { mutableStateOf(true) }

    // Auto-hide bottom navigation on scroll down, show on scroll up
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -8f && isBottomBarVisible) {
                    isBottomBarVisible = false
                } else if (available.y > 8f && !isBottomBarVisible) {
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // If not authenticated, require Firebase Authentication Login
    if (authenticatedPartner == null) {
        PartnerAuthLoginScreen(
            onLoginSubmit = { email, password, onResult ->
                repository.loginPartner(email, password, onResult)
            },
            onSwitchToClient = onSwitchToClient
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        containerColor = DeepBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(0.5.dp, DarkBorder)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SushiRed.copy(alpha = 0.2f))
                                .border(1.dp, SushiRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = SushiRedLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ISAYA PARTNER",
                                    color = GoldPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Surface(
                                    color = if (settings.isOpen) StatusGreen.copy(alpha = 0.2f) else SushiRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (settings.isOpen) "ABIERTO" else "CERRADO",
                                        color = if (settings.isOpen) StatusGreen else SushiRedLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${authenticatedPartner?.displayName ?: "Partner"} • ${authenticatedPartner?.email ?: "randerleon@gmail.com"}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Switch to Client App
                        OutlinedButton(
                            onClick = onSwitchToClient,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "🍣 Clientes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Sound alert test button
                        IconButton(
                            onClick = {
                                SoundAlertHelper.playNewOrderAlert(context)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Probar alerta sonora",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Logout button
                        IconButton(
                            onClick = {
                                repository.logoutPartner()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkCardElevated)
                                .testTag("partner_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    // Tab 1: Kitchen Orders
                    NavigationBarItem(
                        selected = activeTab == PartnerSectionTab.KITCHEN_ORDERS,
                        onClick = { activeTab = PartnerSectionTab.KITCHEN_ORDERS },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingCount > 0) {
                                        Badge(containerColor = SushiRed) {
                                            Text("$pendingCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Cocina",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = { Text("Pedidos", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("partner_tab_kitchen")
                    )

                    // Tab 2: Menu Stock & CMS Manager
                    NavigationBarItem(
                        selected = activeTab == PartnerSectionTab.MENU_CMS,
                        onClick = { activeTab = PartnerSectionTab.MENU_CMS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = "Gestión Menú",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Menú CMS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("partner_tab_menu")
                    )

                    // Tab 3: Ver Más (Historial, Finanzas, Repartidores)
                    NavigationBarItem(
                        selected = activeTab == PartnerSectionTab.MORE_HUB,
                        onClick = { activeTab = PartnerSectionTab.MORE_HUB },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "Ver Más",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Ver Más", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("partner_tab_more")
                    )

                    // Tab 4: Mass Push Notification Broadcast
                    NavigationBarItem(
                        selected = activeTab == PartnerSectionTab.PUSH_BROADCAST,
                        onClick = { activeTab = PartnerSectionTab.PUSH_BROADCAST },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Push Masivo",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Push", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("partner_tab_push")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                PartnerSectionTab.KITCHEN_ORDERS -> {
                    PartnerOrdersScreen(
                        orders = orders,
                        onUpdateStatus = { orderId, newStatus ->
                            repository.updateOrderStatus(orderId, newStatus)
                        },
                        onAssignDriver = { orderId, driverName ->
                            repository.assignDriverToOrder(orderId, driverName)
                        },
                        onSimulateOrder = {
                            val randomRoll = menuItems.shuffled().first()
                            val testItem = CartItem(
                                id = UUID.randomUUID().toString(),
                                product = randomRoll,
                                quantity = 2,
                                includedSauces = listOf("Soya", "Spicy Mayo"),
                                selectedExtras = listOf(ExtraAddon("wasabi", "Wasabi", 0.50)),
                                specialNotes = "Salsas bien empacadas por favor",
                                unitPrice = randomRoll.price
                            )
                            val fakeCustomer = CustomerInfo(
                                fullName = listOf("Valeria Castillo", "Sebastián Moros", "Daniela Pardo", "Rodrigo Silva").random(),
                                whatsappPhone = "+58 424-3" + (100000..999999).random(),
                                deliveryAddress = "Calle La Guairita, Residencias El Bosque, Apto 5-A",
                                referencePoint = "Cerca del centro comercial"
                            )
                            val fakePayment = PaymentProof(
                                referenceDigits = (1000..9999).random().toString(),
                                receiptAttached = true,
                                receiptFileName = "pago_movil_cliente.jpg"
                            )
                            repository.placeNewOrder(
                                items = listOf(testItem),
                                deliveryMode = DeliveryMode.DELIVERY,
                                customerInfo = fakeCustomer,
                                paymentProof = fakePayment
                            )
                        }
                    )
                }

                PartnerSectionTab.MENU_CMS -> {
                    PartnerMenuManagerScreen(
                        settings = settings,
                        menuItems = menuItems,
                        onToggleMasterStoreOpen = {
                            repository.toggleRestaurantOpenStatus()
                        },
                        onToggleItemAvailability = { productId ->
                            repository.toggleProductAvailability(productId)
                        },
                        onAddProduct = { newProduct ->
                            repository.addMenuItem(newProduct)
                        },
                        onUpdateProduct = { updatedProduct ->
                            repository.updateMenuItem(updatedProduct)
                        },
                        onDeleteProduct = { productId ->
                            repository.deleteMenuItem(productId)
                        }
                    )
                }

                PartnerSectionTab.MORE_HUB -> {
                    PartnerMoreHubScreen(
                        orders = orders,
                        onUpdateOrderStatus = { orderId, newStatus ->
                            repository.updateOrderStatus(orderId, newStatus)
                        }
                    )
                }

                PartnerSectionTab.PUSH_BROADCAST -> {
                    PartnerBroadcastNotificationScreen(
                        notifications = notifications,
                        onBroadcast = { title, body, tag ->
                            repository.broadcastPushNotification(title, body, tag)
                        }
                    )
                }
            }
        }
    }
}
