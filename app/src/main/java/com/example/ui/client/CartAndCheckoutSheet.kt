package com.example.ui.client

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import java.util.Locale

// Constantes Oficiales de Pago Móvil de Isaya Sushi Gourmet
const val PAGO_MOVIL_CEDULA = "17016897"
const val PAGO_MOVIL_BANCO = "0105 Mercantil"
const val PAGO_MOVIL_BANCO_NOMBRE = "Banco Mercantil"
const val PAGO_MOVIL_TELEFONO = "04220034452"
const val TASA_BCV_SIMULADA = 78.50

@Composable
fun FloatingCartBottomBar(
    cartItems: List<CartItem>,
    deliveryMode: DeliveryMode,
    onOpenCheckout: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    if (cartItems.isEmpty()) return

    val totalItems = cartItems.sumOf { it.quantity }
    val subtotal = cartItems.sumOf { it.totalPrice }
    val total = if (deliveryMode == DeliveryMode.PICKUP) subtotal else (subtotal + deliveryMode.fee)

    val surfaceBg = if (isDarkMode) DarkSurface else IosLightSurface
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorder
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val textSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldContainer = if (isDarkMode) GoldContainer else IosGoldContainer
    val redColor = if (isDarkMode) SushiRed else IosRedPrimary

    Surface(
        color = surfaceBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cart Summary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = goldContainer,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Carrito",
                            tint = goldColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "$totalItems platillo${if (totalItems > 1) "s" else ""}",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", total)} USD",
                        color = goldColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Checkout Button with 24dp rounded corners
            Button(
                onClick = onOpenCheckout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = redColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("open_checkout_sheet_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ver Pedido & Pagar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class CheckoutExtraItem(
    val id: String,
    val name: String,
    val price: Double,
    val icon: String
)

val DEFAULT_CHECKOUT_EXTRAS = listOf(
    CheckoutExtraItem("chopsticks", "Palillos Chinos Extra", 0.0, "🥢"),
    CheckoutExtraItem("teriyaki", "Salsa Teriyaki Dulce Extra", 0.50, "🍯"),
    CheckoutExtraItem("soya", "Salsa de Soya Tradicional Extra", 0.50, "🍶"),
    CheckoutExtraItem("fuji", "Salsa Fuji Especial Extra", 0.75, "✨"),
    CheckoutExtraItem("servilletas", "Servilletas y Utensilios", 0.0, "🧻")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    cartItems: List<CartItem>,
    deliveryMode: DeliveryMode,
    onUpdateQuantity: (CartItem, Int) -> Unit,
    onClearCart: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmOrder: (CustomerInfo, PaymentProof) -> Unit,
    isDarkMode: Boolean = false
) {
    val context = LocalContext.current
    var savedLocation by remember { mutableStateOf(com.example.util.UserLocationPreferences.getUserLocation(context)) }
    var isMapPickerOpen by remember {
        mutableStateOf(!savedLocation.isConfirmed || savedLocation.phone.isBlank())
    }

    // Dynamic Color Tokens
    val surfaceBg = if (isDarkMode) DarkSurface else IosLightSurface
    val cardSecondaryBg = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorder
    val borderSubtle = if (isDarkMode) DarkBorder.copy(alpha = 0.6f) else IosLightBorderSubtle
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val textSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val textMuted = if (isDarkMode) TextMuted else IosTextMuted
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldContainer = if (isDarkMode) GoldContainer else IosGoldContainer
    val goldBorder = if (isDarkMode) GoldDark else IosGoldBorder
    val redColor = if (isDarkMode) SushiRed else IosRedPrimary

    // Payment Reference & Attached Receipt State
    var paymentReference by remember { mutableStateOf("") }
    var referenceError by remember { mutableStateOf(false) }
    var attachedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var attachedReceiptFileName by remember { mutableStateOf<String?>(null) }

    // Activity Result Launcher for Media Selection (Zero permission Photo Picker as per Play Policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedReceiptUri = uri
            attachedReceiptFileName = "comprobante_${System.currentTimeMillis() % 10000}.jpg"
            referenceError = false
            Toast.makeText(context, "📎 Captura de pago adjuntada exitosamente", Toast.LENGTH_SHORT).show()
        }
    }

    // Extras and special instructions text tray
    val selectedExtras = remember { mutableStateListOf<String>("chopsticks", "servilletas") }
    var specialOrderNotes by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val extrasCost = DEFAULT_CHECKOUT_EXTRAS.filter { it.id in selectedExtras }.sumOf { it.price }
    val deliveryFee = if (deliveryMode == DeliveryMode.DELIVERY) 2.50 else 0.0
    val totalFinalUSD = subtotal + extrasCost + deliveryFee
    val totalFinalBs = totalFinalUSD * TASA_BCV_SIMULADA

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surfaceBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(borderColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tu Pedido",
                        color = goldColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Isaya Sushi Gourmet",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("Vaciar", color = redColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Lista de Platillos en el Carrito
            Text(
                text = "Resumen de Platillos (${cartItems.sumOf { it.quantity }})",
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cartItems.forEach { item ->
                    Surface(
                        color = cardSecondaryBg,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.preparationStyle,
                                    color = goldColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (item.selectedExtras.isNotEmpty()) {
                                    Text(
                                        text = "Extras: " + item.selectedExtras.joinToString(", ") { it.name },
                                        color = textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                                if (item.specialNotes.isNotBlank()) {
                                    Text(
                                        text = "Nota: \"${item.specialNotes}\"",
                                        color = textMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", item.totalPrice)} USD",
                                    color = goldColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Stepper Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isDarkMode) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f))
                                    .border(1.dp, borderSubtle, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { onUpdateQuantity(item, item.quantity - 1) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "Menos",
                                        tint = if (item.quantity == 1) redColor else textPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "${item.quantity}",
                                    color = goldColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = { onUpdateQuantity(item, item.quantity + 1) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Más",
                                        tint = goldColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 14.dp))

            // SECTION: Datos de Ubicación / Entrega
            Surface(
                color = cardSecondaryBg,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = goldColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Datos de Entrega / GPS",
                                color = goldColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = { isMapPickerOpen = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ajustar 📍",
                                color = goldColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (savedLocation.address.isNotBlank()) savedLocation.address else "Ubicación no configurada",
                        color = textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (savedLocation.phone.isNotBlank() || savedLocation.name.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${if (savedLocation.name.isNotBlank()) savedLocation.name else "Cliente Isaya"} • WhatsApp: ${savedLocation.phone}",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 14.dp))

            // SECTION: MÉTODO DE PAGO COMPACTO (PAGO MÓVIL MERCANTIL)
            Surface(
                color = cardSecondaryBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header: Bank Title & Compact Copy Icon in Top Right Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = goldContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = goldColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Pago Móvil $PAGO_MOVIL_BANCO",
                                    color = goldColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Transferencia Inmediata",
                                    color = textMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Compact Copy Button (Two overlapping papers icon)
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipText = """
                                    *ISAYA SUSHI GOURMET - PAGO MÓVIL*
                                    Cédula: $PAGO_MOVIL_CEDULA
                                    Banco: $PAGO_MOVIL_BANCO
                                    Teléfono: $PAGO_MOVIL_TELEFONO
                                    Monto: Bs. ${String.format(Locale.US, "%.2f", totalFinalBs)} ($${String.format(Locale.US, "%.2f", totalFinalUSD)} USD)
                                """.trimIndent()
                                val clip = ClipData.newPlainText("Datos Pago Móvil Isaya", clipText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "✅ ¡Datos de Pago Móvil copiados!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(goldContainer)
                                .border(0.5.dp, goldBorder, CircleShape)
                                .testTag("copy_pago_movil_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar datos",
                                tint = goldColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compact Data Box (Cédula, Banco, Teléfono, Monto)
                    Surface(
                        color = if (isDarkMode) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CI: $PAGO_MOVIL_CEDULA", color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Tel: $PAGO_MOVIL_TELEFONO", color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Monto a Transferir:", color = textSecondary, fontSize = 10.sp)
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", totalFinalUSD)} USD ≈ Bs. ${String.format(Locale.US, "%.2f", totalFinalBs)}",
                                    color = goldColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Single Horizontal Row (Row) for Reference Input & Attach Capture Button (Saves 50% vertical height)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Campo para ingresar los últimos 4 dígitos de la referencia (50% width)
                        OutlinedTextField(
                            value = paymentReference,
                            onValueChange = {
                                if (it.length <= 8) {
                                    paymentReference = it
                                    referenceError = false
                                }
                            },
                            placeholder = { Text("4 dígitos ref.", color = textMuted, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = goldColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            isError = referenceError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("payment_reference_input"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = surfaceBg,
                                unfocusedContainerColor = surfaceBg,
                                focusedBorderColor = goldColor,
                                unfocusedBorderColor = borderColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                cursorColor = goldColor,
                                errorContainerColor = surfaceBg,
                                errorBorderColor = redColor
                            )
                        )

                        // 2. Botón de Comprobante / Badge (50% width)
                        if (attachedReceiptUri == null) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("attach_receipt_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = surfaceBg,
                                    contentColor = goldColor
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = "Adjuntar comprobante",
                                        tint = goldColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Capture 📎",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = goldContainer,
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Adjuntado",
                                            color = textPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            attachedReceiptUri = null
                                            attachedReceiptFileName = null
                                        },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = redColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (referenceError) {
                        Text(
                            text = "⚠️ Ingresa los últimos 4 dígitos o adjunta la captura del pago.",
                            color = redColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cost Breakdown
            Surface(
                color = cardSecondaryBg,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal de Platillos", color = textSecondary, fontSize = 13.sp)
                        Text("$${String.format(Locale.US, "%.2f", subtotal)} USD", color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    if (extrasCost > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Extras Seleccionados", color = textSecondary, fontSize = 13.sp)
                            Text("+$${String.format(Locale.US, "%.2f", extrasCost)} USD", color = goldColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (deliveryMode == DeliveryMode.DELIVERY) "Costo de Envío (Delivery)" else "Retiro en Local",
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (deliveryMode == DeliveryMode.DELIVERY) "$${String.format(Locale.US, "%.2f", deliveryFee)} USD" else "GRATIS ($0.00)",
                            color = goldColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total a Pagar", color = textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Tasa BCV: Bs. ${String.format(Locale.US, "%.2f", totalFinalBs)}", color = textMuted, fontSize = 11.sp)
                        }
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalFinalUSD)} USD",
                            color = goldColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Confirm Order Button with Flexibilized Validation (Reference OR Receipt)
            Button(
                onClick = {
                    if (!savedLocation.isConfirmed || savedLocation.phone.isBlank() || savedLocation.address.isBlank()) {
                        isMapPickerOpen = true
                        Toast.makeText(
                            context,
                            "Por favor completa tu ubicación y WhatsApp para confirmar el pedido",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    val hasRef = paymentReference.trim().isNotBlank()
                    val hasReceipt = attachedReceiptUri != null

                    // Regla Flexibilizada: al menos uno de los dos métodos debe estar completado
                    if (!hasRef && !hasReceipt) {
                        referenceError = true
                        Toast.makeText(
                            context,
                            "Por favor ingresa los 4 dígitos de referencia o adjunta la captura del pago",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    val extraNames = DEFAULT_CHECKOUT_EXTRAS.filter { it.id in selectedExtras }.map { it.name }
                    val notesSummary = buildString {
                        if (extraNames.isNotEmpty()) append("Extras: ${extraNames.joinToString(", ")}. ")
                        if (specialOrderNotes.isNotBlank()) append("Notas: ${specialOrderNotes.trim()}")
                    }

                    val customer = CustomerInfo(
                        fullName = if (savedLocation.name.isNotBlank()) savedLocation.name else "Cliente Isaya",
                        whatsappPhone = savedLocation.phone,
                        deliveryAddress = savedLocation.address,
                        referencePoint = notesSummary
                    )

                    val finalRefNumber = if (hasRef) paymentReference.trim() else "CAP-${System.currentTimeMillis() % 10000}"
                    val payment = PaymentProof(
                        bankName = PAGO_MOVIL_BANCO,
                        rif = PAGO_MOVIL_CEDULA,
                        phone = PAGO_MOVIL_TELEFONO,
                        referenceDigits = finalRefNumber,
                        receiptAttached = hasReceipt,
                        receiptFileName = attachedReceiptFileName ?: "pagomovil_${finalRefNumber}.jpg"
                    )
                    onConfirmOrder(customer, payment)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_order_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = redColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Confirmar Pedido • $${String.format(Locale.US, "%.2f", totalFinalUSD)} USD",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal: Interactive GPS Location Map Picker for quick editing
    if (isMapPickerOpen) {
        InteractiveLocationPickerModal(
            onDismiss = {
                if (savedLocation.isConfirmed && savedLocation.phone.isNotBlank()) {
                    isMapPickerOpen = false
                }
            },
            onLocationConfirmed = { newLocation ->
                savedLocation = newLocation
                isMapPickerOpen = false
            },
            canDismiss = savedLocation.isConfirmed && savedLocation.phone.isNotBlank(),
            isDarkMode = isDarkMode
        )
    }
}

@Composable
private fun PaymentInfoRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun DeliveryZonePickerDialog(
    currentZone: DeliveryZone?,
    onZoneSelected: (DeliveryZone) -> Unit,
    onDismiss: () -> Unit,
    isDarkMode: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriceFilter by remember { mutableStateOf<Double?>(null) }

    val surfaceBg = if (isDarkMode) DarkSurface else IosLightSurface
    val cardSecondaryBg = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorder
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val textSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldContainer = if (isDarkMode) GoldContainer else IosGoldContainer

    val filteredZones = remember(searchQuery, selectedPriceFilter) {
        MARACAY_DELIVERY_ZONES.filter { zone ->
            val matchesQuery = searchQuery.isBlank() ||
                    zone.name.contains(searchQuery, ignoreCase = true)
            val matchesPrice = selectedPriceFilter == null || zone.fee == selectedPriceFilter
            matchesQuery && matchesPrice
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .heightIn(max = 560.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceBg),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seleccionar Zona de Envío",
                        color = goldColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = textSecondary
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar urbanización o sector...", color = textSecondary, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = goldColor,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardSecondaryBg,
                        unfocusedContainerColor = cardSecondaryBg,
                        focusedBorderColor = goldColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                // Price Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val priceOptions = listOf(null to "Todas", 1.00 to "$1.00", 2.00 to "$2.00", 3.00 to "$3.00", 4.00 to "$4.00", 5.00 to "$5.00", 6.00 to "$6.00")
                    priceOptions.forEach { (price, label) ->
                        val isSelected = selectedPriceFilter == price
                        Surface(
                            color = if (isSelected) goldContainer else cardSecondaryBg,
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) goldColor else borderColor
                            ),
                            modifier = Modifier.clickable { selectedPriceFilter = price }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) goldColor else textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Zones List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredZones, key = { it.name }) { zone ->
                        val isSelected = currentZone?.name == zone.name
                        Surface(
                            color = if (isSelected) goldContainer else cardSecondaryBg,
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) goldColor else borderColor.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onZoneSelected(zone)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = zone.name,
                                    color = if (isSelected) goldColor else textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = if (isSelected) goldColor else goldContainer,
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", zone.fee)} USD",
                                        color = if (isSelected) Color.White else goldColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
