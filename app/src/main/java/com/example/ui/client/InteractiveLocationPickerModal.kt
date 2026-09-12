package com.example.ui.client

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.util.SavedUserLocation
import com.example.util.UserLocationPreferences
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Known sectors in Maracay with landmark coordinates
data class MaracaySectorPoint(
    val name: String,
    val zone: String,
    val lat: Double,
    val lng: Double,
    val baseStreet: String
)

val MARACAY_SECTOR_POINTS = listOf(
    MaracaySectorPoint("La Soledad", "Zona Norte", 10.2542, -67.5992, "Av. Las Delicias con Calle 2, Urb. La Soledad"),
    MaracaySectorPoint("Base Aragua", "Zona Centro-Norte", 10.2486, -67.6041, "Av. Fuerzas Aéreas con Av. Casanova Godoy, Base Aragua"),
    MaracaySectorPoint("San Jacinto", "Zona Este", 10.2435, -67.5712, "Av. Principal de San Jacinto, Sector Los Samanes"),
    MaracaySectorPoint("El Castaño", "Zona Norte Alta", 10.2850, -67.5920, "Av. Principal El Castaño, Residencias del Norte"),
    MaracaySectorPoint("Calicanto", "Zona Centro", 10.2498, -67.5930, "Calle López Aveledo con Av. 19 de Abril, Calicanto"),
    MaracaySectorPoint("El Bosque", "Zona Norte", 10.2580, -67.6030, "Av. Las Delicias, Urb. El Bosque"),
    MaracaySectorPoint("Centro / Bolívar", "Zona Centro", 10.2469, -67.5958, "Av. Bolívar Cruce con Calle Santos Michelena, Centro"),
    MaracaySectorPoint("Parque Aragua", "Zona Centro-Este", 10.2470, -67.5850, "Av. Bolívar Este frente a CC Parque Aragua"),
    MaracaySectorPoint("Las Acacias", "Zona Sur-Este", 10.2310, -67.5890, "Av. Fuerzas Aéreas, Urb. Las Acacias"),
    MaracaySectorPoint("Turmero Centro", "Zona Metropolitana", 10.2280, -67.4720, "Av. Intercomunal Maracay-Turmero")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveLocationPickerModal(
    onDismiss: () -> Unit,
    onLocationConfirmed: (SavedUserLocation) -> Unit,
    canDismiss: Boolean = true,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val initialLocation = remember { UserLocationPreferences.getUserLocation(context) }

    // Coordinates state
    var latitude by remember { mutableDoubleStateOf(initialLocation.latitude) }
    var longitude by remember { mutableDoubleStateOf(initialLocation.longitude) }

    // User details state
    var customerName by remember { mutableStateOf(initialLocation.name) }
    var customerPhone by remember { mutableStateOf(initialLocation.phone) }
    var deliveryAddress by remember { mutableStateOf(initialLocation.address) }
    var referenceNotes by remember { mutableStateOf("") }

    // UI and Map interaction state
    var isGpsSearching by remember { mutableStateOf(false) }
    var gpsStatusMessage by remember { mutableStateOf("📍 Mueve el mapa o pin para ajustar la ubicación") }
    var isPhoneError by remember { mutableStateOf(false) }
    var mapZoom by remember { mutableFloatStateOf(1f) } // Zoom factor 0.6f .. 2.5f

    // Map drag/offset in canvas pixels (relative to center)
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }

    // Reverse geocode function
    fun updateAddressFromCoordinates(lat: Double, lng: Double) {
        coroutineScope.launch {
            try {
                val addressText = withContext(Dispatchers.IO) {
                    var resolvedText: String? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        try {
                            val geocoder = Geocoder(context, Locale("es", "VE"))
                            val addresses = geocoder.getFromLocation(lat, lng, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: ""
                                val subAdmin = addr.subAdminArea ?: "Maracay"
                                if (thoroughfare.isNotBlank()) {
                                    resolvedText = "$thoroughfare, $subAdmin"
                                }
                            }
                        } catch (_: Exception) {}
                    } else {
                        try {
                            @Suppress("DEPRECATION")
                            val geocoder = Geocoder(context, Locale("es", "VE"))
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(lat, lng, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: ""
                                val subAdmin = addr.subAdminArea ?: "Maracay"
                                if (thoroughfare.isNotBlank()) {
                                    resolvedText = "$thoroughfare, $subAdmin"
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    if (resolvedText == null) {
                        // Find closest Maracay sector
                        val nearest = MARACAY_SECTOR_POINTS.minByOrNull {
                            val dLat = it.lat - lat
                            val dLng = it.lng - lng
                            dLat * dLat + dLng * dLng
                        }
                        nearest?.let {
                            "${it.baseStreet} (${String.format(Locale.US, "Lat: %.4f, Lng: %.4f", lat, lng)})"
                        } ?: "Av. Las Delicias, Urb. La Soledad, Maracay"
                    } else {
                        resolvedText
                    }
                }

                deliveryAddress = addressText
                gpsStatusMessage = "✅ Ubicación ajustada: $addressText"
            } catch (e: Exception) {
                deliveryAddress = "Maracay, Aragua (Lat: ${String.format(Locale.US, "%.4f", lat)}, Lng: ${String.format(Locale.US, "%.4f", lng)})"
            }
        }
    }

    // GPS Location Fetching
    fun fetchDeviceGpsLocation() {
        isGpsSearching = true
        gpsStatusMessage = "📡 Conectando con satélites GPS..."
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    isGpsSearching = false
                    if (loc != null) {
                        latitude = loc.latitude
                        longitude = loc.longitude
                        mapOffsetX = 0f
                        mapOffsetY = 0f
                        updateAddressFromCoordinates(loc.latitude, loc.longitude)
                        gpsStatusMessage = "📍 GPS exacto fijado (Precisión: ±${loc.accuracy.toInt()}m)"
                    } else {
                        // Fallback to Maracay Central landmark
                        latitude = 10.2520
                        longitude = -67.5980
                        updateAddressFromCoordinates(10.2520, -67.5980)
                        gpsStatusMessage = "📍 Ubicación fijada en Maracay Centro-Norte"
                    }
                }.addOnFailureListener {
                    isGpsSearching = false
                    gpsStatusMessage = "⚠️ Usando punto de referencia en Maracay"
                }
            } else {
                isGpsSearching = false
                gpsStatusMessage = "⚠️ Permiso de ubicación no concedido"
            }
        } catch (e: Exception) {
            isGpsSearching = false
            gpsStatusMessage = "📍 Ubicación fijada en Maracay"
        }
    }

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchDeviceGpsLocation()
        } else {
            gpsStatusMessage = "📍 Modo manual: Arrastra el marcador en el mapa"
        }
    }

    // Request GPS on launch
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            fetchDeviceGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Theme tokens
    val formBg = if (isDarkMode) DarkSurface else IosLightSurface
    val formBorder = if (isDarkMode) DarkBorder else IosLightBorder
    val formTextPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val formTextSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldContainer = if (isDarkMode) GoldContainer else IosGoldContainer
    val goldBorderColor = if (isDarkMode) GoldPrimary.copy(alpha = 0.4f) else IosGoldBorder

    Dialog(
        onDismissRequest = {
            if (canDismiss) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss,
            usePlatformDefaultWidth = false
        )
    ) {
        // Translucent backdrop overlay (Scrim estilo iOS)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    enabled = canDismiss
                ) { onDismiss() }
                .padding(horizontal = 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Floating Centered Modal Card
            Surface(
                modifier = modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.88f)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {}, // Intercept clicks inside card
                shape = RoundedCornerShape(28.dp),
                color = IosDarkMapBg,
                shadowElevation = 24.dp,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, goldBorderColor)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Navigation Bar inside Floating Modal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IosDarkMapSurface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = goldContainer,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = goldColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (!canDismiss) "Registro de Entrega (Obligatorio)" else "Dirección de Entrega & GPS",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (!canDismiss) "Requerido para procesar la orden" else "Isaya Sushi Delivery • Maracay",
                                    color = goldColor,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Close "X" Button (permite cerrar y explorar libremente si canDismiss == true)
                        if (canDismiss) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IosDarkMapCard)
                                    .border(1.dp, IosDarkMapBorder, CircleShape)
                                    .testTag("close_location_picker_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar y Explorar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = goldContainer,
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, goldBorderColor)
                            ) {
                                Text(
                                    text = "Obligatorio",
                                    color = goldColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Quick Sector Chips selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IosDarkMapSurface)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MARACAY_SECTOR_POINTS.take(6).forEach { sector ->
                            Surface(
                                color = IosDarkMapCard,
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IosDarkMapBorder),
                                modifier = Modifier.clickable {
                                    latitude = sector.lat
                                    longitude = sector.lng
                                    mapOffsetX = 0f
                                    mapOffsetY = 0f
                                    updateAddressFromCoordinates(sector.lat, sector.lng)
                                }
                            ) {
                                Text(
                                    text = "📍 ${sector.name}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Interactive Map View Container (Weight 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(IosDarkMapBg)
                            .testTag("interactive_map_container")
                    ) {
                        // Dark theme styled Map Canvas with roads, zones, rivers and grid
                        DarkStyledMapCanvas(
                            offsetX = mapOffsetX,
                            offsetY = mapOffsetY,
                            zoom = mapZoom,
                            onDrag = { dx, dy ->
                                mapOffsetX += dx
                                mapOffsetY += dy
                                // Translate delta in pixels to lat/lng delta
                                val factor = 0.00005 / mapZoom
                                latitude -= dy * factor
                                longitude += dx * factor
                                updateAddressFromCoordinates(latitude, longitude)
                            }
                        )

                        // Pin Dorado fijo en el centro del viewport del mapa
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-18).dp)
                        ) {
                            GoldenLocationPinMarker()
                        }

                        // Floating Controls on Map: GPS Recentering and Zoom Buttons
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // GPS Re-center button
                            FloatingActionButton(
                                onClick = {
                                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (hasFine) {
                                        fetchDeviceGpsLocation()
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        )
                                    }
                                },
                                containerColor = goldColor,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("btn_gps_recenter")
                            ) {
                                if (isGpsSearching) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Mi GPS",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Zoom In Button
                            IconButton(
                                onClick = { mapZoom = (mapZoom * 1.25f).coerceAtMost(3.0f) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(IosDarkMapCard)
                                    .border(1.dp, IosDarkMapBorder, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom +", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            // Zoom Out Button
                            IconButton(
                                onClick = { mapZoom = (mapZoom * 0.8f).coerceAtLeast(0.5f) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(IosDarkMapCard)
                                    .border(1.dp, IosDarkMapBorder, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom -", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Live GPS Status & Coordinates Pill Overlay
                        Surface(
                            color = IosDarkMapCard.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, goldBorderColor),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .widthIn(max = 240.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isGpsSearching) goldColor else IosGreenPrimary)
                                    )
                                    Text(
                                        text = if (isGpsSearching) "Buscando GPS..." else "GPS Fijado",
                                        color = if (isGpsSearching) goldColor else IosGreenPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lat: ${String.format(Locale.US, "%.5f", latitude)} | Lng: ${String.format(Locale.US, "%.5f", longitude)}",
                                    color = goldColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Bottom Form Sheet for Address and Customer Details (Scrollable)
                    Surface(
                        color = formBg,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, formBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Address Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PinDrop,
                                    contentDescription = null,
                                    tint = goldColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Punto de Entrega Seleccionado",
                                    color = formTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Dynamic Address Field with 24dp shape
                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                label = { Text("Dirección de Entrega Geocodificada *", color = formTextSecondary, fontSize = 11.sp) },
                                placeholder = { Text("Calle, edificio, casa o urbanización", color = IosTextMuted, fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("map_address_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = formBg,
                                    unfocusedContainerColor = formBg,
                                    focusedBorderColor = goldColor,
                                    unfocusedBorderColor = formBorder,
                                    focusedTextColor = formTextPrimary,
                                    unfocusedTextColor = formTextPrimary,
                                    cursorColor = goldColor
                                ),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Customer Details (Phone is mandatory, Name is optional)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Phone (Mandatory)
                                OutlinedTextField(
                                    value = customerPhone,
                                    onValueChange = {
                                        customerPhone = it
                                        if (it.isNotBlank()) isPhoneError = false
                                    },
                                    label = { Text("WhatsApp Obligatorio *", color = if (isPhoneError) IosRedPrimary else formTextSecondary, fontSize = 11.sp) },
                                    placeholder = { Text("0414 1234567", color = IosTextMuted, fontSize = 11.sp) },
                                    isError = isPhoneError,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .testTag("map_phone_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = formBg,
                                        unfocusedContainerColor = formBg,
                                        focusedBorderColor = goldColor,
                                        unfocusedBorderColor = if (isPhoneError) IosRedPrimary else formBorder,
                                        focusedTextColor = formTextPrimary,
                                        unfocusedTextColor = formTextPrimary,
                                        cursorColor = goldColor
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    singleLine = true
                                )

                                // Name (Optional)
                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("Nombre (Opcional)", color = formTextSecondary, fontSize = 11.sp) },
                                    placeholder = { Text("Ej. Carlos", color = IosTextMuted, fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .testTag("map_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = formBg,
                                        unfocusedContainerColor = formBg,
                                        focusedBorderColor = goldColor,
                                        unfocusedBorderColor = formBorder,
                                        focusedTextColor = formTextPrimary,
                                        unfocusedTextColor = formTextPrimary,
                                        cursorColor = goldColor
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    singleLine = true
                                )
                            }

                            if (isPhoneError) {
                                Text(
                                    text = "⚠️ Por favor ingresa tu número de WhatsApp para confirmar el delivery.",
                                    color = IosRedPrimary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Floating Confirmation Button: "Confirmar esta ubicación" with 24dp shape
                            Button(
                                onClick = {
                                    if (customerPhone.isBlank()) {
                                        isPhoneError = true
                                    } else {
                                        // Save to SharedPreferences permanently
                                        UserLocationPreferences.saveUserLocation(
                                            context = context,
                                            name = customerName.trim(),
                                            phone = customerPhone.trim(),
                                            address = deliveryAddress.trim(),
                                            latitude = latitude,
                                            longitude = longitude,
                                            isConfirmed = true
                                        )
                                        onLocationConfirmed(
                                            SavedUserLocation(
                                                name = customerName.trim(),
                                                phone = customerPhone.trim(),
                                                address = deliveryAddress.trim(),
                                                latitude = latitude,
                                                longitude = longitude,
                                                isConfirmed = true
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = goldColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(4.dp, RoundedCornerShape(24.dp))
                                    .testTag("btn_confirm_location")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Confirmar y Guardar Ubicación",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dark theme interactive Canvas rendering a stylized vector map of Maracay
 * with avenues, city blocks, rivers, green areas and sector labels.
 */
@Composable
fun DarkStyledMapCanvas(
    offsetX: Float,
    offsetY: Float,
    zoom: Float,
    onDrag: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f + offsetX
        val centerY = height / 2f + offsetY

        // Background Dark Map Ocean / Base Land
        drawRect(color = Color(0xFF0F141C))

        // Grid Blocks (Residential city blocks)
        val blockSize = 60f * zoom
        val gridStartX = (centerX % blockSize) - blockSize
        val gridStartY = (centerY % blockSize) - blockSize

        var x = gridStartX
        while (x < width + blockSize) {
            var y = gridStartY
            while (y < height + blockSize) {
                // City Block Rectangles
                drawRoundRect(
                    color = Color(0xFF18202C),
                    topLeft = Offset(x + 4f * zoom, y + 4f * zoom),
                    size = androidx.compose.ui.geometry.Size(blockSize - 8f * zoom, blockSize - 8f * zoom),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * zoom, 6f * zoom)
                )
                y += blockSize
            }
            x += blockSize
        }

        // Green Areas / Parks (Parque Las Ballenas, El Castaño Hills)
        val parkPath = Path().apply {
            moveTo(centerX + 120f * zoom, centerY - 150f * zoom)
            lineTo(centerX + 260f * zoom, centerY - 140f * zoom)
            lineTo(centerX + 240f * zoom, centerY - 40f * zoom)
            lineTo(centerX + 110f * zoom, centerY - 50f * zoom)
            close()
        }
        drawPath(parkPath, color = Color(0xFF142B20)) // Dark Forest Green

        val parkPath2 = Path().apply {
            moveTo(centerX - 240f * zoom, centerY + 80f * zoom)
            lineTo(centerX - 120f * zoom, centerY + 70f * zoom)
            lineTo(centerX - 130f * zoom, centerY + 180f * zoom)
            lineTo(centerX - 250f * zoom, centerY + 160f * zoom)
            close()
        }
        drawPath(parkPath2, color = Color(0xFF142B20))

        // Secondary Streets (Cian-Gray)
        val streetStroke = 3f * zoom
        var sx = gridStartX
        while (sx < width + blockSize) {
            drawLine(
                color = Color(0xFF243042),
                start = Offset(sx, 0f),
                end = Offset(sx, height),
                strokeWidth = streetStroke
            )
            sx += blockSize
        }

        var sy = gridStartY
        while (sy < height + blockSize) {
            drawLine(
                color = Color(0xFF243042),
                start = Offset(0f, sy),
                end = Offset(width, sy),
                strokeWidth = streetStroke
            )
            sy += blockSize
        }

        // Primary Avenues (Av. Las Delicias, Av. Bolívar, Av. Casanova Godoy, Av. Fuerzas Aéreas)
        val avenueStroke = 8f * zoom
        val avenueColor = Color(0xFF2E3D52)
        val avenueHighlight = Color(0xFFD4AF37).copy(alpha = 0.35f)

        // Av. Las Delicias (North - South arterial highway)
        drawLine(
            color = avenueColor,
            start = Offset(centerX - 30f * zoom, 0f),
            end = Offset(centerX - 30f * zoom, height),
            strokeWidth = avenueStroke
        )
        drawLine(
            color = avenueHighlight,
            start = Offset(centerX - 30f * zoom, 0f),
            end = Offset(centerX - 30f * zoom, height),
            strokeWidth = 2f * zoom
        )

        // Av. Casanova Godoy (East - West arterial)
        drawLine(
            color = avenueColor,
            start = Offset(0f, centerY - 60f * zoom),
            end = Offset(width, centerY - 60f * zoom),
            strokeWidth = avenueStroke
        )
        drawLine(
            color = avenueHighlight,
            start = Offset(0f, centerY - 60f * zoom),
            end = Offset(width, centerY - 60f * zoom),
            strokeWidth = 2f * zoom
        )

        // Av. Bolívar (East - West major artery)
        drawLine(
            color = avenueColor,
            start = Offset(0f, centerY + 90f * zoom),
            end = Offset(width, centerY + 90f * zoom),
            strokeWidth = avenueStroke
        )
        drawLine(
            color = avenueHighlight,
            start = Offset(0f, centerY + 90f * zoom),
            end = Offset(width, centerY + 90f * zoom),
            strokeWidth = 2f * zoom
        )

        // Av. Fuerzas Aéreas (Diagonal artery)
        drawLine(
            color = avenueColor,
            start = Offset(centerX + 80f * zoom, 0f),
            end = Offset(centerX + 80f * zoom, height),
            strokeWidth = avenueStroke
        )

        // Pulse wave effect around center (active GPS scan zone)
        drawCircle(
            color = Color(0xFFD4AF37).copy(alpha = 0.08f),
            radius = 90f * zoom,
            center = Offset(width / 2f, height / 2f)
        )
        drawCircle(
            color = Color(0xFFD4AF37).copy(alpha = 0.25f),
            radius = 90f * zoom,
            center = Offset(width / 2f, height / 2f),
            style = Stroke(width = 1.5f)
        )
    }
}

/**
 * High-craft Golden Location Pin Marker (#D4AF37) with drop shadow and pulse anchor.
 */
@Composable
fun GoldenLocationPinMarker(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Pin Head with Gold Gradient and inner dot
        Surface(
            color = IosGoldPrimary,
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(2.5.dp, Color.White),
            modifier = Modifier
                .size(40.dp)
                .shadow(8.dp, CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(14.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            color = IosGoldPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(6.dp)
                        ) {}
                    }
                }
            }
        }

        // Pin Pointer Triangle
        Canvas(modifier = Modifier.size(width = 14.dp, height = 10.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, color = Color(0xFFD4AF37))
        }

        // Anchor Shadow Dot on ground
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(5.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        )
    }
}
