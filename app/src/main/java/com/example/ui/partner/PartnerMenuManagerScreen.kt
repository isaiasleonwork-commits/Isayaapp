package com.example.ui.partner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.RestaurantSettings
import com.example.model.SushiCategory
import com.example.model.SushiProduct
import com.example.ui.common.SushiImage
import com.example.ui.theme.*
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerMenuManagerScreen(
    settings: RestaurantSettings,
    menuItems: List<SushiProduct>,
    onToggleMasterStoreOpen: () -> Unit,
    onToggleItemAvailability: (String) -> Unit,
    onAddProduct: (SushiProduct) -> Unit,
    onUpdateProduct: (SushiProduct) -> Unit,
    onDeleteProduct: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<SushiCategory?>(null) }
    var editingProduct by remember { mutableStateOf<SushiProduct?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<SushiProduct?>(null) }

    val filteredItems = remember(menuItems, searchQuery, selectedCategoryFilter) {
        menuItems.filter { item ->
            val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CMS DE MENÚ & CATÁLOGO",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Gestor Dinámico de Menú",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { isAddingNewProduct = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_add_new_dish")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Nuevo Platillo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Master Switch: Store Open / Closed
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (settings.isOpen) StatusGreen.copy(alpha = 0.4f) else SushiRed.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (settings.isOpen) StatusGreenContainer else SushiRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = if (settings.isOpen) StatusGreen else SushiRedLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (settings.isOpen) "Tienda ABIERTA" else "Tienda CERRADA",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (settings.isOpen) StatusGreen else SushiRed)
                                )
                            }
                            Text(
                                text = if (settings.isOpen) "Aceptando pedidos activamente" else "Fuera de horario comercial",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = settings.isOpen,
                        onCheckedChange = { onToggleMasterStoreOpen() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBlack,
                            checkedTrackColor = StatusGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkCardElevated
                        ),
                        modifier = Modifier.testTag("master_store_open_switch")
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre, relleno o ingrediente...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(36.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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

        // Category Filter Row with 8dp spacing and horizontal scrolling
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("Todos (${menuItems.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = DeepBlack,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    )
                )

                SushiCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text("${cat.icon} ${cat.title}", fontSize = 11.sp) },
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

        // Items Summary count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrando ${filteredItems.size} platillos",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sincronización en tiempo real",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Clean Horizontal Product Cards: [Image 64x64px] | [Title & Description (flex-1)] | [Price + Edit Button]
        items(filteredItems, key = { it.id }) { product ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (product.isAvailable) DarkBorder else SushiRed.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cms_product_card_${product.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Left: Image 64x64px
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
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
                                    .background(DeepBlack.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Agotado",
                                    color = SushiRedLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 2. Center: Title & Description (flex-1 / weight(1f))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = product.name,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Surface(
                                color = DarkCardElevated,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = product.category.title,
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Description max 2 lines with ellipsis (line-clamp-2)
                        Text(
                            text = product.description.ifBlank { "Relleno e ingredientes frescos Isaya Sushi." },
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 3. Right: Price & Compact Edit Button
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", product.price)}",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )

                        // Compact secondary edit button (clean pencil icon + text)
                        Button(
                            onClick = { editingProduct = product },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("btn_edit_product_${product.id}"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkCardElevated,
                                contentColor = GoldPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal: Add New Product
    if (isAddingNewProduct) {
        ProductFormDialog(
            initialProduct = null,
            onDismiss = { isAddingNewProduct = false },
            onSave = { newProd ->
                onAddProduct(newProd)
                isAddingNewProduct = false
            }
        )
    }

    // Modal: Edit Product
    editingProduct?.let { prod ->
        ProductFormDialog(
            initialProduct = prod,
            onDismiss = { editingProduct = null },
            onSave = { updatedProd ->
                onUpdateProduct(updatedProd)
                editingProduct = null
            }
        )
    }

    // Confirmation Alert: Delete Product
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Eliminar Platillo",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar permanentemente \"${prod.name}\" del catálogo del restaurante?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(prod.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SushiRed, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { productToDelete = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * MODAL DE EDICIÓN DE PLATILLO (CMS DE MENÚ)
 * Incluye aclaración y personalización de:
 * 1. Nombre y Precio ($ USD)
 * 2. Categoría y Rellenos/Ingredientes
 * 3. Banner / Etiqueta Promocional (Ticket/Insignia: texto y color personalizado)
 * 4. Fotografía real del platillo (subida de archivo o presets)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    initialProduct: SushiProduct?,
    onDismiss: () -> Unit,
    onSave: (SushiProduct) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.let { String.format(Locale.US, "%.2f", it) } ?: "12.00") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: SushiCategory.COMBOS) }
    var description by remember {
        mutableStateOf(
            initialProduct?.description
                ?: "Relleno Isaya: pollo crujiente, camarón tempura, plátano frito, queso crema y aguacate."
        )
    }

    // Banner / Ticket Promocional
    var badgeText by remember { mutableStateOf(initialProduct?.popularBadge ?: "") }
    var badgeColorHex by remember { mutableStateOf(initialProduct?.badgeColorHex ?: "#D4AF37") }

    // Imagen / Fotografía
    var selectedImageRes by remember {
        mutableStateOf(
            initialProduct?.imageRes ?: R.drawable.img_sushi_hero
        )
    }
    var customImageUri by remember { mutableStateOf(initialProduct?.customImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Preset badges
    val presetBadges = remember {
        listOf(
            "Promo del Día 🌟",
            "Más Vendido 🔥",
            "Nuevo 🍣",
            "2x1 Especial ✨",
            "Super Oferta ⚡",
            "Crujiente 🍤"
        )
    }

    // Badge color palette
    val badgeColors = remember {
        listOf(
            "#D4AF37" to "Dorado",
            "#C8102E" to "Rojo",
            "#10B981" to "Verde",
            "#1E90FF" to "Azul",
            "#8E24AA" to "Morado",
            "#FB8C00" to "Naranja"
        )
    }

    // Banner image presets for easy 1-touch visual selection
    val availableBanners = remember {
        listOf(
            R.drawable.img_sushi_hero to "Sushi Hero / Combo",
            R.drawable.img_rolls_tempura to "Roll Tempurizado",
            R.drawable.img_rolls_cold to "Roll Frío / Salmón",
            R.drawable.img_glup_soda to "Bebida / Soda"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                        Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (initialProduct == null) "Nuevo Platillo" else "Editar Platillo",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
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

                // Error Message if any
                errorMessage?.let { err ->
                    Surface(
                        color = SushiRedContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = SushiRedLight,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // ==========================================
                // ROW 1: NOMBRE (Col 1) | PRECIO $ USD (Col 2)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Col 1: Nombre
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "1. Nombre del Platillo *",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; errorMessage = null },
                            placeholder = { Text("Ej. Combo Isaya 20 pzs", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
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

                    // Col 2: Precio ($ USD)
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(
                            text = "2. Precio ($ USD) *",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it; errorMessage = null },
                            placeholder = { Text("12.00", color = TextMuted, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
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
                }

                // Categoría
                Column {
                    Text(
                        text = "Categoría del Menú",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SushiCategory.entries.forEach { cat ->
                            val isSel = selectedCategory == cat
                            Surface(
                                color = if (isSel) GoldPrimary else DarkCard,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) GoldPrimary else DarkBorder
                                ),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = "${cat.icon} ${cat.title}",
                                    color = if (isSel) DeepBlack else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Rellenos e Ingredientes
                Column {
                    Text(
                        text = "3. Rellenos / Ingredientes *",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it; errorMessage = null },
                        placeholder = { Text("Pollo crujiente, camarón tempura, plátano frito, queso crema...", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 3
                    )
                }

                // =========================================================================
                // SECCIÓN: BANNER / ETIQUETA PROMOCIONAL (Ticket / Insignia de Producto)
                // =========================================================================
                Surface(
                    color = DarkCardElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🎫", fontSize = 13.sp)
                            Column {
                                Text(
                                    text = "Banner / Etiqueta Promocional",
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Insignia o ticket que resalta sobre la tarjeta del platillo",
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Input de texto del ticket
                        OutlinedTextField(
                            value = badgeText,
                            onValueChange = { badgeText = it },
                            placeholder = { Text("Ej. Promo del Día 🌟, Más Vendido 🔥, Nuevo 🍣", color = TextMuted, fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
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

                        // Chips de sugerencias rápidas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            presetBadges.forEach { preset ->
                                Surface(
                                    color = if (badgeText == preset) GoldContainer else DarkCard,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        if (badgeText == preset) GoldPrimary else DarkBorder
                                    ),
                                    modifier = Modifier.clickable { badgeText = preset }
                                ) {
                                    Text(
                                        text = preset,
                                        color = if (badgeText == preset) GoldLight else TextSecondary,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (badgeText.isNotBlank()) {
                                Surface(
                                    color = SushiRedContainer,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.clickable { badgeText = "" }
                                ) {
                                    Text(
                                        text = "✕ Quitar",
                                        color = SushiRedLight,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Selector de color del Ticket / Insignia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Color del Ticket:",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            badgeColors.forEach { (hex, label) ->
                                val isSelected = badgeColorHex.equals(hex, ignoreCase = true)
                                val colorObj = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { GoldPrimary }
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(colorObj)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.5.dp,
                                            color = if (isSelected) Color.White else DarkBorder,
                                            shape = CircleShape
                                        )
                                        .clickable { badgeColorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        // Live Preview Box
                        if (badgeText.isNotBlank()) {
                            val colorObj = try { Color(android.graphics.Color.parseColor(badgeColorHex)) } catch (e: Exception) { GoldPrimary }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("Vista previa:", color = TextMuted, fontSize = 9.sp)
                                Surface(
                                    color = colorObj.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorObj.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = colorObj,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // SECCIÓN: FOTOGRAFÍA / IMAGEN DEL PLATILLO
                // =========================================================================
                Surface(
                    color = DarkCardElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📷 Fotografía del Platillo",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current Active Image Preview
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.5.dp, GoldPrimary, RoundedCornerShape(8.dp))
                            ) {
                                SushiImage(
                                    fallbackRes = selectedImageRes,
                                    customUri = customImageUri,
                                    contentDescription = "Foto Platillo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack)
                                ) {
                                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Subir Foto Real", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                if (!customImageUri.isNullOrBlank()) {
                                    Text(
                                        text = "✓ Foto personalizada activa",
                                        color = StatusGreen,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        // Presets Galería
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableBanners.forEach { (resId, label) ->
                                val isSelected = selectedImageRes == resId && customImageUri == null
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.5.dp,
                                            color = if (isSelected) GoldPrimary else DarkBorder,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            selectedImageRes = resId
                                            customImageUri = null
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = label,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer Action Buttons (Horizontal Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "El nombre del platillo es obligatorio."
                                return@Button
                            }
                            val parsedPrice = priceText.toDoubleOrNull()
                            if (parsedPrice == null || parsedPrice <= 0.0) {
                                errorMessage = "Ingresa un precio válido en USD."
                                return@Button
                            }

                            val product = (initialProduct ?: SushiProduct(
                                id = "prod_" + UUID.randomUUID().toString().take(8),
                                name = name.trim(),
                                category = selectedCategory,
                                description = description.trim(),
                                price = parsedPrice,
                                imageRes = selectedImageRes,
                                isAvailable = true
                            )).copy(
                                name = name.trim(),
                                price = parsedPrice,
                                category = selectedCategory,
                                description = description.trim(),
                                imageRes = selectedImageRes,
                                customImageUri = customImageUri,
                                popularBadge = badgeText.trim().ifBlank { null },
                                badgeColorHex = if (badgeText.isNotBlank()) badgeColorHex else null
                            )

                            onSave(product)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DeepBlack),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


