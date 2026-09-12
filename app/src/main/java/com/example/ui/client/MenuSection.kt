package com.example.ui.client

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SushiCategory
import com.example.model.SushiProduct
import com.example.ui.common.SushiImage
import com.example.ui.theme.*
import java.util.Locale

fun parseCustomBadgeColor(hex: String?, fallbackColor: Color): Color {
    if (hex.isNullOrBlank()) return fallbackColor
    return try {
        val clean = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(clean))
    } catch (e: Exception) {
        fallbackColor
    }
}

@Composable
fun MenuSection(
    menuItems: List<SushiProduct>,
    onProductClick: (SushiProduct) -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    var selectedCategory by remember { mutableStateOf<SushiCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Dynamic Theme Color Tokens
    val sectionBg = if (isDarkMode) DeepBlack else IosLightBg
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

    // Categories that actually have products in current menu
    val availableCategories = remember(menuItems) {
        menuItems.map { it.category }.distinct()
    }

    val filteredItems = remember(menuItems, selectedCategory, searchQuery) {
        menuItems.filter { item ->
            val matchesCategory = selectedCategory == null || item.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(sectionBg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp)
    ) {
        // Hero Promo Banner with 24dp rounded corners and gold accents
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(115.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, goldBorder, RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_sushi_hero),
                contentDescription = "Promo Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.88f),
                                Color.Black.copy(alpha = 0.40f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = redColor,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "100% TEMPURIZADO Y COCINADO",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Isaya Sushi Maracay",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Combos crujientes con nuestro relleno de la casa",
                    color = goldColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Search bar with 24dp rounded corners and clean iOS style
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar combos, rolls tempura, bebida...", color = textMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = goldColor,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("menu_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = surfaceBg,
                unfocusedContainerColor = surfaceBg,
                focusedBorderColor = goldColor,
                unfocusedBorderColor = borderColor,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                cursorColor = goldColor
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        // ANUNCIO DESTACADO ROTATIVO: Combo Sushi Fest ($12) <-> Tempura Roll ($6)
        FeaturedAdsRotator(
            menuItems = menuItems,
            onProductClick = onProductClick,
            isDarkMode = isDarkMode,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Navigable Category Filters (Scrollable Row with 24dp chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "Todos" Filter
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = {
                    Text(
                        "✨ Todos (${menuItems.size})",
                        fontSize = 12.sp,
                        fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = goldColor,
                    selectedLabelColor = Color.White,
                    containerColor = surfaceBg,
                    labelColor = textSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == null,
                    borderColor = borderColor,
                    selectedBorderColor = goldColor
                )
            )

            availableCategories.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategory = if (isSelected) null else category
                    },
                    label = {
                        Text(
                            "${category.icon} ${category.title}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = goldColor,
                        selectedLabelColor = Color.White,
                        containerColor = surfaceBg,
                        labelColor = textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = borderColor,
                        selectedBorderColor = goldColor
                    )
                )
            }
        }

        // Product Cards List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceBg),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "No se encontraron productos en esta categoría.",
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredItems.forEach { product ->
                    ProductCard(
                        product = product,
                        isDarkMode = isDarkMode,
                        onClick = {
                            if (product.isAvailable) onProductClick(product)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(120.dp)) // Padding for bottom floating cart and navigation bar
            }
        }
    }
}

/**
 * Anuncio rotativo interactivo que permite cambiar fácilmente entre
 * el "Combo Sushi Fest ($12)" y el "Tempura Roll ($6)".
 */
@Composable
fun FeaturedAdsRotator(
    menuItems: List<SushiProduct>,
    onProductClick: (SushiProduct) -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val isayaCombo = remember(menuItems) {
        menuItems.firstOrNull { it.id == "prod_combo_sushi_fest" }
            ?: menuItems.firstOrNull { it.isComboOfTheDay }
            ?: menuItems.firstOrNull { it.price == 12.0 }
    }

    val tempuraRoll = remember(menuItems) {
        menuItems.firstOrNull { it.id == "prod_tempura_roll" }
            ?: menuItems.firstOrNull { it.price == 6.0 }
    }

    val ads = remember(isayaCombo, tempuraRoll) {
        listOfNotNull(isayaCombo, tempuraRoll)
    }

    if (ads.isEmpty()) return

    var currentAdIndex by remember { mutableIntStateOf(0) }
    val activeProduct = ads.getOrNull(currentAdIndex % ads.size) ?: ads[0]

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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = goldBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable {
                if (activeProduct.isAvailable) onProductClick(activeProduct)
            }
            .testTag("featured_ad_rotator_card"),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Anuncio Header Banner Ribbon with Rotator Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardSecondaryBg)
                    .border(0.5.dp, borderColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
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
                        Surface(
                            color = goldColor,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "ANUNCIO ESPECIAL",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = if (activeProduct.id == "prod_combo_sushi_fest") "⭐ COMBO ISAYA" else "🍤 TEMPURA ROLL",
                            color = textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón de rotación manual de anuncio with 24dp rounded corners
                    Surface(
                        color = goldContainer,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder),
                        modifier = Modifier.clickable {
                            currentAdIndex = (currentAdIndex + 1) % ads.size
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = "Rotar anuncio",
                                tint = goldColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Cambiar 🔄",
                                color = goldColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tab switch selector directly visible on ad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ads.forEachIndexed { index, product ->
                    val isSelected = (currentAdIndex % ads.size) == index
                    Surface(
                        color = if (isSelected) goldColor else cardSecondaryBg,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) goldColor else borderSubtle
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentAdIndex = index }
                    ) {
                        Text(
                            text = if (product.id == "prod_combo_sushi_fest") "🍱 Combo Isaya ($12)" else "🍤 Tempura Roll ($6)",
                            color = if (isSelected) Color.White else textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Body: Animated content showing the selected ad
            AnimatedContent(
                targetState = activeProduct,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                },
                label = "ad_transition"
            ) { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sushi Image with 24dp Rounded Corners (keeps natural vibrant colors)
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                    ) {
                        SushiImage(
                            fallbackRes = product.imageRes,
                            customUri = product.customImageUri,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = redColor,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = if (product.price == 12.0) "33% OFF" else "OFERTA",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        product.pieces?.let { pcs ->
                            Surface(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                            ) {
                                Text(
                                    text = "$pcs pcs",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = product.name,
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = product.description,
                            color = textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 2
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", product.price)} USD",
                                color = goldColor,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            product.originalPrice?.let { orig ->
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", orig)} USD",
                                    color = textMuted,
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }

                        Button(
                            onClick = { onProductClick(product) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = redColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("featured_ad_action_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "¡Pedir Ahora ($${String.format(Locale.US, "%.0f", product.price)})! 🍱",
                                    fontSize = 11.sp,
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

@Composable
fun ProductCard(
    product: SushiProduct,
    onClick: () -> Unit,
    isDarkMode: Boolean = false
) {
    val surfaceBg = if (isDarkMode) DarkSurface else IosLightSurface
    val cardSecondaryBg = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorder
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val textSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val textMuted = if (isDarkMode) TextMuted else IosTextMuted
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldContainer = if (isDarkMode) GoldContainer else IosGoldContainer
    val goldBorder = if (isDarkMode) GoldDark else IosGoldBorder
    val redColor = if (isDarkMode) SushiRed else IosRedPrimary
    val redContainer = if (isDarkMode) SushiRedContainer else IosRedContainer
    val redBorder = if (isDarkMode) SushiRed else IosRedBorder

    val cardBorder = when {
        !product.isAvailable -> redBorder
        product.isComboOfTheDay -> goldBorder
        else -> borderColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                if (product.isComboOfTheDay) 1.5.dp else 1.dp,
                cardBorder,
                RoundedCornerShape(24.dp)
            )
            .clickable(enabled = product.isAvailable, onClick = onClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isAvailable) surfaceBg else cardSecondaryBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Content: Title, Ingredients, Price
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (product.isComboOfTheDay) {
                        Surface(
                            color = goldContainer,
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder)
                        ) {
                            Text(
                                text = "COMBO DEL DÍA ⭐",
                                color = goldColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    product.popularBadge?.let { badge ->
                        if (!product.isComboOfTheDay) {
                            val badgeBg = parseCustomBadgeColor(product.badgeColorHex, redColor)
                            Surface(
                                color = badgeBg.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, badgeBg.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = badge,
                                    color = badgeBg,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (product.isCrispy) {
                        Surface(
                            color = goldContainer,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Crunchy",
                                color = goldColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.name,
                    color = if (product.isAvailable) textPrimary else textMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = product.description,
                    color = textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", product.price)} USD",
                        color = if (product.isAvailable) goldColor else textMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    product.originalPrice?.let { orig ->
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", orig)} USD",
                            color = textMuted,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    product.pieces?.let {
                        Text(
                            text = "• $it pcs",
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Right Image + Add/Out of Stock Button with 24dp Rounded Corners (keeps natural image color)
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            ) {
                SushiImage(
                    fallbackRes = product.imageRes,
                    customUri = product.customImageUri,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (!product.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Agotado",
                                tint = redColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Agotado",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Plus button overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(redColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
