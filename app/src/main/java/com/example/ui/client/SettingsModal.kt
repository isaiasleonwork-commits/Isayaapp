package com.example.ui.client

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.SavedUserLocation
import com.example.util.UserLocationPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModal(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    savedLocation: SavedUserLocation,
    onSaveLocation: (SavedUserLocation) -> Unit,
    onOpenGpsPicker: () -> Unit,
    onDismiss: () -> Unit,
    onSwitchToPartner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editName by remember(savedLocation) { mutableStateOf(savedLocation.name) }
    var editPhone by remember(savedLocation) { mutableStateOf(savedLocation.phone) }
    var editAddress by remember(savedLocation) { mutableStateOf(savedLocation.address) }
    var isSaving by remember { mutableStateOf(false) }

    // Expandable states for legal/info sections
    var expandedSection by remember { mutableStateOf<String?>(null) }

    // Dynamic color tokens based on active theme
    val modalBg = if (isDarkMode) DeepBlack else IosLightBg
    val cardBg = if (isDarkMode) DarkCard else IosLightSurface
    val cardSecondaryBg = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorder
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val textSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val textMuted = if (isDarkMode) TextMuted else IosTextMuted
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldContainer = if (isDarkMode) GoldContainer else IosGoldContainer
    val goldBorder = if (isDarkMode) GoldDark else IosGoldBorder

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(modalBg),
            color = modalBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkMode) DarkSurface else IosLightSurface)
                        .border(0.5.dp, borderColor)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
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
                            border = BorderStroke(1.dp, goldBorder),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = goldColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Ajustes de la App",
                                color = textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Isaya Sushi Gourmet • Configuración",
                                color = goldColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cardSecondaryBg)
                            .testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar Ajustes",
                            tint = textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECTION 1: Selector de Tema (Oscuro / Claro)
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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
                                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = goldColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Apariencia Visual",
                                            color = textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isDarkMode) "Tema Oscuro (#0D0D0D y Dorado)" else "Tema Claro (Estilo iOS Minimal)",
                                            color = textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { onToggleDarkMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = goldColor,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = IosTextMuted
                                    ),
                                    modifier = Modifier.testTag("toggle_dark_mode_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Visual Theme Selector Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Light Theme Option Card
                                Surface(
                                    color = if (!isDarkMode) IosGoldContainer else cardSecondaryBg,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(
                                        if (!isDarkMode) 1.5.dp else 1.dp,
                                        if (!isDarkMode) goldColor else borderColor
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onToggleDarkMode(false) }
                                        .testTag("select_light_theme_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = if (!isDarkMode) goldColor else textMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Tema Claro",
                                            color = if (!isDarkMode) goldColor else textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "iOS Blanco & Oro",
                                            color = textSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Dark Theme Option Card
                                Surface(
                                    color = if (isDarkMode) GoldContainer else cardSecondaryBg,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(
                                        if (isDarkMode) 1.5.dp else 1.dp,
                                        if (isDarkMode) goldColor else borderColor
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onToggleDarkMode(true) }
                                        .testTag("select_dark_theme_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = if (isDarkMode) goldColor else textMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Tema Oscuro",
                                            color = if (isDarkMode) goldColor else textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Deep Black & Oro",
                                            color = textSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: Editar Datos de Entrega y Geolocalización
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = goldColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Datos de Entrega & GPS",
                                            color = textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Configura tu dirección fija para pedidos",
                                            color = textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (savedLocation.isConfirmed) {
                                    Surface(
                                        color = if (isDarkMode) StatusGreenContainer else IosGreenContainer,
                                        shape = RoundedCornerShape(24.dp),
                                        border = BorderStroke(1.dp, if (isDarkMode) StatusGreen else IosGreenBorder)
                                    ) {
                                        Text(
                                            text = "Guardada",
                                            color = if (isDarkMode) StatusGreen else IosGreenPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive GPS Location button
                            Surface(
                                color = goldContainer,
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, goldBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenGpsPicker() }
                                    .testTag("settings_open_gps_map_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditLocation,
                                            contentDescription = null,
                                            tint = goldColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Reajustar Ubicación en Mapa GPS",
                                                color = goldColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Abre el mapa interactivo con sensor GPS",
                                                color = textSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        color = goldColor,
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text(
                                            text = "Mapa 📍",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Text Fields
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Nombre del Cliente", color = textSecondary, fontSize = 12.sp) },
                                placeholder = { Text("Ej. Sofía Hernández", color = textMuted, fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = cardSecondaryBg,
                                    unfocusedContainerColor = cardSecondaryBg,
                                    focusedBorderColor = goldColor,
                                    unfocusedBorderColor = borderColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    cursorColor = goldColor
                                ),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Teléfono WhatsApp", color = textSecondary, fontSize = 12.sp) },
                                placeholder = { Text("Ej. 0414-1234567", color = textMuted, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_phone_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = cardSecondaryBg,
                                    unfocusedContainerColor = cardSecondaryBg,
                                    focusedBorderColor = goldColor,
                                    unfocusedBorderColor = borderColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    cursorColor = goldColor
                                ),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = editAddress,
                                onValueChange = { editAddress = it },
                                label = { Text("Dirección de Entrega en Maracay", color = textSecondary, fontSize = 12.sp) },
                                placeholder = { Text("Calle, edificio, urbanización...", color = textMuted, fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_address_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = cardSecondaryBg,
                                    unfocusedContainerColor = cardSecondaryBg,
                                    focusedBorderColor = goldColor,
                                    unfocusedBorderColor = borderColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    cursorColor = goldColor
                                ),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Save Location Button
                            Button(
                                onClick = {
                                    val updatedLoc = savedLocation.copy(
                                        name = editName.trim(),
                                        phone = editPhone.trim(),
                                        address = editAddress.trim(),
                                        isConfirmed = true
                                    )
                                    UserLocationPreferences.saveUserLocation(
                                        context = context,
                                        name = updatedLoc.name,
                                        phone = updatedLoc.phone,
                                        address = updatedLoc.address,
                                        latitude = updatedLoc.latitude,
                                        longitude = updatedLoc.longitude,
                                        isConfirmed = true
                                    )
                                    onSaveLocation(updatedLoc)
                                    Toast.makeText(context, "✅ Datos de entrega guardados exitosamente", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = goldColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("settings_save_location_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Guardar Cambios de Entrega",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // SECTION 3: Ajustes Básicos & Información
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = goldColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Ajustes Básicos & Legal",
                                        color = textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Políticas, términos y datos del restaurante",
                                        color = textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Option 1: Términos y Condiciones
                            SettingsExpandableItem(
                                title = "Términos y Condiciones",
                                icon = Icons.Default.Description,
                                isExpanded = expandedSection == "terms",
                                onClick = {
                                    expandedSection = if (expandedSection == "terms") null else "terms"
                                },
                                isDarkMode = isDarkMode
                            ) {
                                Text(
                                    text = "1. Horario de Servicio: Nuestro horario de atención al público es de lunes a domingo de 12:00 PM a 10:30 PM en Maracay, Edo. Aragua.\n\n" +
                                            "2. Métodos de Pago: Aceptamos Pago Móvil Banesco a tasa oficial, Efectivo USD en entrega y transferencias bancarias directas.\n\n" +
                                            "3. Preparación y Despacho: Cada orden de sushi se elabora al momento con insumos frescos certificados. El tiempo estimado de cocina oscila entre 20 y 35 minutos según la afluencia.\n\n" +
                                            "4. Garantía de Calidad: Si tu orden presenta alguna anomalía en temperatura o presentación, comunícate con soporte en los primeros 30 minutos tras la recepción para reposición inmediata.",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Option 2: Política de Privacidad
                            SettingsExpandableItem(
                                title = "Política de Privacidad",
                                icon = Icons.Default.Security,
                                isExpanded = expandedSection == "privacy",
                                onClick = {
                                    expandedSection = if (expandedSection == "privacy") null else "privacy"
                                },
                                isDarkMode = isDarkMode
                            ) {
                                Text(
                                    text = "1. Datos Personales: La información suministrada (nombre, teléfono y dirección) se almacena localmente de forma segura en tu dispositivo para agilizar tus compras.\n\n" +
                                            "2. Uso de Geolocalización GPS: El acceso al GPS se utiliza exclusivamente para posicionar el pin de entrega sobre el mapa de Maracay y garantizar que el repartidor llegue a tu puerta exacta.\n\n" +
                                            "3. Comunicaciones: Tu teléfono WhatsApp sólo será contactado para actualizaciones en vivo sobre el estatus de tu comanda o confirmación de pago móvil.\n\n" +
                                            "4. Privacidad Total: No compartimos ni comercializamos tu información con terceras empresas ni agencias publicitarias.",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Option 3: Acerca de Isaya Sushi
                            SettingsExpandableItem(
                                title = "Acerca de Isaya Sushi",
                                icon = Icons.Default.Store,
                                isExpanded = expandedSection == "about",
                                onClick = {
                                    expandedSection = if (expandedSection == "about") null else "about"
                                },
                                isDarkMode = isDarkMode
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "🥢 Isaya Sushi Gourmet",
                                        color = goldColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Somos pioneros en la alta cocina Nikkei y el arte culinario japonés contemporáneo en Maracay. Combinamos la técnica milenaria del itamae con ingredientes frescos y salsas de autor.",
                                        color = textSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📍 Sede Principal: Av. Las Delicias con Callejón López Aveledo, Urb. La Soledad, Maracay, Edo. Aragua.",
                                        color = textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "📞 WhatsApp Soporte: +58 414-1234567\n📸 Instagram: @isayasushigourmet",
                                        color = textSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = cardSecondaryBg,
                                        shape = RoundedCornerShape(24.dp),
                                        border = BorderStroke(1.dp, borderColor)
                                    ) {
                                        Text(
                                            text = "App Versión 2.4.0 • Build Premium 2026",
                                            color = textMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Switch to Partner Button
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onSwitchToPartner()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDarkMode) SushiRedLight else SushiRed),
                        border = BorderStroke(1.dp, SushiRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OutdoorGrill,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Abrir Modo Partner / Cocina",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Done Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = goldColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("settings_done_button")
                    ) {
                        Text("Cerrar y Volver al Menú", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsExpandableItem(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean,
    content: @Composable () -> Unit
) {
    val cardSecondaryBg = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorderSubtle
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary

    Surface(
        color = cardSecondaryBg,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isExpanded) goldColor else borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isExpanded) goldColor else textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        color = if (isExpanded) goldColor else textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = if (isExpanded) goldColor else IosTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(
                        color = borderColor,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    content()
                }
            }
        }
    }
}
