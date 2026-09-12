package com.example.ui.client

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.IsayaRepository
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ClientMainView {
    MENU,
    ORDER_TRACKER
}

data class FlyingItemParticle(
    val imageRes: Int,
    val id: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientAppRoot(
    repository: IsayaRepository,
    onSwitchToPartner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Repository Flows
    val settings by repository.settings.collectAsStateWithLifecycle()
    val brandIdentity by repository.brandIdentity.collectAsStateWithLifecycle()
    val menuItems by repository.menu.collectAsStateWithLifecycle()
    val orders by repository.orders.collectAsStateWithLifecycle()
    val activeOrderId by repository.activeClientOrderId.collectAsStateWithLifecycle()
    val notifications by repository.notifications.collectAsStateWithLifecycle()

    // Local State
    val context = androidx.compose.ui.platform.LocalContext.current
    var isDarkMode by remember { mutableStateOf(com.example.util.UserLocationPreferences.isDarkMode(context)) }
    var savedLocation by remember { mutableStateOf(com.example.util.UserLocationPreferences.getUserLocation(context)) }
    var isLocationPickerOpen by remember { mutableStateOf(!savedLocation.isConfirmed) }
    var isLocationPickerMandatory by remember { mutableStateOf(false) }
    var currentView by remember { mutableStateOf(ClientMainView.MENU) }
    var selectedDeliveryMode by remember { mutableStateOf(DeliveryMode.DELIVERY) }
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var productToCustomize by remember { mutableStateOf<SushiProduct?>(null) }
    var isCheckoutOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isNotificationsSheetOpen by remember { mutableStateOf(false) }
    var activePushBanner by remember { mutableStateOf<PushNotificationMessage?>(null) }

    // Scroll-based auto-hide state for Header cards & Bottom Navigation
    var isHeaderCardsExpanded by remember { mutableStateOf(true) }
    var isBottomNavVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -8f) {
                    // Scrolling DOWN: collapse header cards and slide out bottom navigation
                    if (isBottomNavVisible) isBottomNavVisible = false
                    if (isHeaderCardsExpanded) isHeaderCardsExpanded = false
                } else if (delta > 8f) {
                    // Scrolling UP: expand header cards and slide in bottom navigation
                    if (!isBottomNavVisible) isBottomNavVisible = true
                    if (!isHeaderCardsExpanded) isHeaderCardsExpanded = true
                }
                return Offset.Zero
            }
        }
    }

    // Absorption Flying Animation State (Curved trajectory into top Cart icon)
    var activeFlyingItem by remember { mutableStateOf<FlyingItemParticle?>(null) }
    val flyingAnim = remember { Animatable(0f) }
    val cartBadgeScale = remember { Animatable(1f) }

    fun triggerAddToCartWithAnimation(cartItem: CartItem) {
        cartItems.add(cartItem)
        scope.launch {
            activeFlyingItem = FlyingItemParticle(imageRes = cartItem.product.imageRes, id = System.currentTimeMillis())
            flyingAnim.snapTo(0f)
            flyingAnim.animateTo(1f, animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing))
            activeFlyingItem = null

            // Bounce scale cart badge upon arrival
            cartBadgeScale.animateTo(1.4f, animationSpec = tween(120))
            cartBadgeScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    // Push notification auto-permission prompt on launch (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Dynamic Theme Color Tokens
    val clientBg = if (isDarkMode) DeepBlack else IosLightBg
    val clientSurface = if (isDarkMode) DarkSurface else IosLightSurface
    val clientCardSecondary = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val clientBorder = if (isDarkMode) DarkBorder else IosLightBorder
    val clientBorderSubtle = if (isDarkMode) DarkBorder.copy(alpha = 0.6f) else IosLightBorderSubtle
    val clientTextPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val clientTextSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val clientTextMuted = if (isDarkMode) TextMuted else IosTextMuted
    val clientGold = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val clientGoldContainer = if (isDarkMode) GoldContainer else IosGoldContainer
    val clientGoldBorder = if (isDarkMode) GoldDark else IosGoldBorder
    val clientRed = if (isDarkMode) SushiRed else IosRedPrimary

    // Listen for incoming live push notifications from the partner kitchen
    LaunchedEffect(repository) {
        repository.newPushNotificationEvent.collectLatest { pushMsg ->
            activePushBanner = pushMsg
            snackbarHostState.showSnackbar(
                message = "🔔 ${pushMsg.title}: ${pushMsg.body}",
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = clientBg,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(clientSurface)
                        .border(0.5.dp, clientBorder)
                ) {
                    // Top App Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ISAYA SUSHI",
                                color = clientGold,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Surface(
                                color = if (settings.isOpen) (if (isDarkMode) StatusGreenContainer else IosGreenContainer) else (if (isDarkMode) SushiRedContainer else IosRedContainer),
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (settings.isOpen) (if (isDarkMode) StatusGreen else IosGreenBorder) else (if (isDarkMode) SushiRed else IosRedBorder)
                                )
                            ) {
                                Text(
                                    text = if (settings.isOpen) "Abierto" else "Cerrado",
                                    color = if (settings.isOpen) (if (isDarkMode) StatusGreen else IosGreenPrimary) else (if (isDarkMode) SushiRedLight else IosRedPrimary),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Settings Gear Icon (Ajustes ⚙️)
                            IconButton(
                                onClick = { isSettingsOpen = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(clientCardSecondary)
                                    .border(1.dp, clientBorder, RoundedCornerShape(20.dp))
                                    .testTag("client_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ajustes",
                                    tint = clientGold,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Cart Icon with dynamic red badge (Movido a la barra superior derecha)
                            val totalCartCount = cartItems.sumOf { it.quantity }
                            IconButton(
                                onClick = { isCheckoutOpen = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (totalCartCount > 0) clientGoldContainer else clientCardSecondary)
                                    .border(1.dp, if (totalCartCount > 0) clientGoldBorder else clientBorder, RoundedCornerShape(20.dp))
                                    .scale(cartBadgeScale.value)
                                    .testTag("client_cart_header_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (totalCartCount > 0) {
                                            Badge(
                                                containerColor = clientRed,
                                                contentColor = Color.White,
                                                modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                                            ) {
                                                Text(
                                                    text = "$totalCartCount",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Carrito",
                                        tint = if (totalCartCount > 0) clientGold else clientTextPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            // Push Notifications Bell 🔔
                            IconButton(
                                onClick = { isNotificationsSheetOpen = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(clientCardSecondary)
                                    .border(1.dp, clientBorder, RoundedCornerShape(20.dp))
                                    .testTag("client_notifications_bell")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (notifications.isNotEmpty()) {
                                            Badge(
                                                containerColor = clientRed,
                                                modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                                            ) {
                                                Text(
                                                    text = "${notifications.size}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notificaciones Push",
                                        tint = clientTextPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Incoming Push Banner (if active)
                    AnimatedVisibility(
                        visible = activePushBanner != null,
                        enter = slideInVertically(),
                        exit = slideOutVertically()
                    ) {
                        activePushBanner?.let { push ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clickable { isNotificationsSheetOpen = true },
                                colors = CardDefaults.cardColors(containerColor = clientGoldContainer),
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, clientGoldBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            color = clientGold,
                                            shape = RoundedCornerShape(24.dp),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Campaign,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = push.title,
                                                color = clientTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = push.body,
                                                color = clientTextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { activePushBanner = null },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cerrar",
                                            tint = clientTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Auto-Hide Navigation Bar with fluid slide down animation
                AnimatedVisibility(
                    visible = isBottomNavVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NavigationBar(
                        containerColor = clientSurface,
                        contentColor = clientTextPrimary,
                        tonalElevation = 6.dp,
                        modifier = Modifier.border(0.5.dp, clientBorder)
                    ) {
                        NavigationBarItem(
                            selected = currentView == ClientMainView.MENU,
                            onClick = { currentView = ClientMainView.MENU },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = "Menú"
                                )
                            },
                            label = { Text("Menú Nikkei", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = clientGold,
                                indicatorColor = clientGold,
                                unselectedIconColor = clientTextSecondary,
                                unselectedTextColor = clientTextSecondary
                            ),
                            modifier = Modifier.testTag("client_tab_menu")
                        )

                        val hasActiveOrder = activeOrderId != null
                        NavigationBarItem(
                            selected = currentView == ClientMainView.ORDER_TRACKER,
                            onClick = { currentView = ClientMainView.ORDER_TRACKER },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (hasActiveOrder) {
                                            Badge(
                                                containerColor = clientRed
                                            ) { Text("1", color = Color.White) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = "Seguimiento"
                                    )
                                }
                            },
                            label = { Text("Mi Pedido (Estatus)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = clientGold,
                                indicatorColor = clientGold,
                                unselectedIconColor = clientTextSecondary,
                                unselectedTextColor = clientTextSecondary
                            ),
                            modifier = Modifier.testTag("client_tab_tracker")
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = clientSurface,
                        contentColor = clientTextPrimary,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .padding(12.dp)
                            .border(1.dp, clientBorder, RoundedCornerShape(24.dp))
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentView) {
                    ClientMainView.MENU -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                        ) {
                            ClientHeader(
                                selectedMode = selectedDeliveryMode,
                                onModeChange = { selectedDeliveryMode = it },
                                isOpen = settings.isOpen,
                                savedLocation = savedLocation,
                                onOpenLocationPicker = { isLocationPickerOpen = true },
                                brandIdentity = brandIdentity,
                                isCardsExpanded = isHeaderCardsExpanded,
                                isDarkMode = isDarkMode
                            )

                            MenuSection(
                                menuItems = menuItems,
                                onProductClick = { product ->
                                    productToCustomize = product
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }

                    ClientMainView.ORDER_TRACKER -> {
                        val activeOrder = orders.firstOrNull { it.id == activeOrderId } ?: orders.firstOrNull()
                        if (activeOrder != null) {
                            OrderStatusTracker(
                                order = activeOrder,
                                onBackToMenu = { currentView = ClientMainView.MENU },
                                isDarkMode = isDarkMode
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = clientSurface),
                                    shape = RoundedCornerShape(24.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, clientBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(28.dp)
                                    ) {
                                        Surface(
                                            color = clientCardSecondary,
                                            shape = RoundedCornerShape(24.dp),
                                            modifier = Modifier.size(64.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.ShoppingBag,
                                                    contentDescription = null,
                                                    tint = clientTextMuted,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No tienes pedidos activos",
                                            color = clientTextPrimary,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Explora el menú y arma tu pedido con Pago Móvil Mercantil.",
                                            color = clientTextSecondary,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                                        )
                                        Button(
                                            onClick = { currentView = ClientMainView.MENU },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = clientGold,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(24.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            Text("Ver Menú Nikkei", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Absorption Miniature Particle Overlay (travels in curved trajectory towards the top-right Cart icon)
        activeFlyingItem?.let { flying ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val progress = flyingAnim.value
                val startX = maxWidth.value * 0.5f
                val startY = maxHeight.value * 0.65f
                val targetX = maxWidth.value - 60f
                val targetY = 22f

                // Curved bezier control point arching upwards
                val controlX = maxWidth.value * 0.20f
                val controlY = maxHeight.value * 0.15f

                val oneMinusT = 1f - progress
                val curX = (oneMinusT * oneMinusT * startX) + (2 * oneMinusT * progress * controlX) + (progress * progress * targetX)
                val curY = (oneMinusT * oneMinusT * startY) + (2 * oneMinusT * progress * controlY) + (progress * progress * targetY)

                val currentScale = (1.2f - (progress * 0.85f)).coerceAtLeast(0.35f)
                val currentAlpha = if (progress > 0.85f) ((1f - progress) / 0.15f).coerceIn(0f, 1f) else 1f

                Surface(
                    color = clientGold,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .offset(x = curX.dp, y = curY.dp)
                        .size(54.dp)
                        .scale(currentScale)
                        .alpha(currentAlpha)
                ) {
                    Image(
                        painter = painterResource(id = flying.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    // Modal 1: Product Customization
    productToCustomize?.let { product ->
        ProductCustomizationModal(
            product = product,
            onDismiss = { productToCustomize = null },
            onAddToCart = { cartItem ->
                triggerAddToCartWithAnimation(cartItem)
                productToCustomize = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "🍣 ${cartItem.quantity}x ${cartItem.product.name} añadido al carrito",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            isDarkMode = isDarkMode
        )
    }

    // Modal 2: Checkout & Pago Móvil
    if (isCheckoutOpen) {
        CheckoutBottomSheet(
            cartItems = cartItems,
            deliveryMode = selectedDeliveryMode,
            onUpdateQuantity = { item, newQty ->
                val index = cartItems.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    if (newQty <= 0) {
                        cartItems.removeAt(index)
                    } else {
                        cartItems[index] = cartItems[index].copy(quantity = newQty)
                    }
                }
            },
            onClearCart = {
                cartItems.clear()
                isCheckoutOpen = false
            },
            onDismiss = { isCheckoutOpen = false },
            onConfirmOrder = { customer, payment ->
                val placedOrder = repository.placeNewOrder(
                    items = cartItems.toList(),
                    deliveryMode = selectedDeliveryMode,
                    customerInfo = customer,
                    paymentProof = payment
                )
                cartItems.clear()
                isCheckoutOpen = false
                currentView = ClientMainView.ORDER_TRACKER
            },
            isDarkMode = isDarkMode
        )
    }

    // Modal 3: Received Push Notifications List Sheet
    if (isNotificationsSheetOpen) {
        Dialog(onDismissRequest = { isNotificationsSheetOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = clientSurface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, clientBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = clientGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Notificaciones Push",
                                color = clientTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { isNotificationsSheetOpen = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = clientTextSecondary
                            )
                        }
                    }

                    if (notifications.isEmpty()) {
                        Text(
                            text = "Aún no has recibido notificaciones push del restaurante.",
                            color = clientTextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notifications, key = { it.id }) { notif ->
                                val formattedDate = remember(notif.timestamp) {
                                    SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = clientCardSecondary),
                                    shape = RoundedCornerShape(24.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, clientBorderSubtle)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notif.title,
                                                color = clientGold,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            notif.promoTag?.let { tag ->
                                                Surface(
                                                    color = if (isDarkMode) SushiRedContainer else IosRedContainer,
                                                    shape = RoundedCornerShape(24.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isDarkMode) SushiRed else IosRedBorder
                                                    )
                                                ) {
                                                    Text(
                                                        text = tag,
                                                        color = if (isDarkMode) SushiRedLight else IosRedPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = notif.body,
                                            color = clientTextSecondary,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                        Text(
                                            text = formattedDate,
                                            color = clientTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { isNotificationsSheetOpen = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = clientGold,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal 4: Interactive GPS Location Map Selector (Ventana Flotante / Modal iOS)
    if (isLocationPickerOpen) {
        InteractiveLocationPickerModal(
            onDismiss = {
                if (!isLocationPickerMandatory) {
                    isLocationPickerOpen = false
                }
            },
            onLocationConfirmed = { newLocation ->
                savedLocation = newLocation
                isLocationPickerOpen = false
                isLocationPickerMandatory = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "📍 Ubicación guardada: ${newLocation.address}",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            canDismiss = !isLocationPickerMandatory,
            isDarkMode = isDarkMode
        )
    }

    // Modal 5: Settings Screen / Modal (Tema, Datos de Entrega, Legal)
    if (isSettingsOpen) {
        SettingsModal(
            isDarkMode = isDarkMode,
            onToggleDarkMode = { newDarkMode ->
                isDarkMode = newDarkMode
                com.example.util.UserLocationPreferences.setDarkMode(context, newDarkMode)
            },
            savedLocation = savedLocation,
            onSaveLocation = { updatedLoc ->
                savedLocation = updatedLoc
            },
            onOpenGpsPicker = {
                isLocationPickerOpen = true
            },
            onDismiss = { isSettingsOpen = false },
            onSwitchToPartner = onSwitchToPartner
        )
    }
}
