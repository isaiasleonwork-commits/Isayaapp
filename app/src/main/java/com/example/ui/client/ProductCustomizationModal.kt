package com.example.ui.client

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.ExtraAddon
import com.example.model.SushiProduct
import com.example.ui.theme.*
import java.util.Locale

val DefaultIncludedSauces = listOf(
    "Salsa Teriyaki Dulce",
    "Salsa de Soya",
    "Salsa Fuji Especial"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductCustomizationModal(
    product: SushiProduct,
    onDismiss: () -> Unit,
    onAddToCart: (CartItem) -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    var quantity by remember { mutableIntStateOf(1) }

    // Dynamic Theme Color Tokens
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
    val redContainer = if (isDarkMode) SushiRedContainer else IosRedContainer
    val redBorder = if (isDarkMode) SushiRed else IosRedBorder
    
    // Preparación sin costo (Tempurizado caliente vs Frío)
    var preparationStyle by remember {
        mutableStateOf(if (product.id == "prod_combo_chic") "Frío (Sin Freír)" else "Tempurizado Caliente")
    }

    // Salsas incluidas sin costo
    val selectedSauces = remember {
        mutableStateMapOf<String, Boolean>().apply {
            DefaultIncludedSauces.forEach { this[it] = true }
        }
    }

    // Toppings & Extras oficiales con contadores (+ / -)
    var isExtrasExpanded by remember { mutableStateOf(false) }
    var qtyShrimp by remember { mutableIntStateOf(0) } // $3.00 c/u
    var qtyChicken by remember { mutableIntStateOf(0) } // $2.00 c/u
    var qtyDinamitaFria by remember { mutableIntStateOf(0) } // $2.00 c/u
    var qtyDinamitaTempura by remember { mutableIntStateOf(0) } // $2.00 c/u
    var qtyGlupSoda by remember { mutableIntStateOf(0) } // $1.50 c/u
    var qtyExtraTeriyaki by remember { mutableIntStateOf(0) } // $0.50 c/u
    var qtyExtraSoya by remember { mutableIntStateOf(0) } // $0.50 c/u
    var qtyExtraFuji by remember { mutableIntStateOf(0) } // $0.75 c/u

    val totalExtrasCount = qtyShrimp + qtyChicken + qtyDinamitaFria + qtyDinamitaTempura + qtyGlupSoda + qtyExtraTeriyaki + qtyExtraSoya + qtyExtraFuji

    // Bandeja de texto para notas e instrucciones especiales
    var specialNotes by remember { mutableStateOf("") }

    // Cálculo dinámico de ensalada dinamita (si elige 1 de cada una aplica promo de $3 en vez de $4)
    val dinamitaCost = if (qtyDinamitaFria >= 1 && qtyDinamitaTempura >= 1) {
        val pairs = minOf(qtyDinamitaFria, qtyDinamitaTempura)
        val remainingFria = qtyDinamitaFria - pairs
        val remainingTempura = qtyDinamitaTempura - pairs
        (pairs * 3.00) + (remainingFria * 2.00) + (remainingTempura * 2.00)
    } else {
        (qtyDinamitaFria * 2.00) + (qtyDinamitaTempura * 2.00)
    }

    val extrasCost = (qtyShrimp * 3.00) +
            (qtyChicken * 2.00) +
            dinamitaCost +
            (qtyGlupSoda * 1.50) +
            (qtyExtraTeriyaki * 0.50) +
            (qtyExtraSoya * 0.50) +
            (qtyExtraFuji * 0.75)

    val itemTotal = (product.price + extrasCost) * quantity

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
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with image and product title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    product.popularBadge?.let { badge ->
                        Surface(
                            color = redContainer,
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, redBorder)
                        ) {
                            Text(
                                text = badge,
                                color = redColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = product.name,
                        color = textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    product.pieces?.let {
                        Text(
                            text = "$it piezas",
                            color = goldColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", product.price)} USD",
                            color = goldColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        product.originalPrice?.let { orig ->
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", orig)} USD",
                                color = textMuted,
                                fontSize = 13.sp,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = product.imageRes),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                text = product.description,
                color = textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 12.dp))

            // Selector de Estilo de Preparación (Tempurizado vs Frío)
            Text(
                text = "Estilo de Preparación (Sin Costo):",
                color = goldColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isHot = preparationStyle == "Tempurizado Caliente"
                Surface(
                    color = if (isHot) goldContainer else cardSecondaryBg,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isHot) goldColor else borderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { preparationStyle = "Tempurizado Caliente" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RadioButton(
                            selected = isHot,
                            onClick = { preparationStyle = "Tempurizado Caliente" },
                            colors = RadioButtonDefaults.colors(selectedColor = goldColor)
                        )
                        Column {
                            Text(
                                text = "Tempurizado 🔥",
                                color = if (isHot) goldColor else textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Crujiente • $0.00",
                                color = textMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                val isCold = preparationStyle == "Frío (Sin Freír)"
                Surface(
                    color = if (isCold) goldContainer else cardSecondaryBg,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isCold) goldColor else borderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { preparationStyle = "Frío (Sin Freír)" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RadioButton(
                            selected = isCold,
                            onClick = { preparationStyle = "Frío (Sin Freír)" },
                            colors = RadioButtonDefaults.colors(selectedColor = goldColor)
                        )
                        Column {
                            Text(
                                text = "Opción Frío ❄️",
                                color = if (isCold) goldColor else textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sin freír • $0.00",
                                color = textMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 12.dp))

            // SECCIÓN: BOTÓN DESPLEGABLE PARA EXTRAS / COMPLEMENTOS
            Surface(
                color = if (totalExtrasCount > 0) goldContainer else cardSecondaryBg,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    if (totalExtrasCount > 0) goldColor else borderSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { isExtrasExpanded = !isExtrasExpanded }
                    .testTag("toggle_extras_dropdown_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (totalExtrasCount > 0) goldColor else goldContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isExtrasExpanded) Icons.Default.ExpandLess else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (totalExtrasCount > 0) Color.White else goldColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isExtrasExpanded) "Ocultar complementos ⬆" else "Añadir complementos ➔",
                                color = goldColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (totalExtrasCount > 0) {
                                    "$totalExtrasCount seleccionado${if (totalExtrasCount > 1) "s" else ""} (+$${String.format(Locale.US, "%.2f", extrasCost)} USD)"
                                } else {
                                    "Camarones, pollo, ensaladas, bebidas y salsas"
                                },
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExtrasExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Lista desplegada de aditamentos con animación fluida
            AnimatedVisibility(
                visible = isExtrasExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Camarones Extra
                    ExtraStepperRow(
                        title = "Camarones Extra Apanados 🍤",
                        price = 3.00,
                        quantity = qtyShrimp,
                        onIncrease = { if (qtyShrimp < 5) qtyShrimp++ },
                        onDecrease = { if (qtyShrimp > 0) qtyShrimp-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyShrimp > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )

                    // 2. Pollo Extra Crunchy
                    ExtraStepperRow(
                        title = "Pollo Extra Crunchy 🍗",
                        price = 2.00,
                        quantity = qtyChicken,
                        onIncrease = { if (qtyChicken < 5) qtyChicken++ },
                        onDecrease = { if (qtyChicken > 0) qtyChicken-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyChicken > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )

                    // 3. Ensalada Dinamita Fría
                    ExtraStepperRow(
                        title = "Ensalada Dinamita Fría 🥗",
                        price = 2.00,
                        subtitle = if (qtyDinamitaFria > 0 && qtyDinamitaTempura > 0) "¡Promo Dúo $3 aplicada!" else "$2 c/u o $3 las dos",
                        quantity = qtyDinamitaFria,
                        onIncrease = { if (qtyDinamitaFria < 5) qtyDinamitaFria++ },
                        onDecrease = { if (qtyDinamitaFria > 0) qtyDinamitaFria-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyDinamitaFria > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )

                    // 4. Ensalada Dinamita Tempura
                    ExtraStepperRow(
                        title = "Ensalada Dinamita Tempura 💥",
                        price = 2.00,
                        subtitle = if (qtyDinamitaFria > 0 && qtyDinamitaTempura > 0) "¡Promo Dúo $3 aplicada!" else "$2 c/u o $3 las dos",
                        quantity = qtyDinamitaTempura,
                        onIncrease = { if (qtyDinamitaTempura < 5) qtyDinamitaTempura++ },
                        onDecrease = { if (qtyDinamitaTempura > 0) qtyDinamitaTempura-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyDinamitaTempura > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )

                    // 5. Bebida Glup 1 Litro
                    ExtraStepperRow(
                        title = "Bebida Glup Negro 1 Litro 🥤",
                        subtitle = "Bien fría",
                        price = 1.50,
                        quantity = qtyGlupSoda,
                        onIncrease = { if (qtyGlupSoda < 5) qtyGlupSoda++ },
                        onDecrease = { if (qtyGlupSoda > 0) qtyGlupSoda-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyGlupSoda > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )

                    // 6. Salsa Teriyaki Dulce Extra
                    ExtraStepperRow(
                        title = "Salsa Teriyaki Dulce Extra 🍯",
                        price = 0.50,
                        quantity = qtyExtraTeriyaki,
                        onIncrease = { if (qtyExtraTeriyaki < 5) qtyExtraTeriyaki++ },
                        onDecrease = { if (qtyExtraTeriyaki > 0) qtyExtraTeriyaki-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyExtraTeriyaki > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )

                    // 7. Salsa de Soya Extra
                    ExtraStepperRow(
                        title = "Salsa de Soya Extra 🥢",
                        price = 0.50,
                        quantity = qtyExtraSoya,
                        onIncrease = { if (qtyExtraSoya < 5) qtyExtraSoya++ },
                        onDecrease = { if (qtyExtraSoya > 0) qtyExtraSoya-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyExtraSoya > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )

                    // 8. Salsa Fuji Especial Extra
                    ExtraStepperRow(
                        title = "Salsa Fuji Especial Extra ✨",
                        price = 0.75,
                        quantity = qtyExtraFuji,
                        onIncrease = { if (qtyExtraFuji < 5) qtyExtraFuji++ },
                        onDecrease = { if (qtyExtraFuji > 0) qtyExtraFuji-- },
                        cardBg = cardSecondaryBg,
                        borderClr = if (qtyExtraFuji > 0) goldBorder else borderSubtle,
                        textClr = textPrimary,
                        goldClr = goldColor,
                        isDarkMode = isDarkMode
                    )
                }
            }

            HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 12.dp))

            // SECCIÓN: Salsas Incluidas (Checkboxes Sin Costo)
            Text(
                text = "Salsas Incluidas (Sin Costo)",
                color = goldColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Elige tus salsas de cortesía incluidas:",
                color = textSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            DefaultIncludedSauces.forEach { sauce ->
                val isChecked = selectedSauces[sauce] ?: false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { selectedSauces[sauce] = !isChecked }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { selectedSauces[sauce] = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = goldColor,
                                checkmarkColor = Color.White,
                                uncheckedColor = textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sauce,
                            color = textPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "Gratis",
                        color = goldColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = borderColor, modifier = Modifier.padding(vertical = 12.dp))

            // SECCIÓN: BANDEJA DE TEXTO PARA INSTRUCCIONES
            Text(
                text = "Notas para tu pedido",
                color = goldColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Notas para tu pedido (ej. sin cebollín, salsa aparte):",
                color = textSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Chips rápidos de sugerencia with 24dp rounded corners
            val suggestions = listOf(
                "Sin cebollín",
                "Salsa aparte",
                "Extra teriyaki dulce",
                "Extra salsa fuji",
                "Más palitos",
                "Sin plátano",
                "Bien crujiente"
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach { suggestion ->
                    val isSelected = specialNotes.contains(suggestion)
                    Surface(
                        color = if (isSelected) goldContainer else cardSecondaryBg,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) goldColor else borderSubtle
                        ),
                        modifier = Modifier.clickable {
                            if (isSelected) {
                                specialNotes = specialNotes.replace(suggestion, "").replace(", ,", ",").trim(',', ' ')
                            } else {
                                specialNotes = if (specialNotes.isBlank()) suggestion else "$specialNotes, $suggestion"
                            }
                        }
                    ) {
                        Text(
                            text = suggestion,
                            color = if (isSelected) goldColor else textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Bandeja de Texto estilizada estilo iOS with 24dp rounded corners
            OutlinedTextField(
                value = specialNotes,
                onValueChange = { specialNotes = it },
                placeholder = {
                    Text(
                        text = "Escribe aquí cualquier indicación especial para la cocina...",
                        color = textMuted,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .testTag("special_instructions_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = surfaceBg,
                    unfocusedContainerColor = surfaceBg,
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    cursorColor = goldColor
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Selector de Cantidad de Platillos and Add to Cart Button with 24dp rounded corners
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Stepper
                Surface(
                    color = cardSecondaryBg,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(34.dp),
                            enabled = quantity > 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Menos",
                                tint = if (quantity > 1) textPrimary else textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "$quantity",
                            color = goldColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = { if (quantity < 20) quantity++ },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Más",
                                tint = goldColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Add to Cart Button
                Button(
                    onClick = {
                        val activeExtras = mutableListOf<ExtraAddon>()
                        if (qtyShrimp > 0) activeExtras.add(ExtraAddon("shrimp", "Camarones Extra Apanados (x$qtyShrimp)", 3.00 * qtyShrimp))
                        if (qtyChicken > 0) activeExtras.add(ExtraAddon("chicken", "Pollo Extra Crunchy (x$qtyChicken)", 2.00 * qtyChicken))
                        if (qtyDinamitaFria > 0) activeExtras.add(ExtraAddon("dinamita_fria", "Ensalada Dinamita Fría (x$qtyDinamitaFria)", 2.00 * qtyDinamitaFria))
                        if (qtyDinamitaTempura > 0) activeExtras.add(ExtraAddon("dinamita_tempura", "Ensalada Dinamita Tempura (x$qtyDinamitaTempura)", 2.00 * qtyDinamitaTempura))
                        if (qtyGlupSoda > 0) activeExtras.add(ExtraAddon("glup_soda", "Bebida Glup Negro 1L (x$qtyGlupSoda)", 1.50 * qtyGlupSoda))
                        if (qtyExtraTeriyaki > 0) activeExtras.add(ExtraAddon("extra_teriyaki", "Salsa Teriyaki Dulce Extra (x$qtyExtraTeriyaki)", 0.50 * qtyExtraTeriyaki))
                        if (qtyExtraSoya > 0) activeExtras.add(ExtraAddon("extra_soya", "Salsa de Soya Extra (x$qtyExtraSoya)", 0.50 * qtyExtraSoya))
                        if (qtyExtraFuji > 0) activeExtras.add(ExtraAddon("extra_fuji", "Salsa Fuji Especial Extra (x$qtyExtraFuji)", 0.75 * qtyExtraFuji))

                        val includedSaucesList = selectedSauces.filter { it.value }.keys.toList()

                        val item = CartItem(
                            product = product,
                            quantity = quantity,
                            includedSauces = includedSaucesList,
                            selectedExtras = activeExtras,
                            specialNotes = specialNotes.trim(),
                            unitPrice = product.price,
                            preparationStyle = preparationStyle
                        )
                        onAddToCart(item)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("confirm_add_to_cart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = redColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Agregar ($quantity)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", itemTotal)} USD",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExtraStepperRow(
    title: String,
    price: Double,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    cardBg: Color,
    borderClr: Color,
    textClr: Color,
    goldClr: Color,
    isDarkMode: Boolean,
    subtitle: String? = null
) {
    Surface(
        color = cardBg,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderClr),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = textClr,
                    fontSize = 13.sp,
                    fontWeight = if (quantity > 0) FontWeight.Bold else FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "+$${String.format(Locale.US, "%.2f", price)} USD",
                        color = goldClr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    subtitle?.let {
                        Text(
                            text = "• $it",
                            color = textClr.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Simple (+ / -) stepper buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDarkMode) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f))
                    .border(1.dp, borderClr, RoundedCornerShape(24.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(28.dp),
                    enabled = quantity > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Disminuir",
                        tint = if (quantity > 0) textClr else textClr.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "$quantity",
                    color = if (quantity > 0) goldClr else textClr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = goldClr,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
