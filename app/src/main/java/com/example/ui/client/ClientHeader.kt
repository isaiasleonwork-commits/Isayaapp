package com.example.ui.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.R
import com.example.model.BrandIdentity
import com.example.model.DeliveryMode
import com.example.ui.common.SushiImage
import com.example.ui.theme.*
import com.example.util.SavedUserLocation

@Composable
fun ClientHeader(
    selectedMode: DeliveryMode,
    onModeChange: (DeliveryMode) -> Unit,
    isOpen: Boolean,
    savedLocation: SavedUserLocation,
    onOpenLocationPicker: () -> Unit,
    modifier: Modifier = Modifier,
    brandIdentity: BrandIdentity = BrandIdentity(),
    isCardsExpanded: Boolean = true,
    isDarkMode: Boolean = false
) {
    val headerBg = if (isDarkMode) DarkSurface else IosLightSurface
    val cardSecondaryBg = if (isDarkMode) DarkCardElevated else IosLightCardSecondary
    val cardActiveBg = if (isDarkMode) DarkCard else IosLightSurface
    val borderColor = if (isDarkMode) DarkBorder else IosLightBorder
    val textPrimary = if (isDarkMode) TextPrimary else IosTextPrimary
    val textSecondary = if (isDarkMode) TextSecondary else IosTextSecondary
    val textMuted = if (isDarkMode) TextMuted else IosTextMuted
    val goldColor = if (isDarkMode) GoldPrimary else IosGoldPrimary
    val goldContainer = if (isDarkMode) GoldContainer else IosGoldContainer
    val goldBorder = if (isDarkMode) GoldDark else IosGoldBorder

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(headerBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Branding Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, goldColor, CircleShape)
                ) {
                    SushiImage(
                        fallbackRes = brandIdentity.logoRes,
                        customUri = brandIdentity.logoUri,
                        contentDescription = "${brandIdentity.brandName} Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ISAYA",
                            color = goldColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SUSHI",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = brandIdentity.officialSlogan,
                        color = textSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            // Real-Time Open/Closed status chip
            Surface(
                color = if (isOpen) (if (isDarkMode) StatusGreenContainer else IosGreenContainer) else (if (isDarkMode) SushiRedContainer else IosRedContainer),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOpen) (if (isDarkMode) StatusGreen else IosGreenBorder) else (if (isDarkMode) SushiRed else IosRedBorder)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isOpen) (if (isDarkMode) StatusGreen else IosGreenPrimary) else (if (isDarkMode) SushiRed else IosRedPrimary))
                    )
                    Text(
                        text = if (isOpen) "ABIERTO" else "CERRADO",
                        color = if (isOpen) (if (isDarkMode) StatusGreen else IosGreenPrimary) else (if (isDarkMode) SushiRedLight else IosRedPrimary),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Collapsible Delivery and GPS cards on scroll down
        AnimatedVisibility(
            visible = isCardsExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                // Delivery vs Pick-Up Selector (iOS Segmented Style with 24dp rounded corners)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardSecondaryBg)
                        .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Delivery Option
                    val isDelivery = selectedMode == DeliveryMode.DELIVERY
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDelivery) cardActiveBg else Color.Transparent)
                            .border(
                                if (isDelivery) 1.dp else 0.dp,
                                if (isDelivery) (if (isDarkMode) goldColor else borderColor) else Color.Transparent,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { onModeChange(DeliveryMode.DELIVERY) }
                            .padding(vertical = 7.dp, horizontal = 8.dp)
                            .testTag("mode_delivery_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "Delivery",
                            tint = if (isDelivery) goldColor else textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Delivery GPS",
                                fontSize = 11.sp,
                                fontWeight = if (isDelivery) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDelivery) textPrimary else textSecondary
                            )
                            Text(
                                text = "30-45 min • $2.50",
                                fontSize = 9.sp,
                                color = if (isDelivery) textSecondary else textMuted
                            )
                        }
                    }

                    // Pick-Up Option
                    val isPickup = selectedMode == DeliveryMode.PICKUP
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isPickup) cardActiveBg else Color.Transparent)
                            .border(
                                if (isPickup) 1.dp else 0.dp,
                                if (isPickup) (if (isDarkMode) goldColor else borderColor) else Color.Transparent,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { onModeChange(DeliveryMode.PICKUP) }
                            .padding(vertical = 7.dp, horizontal = 8.dp)
                            .testTag("mode_pickup_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Pick-Up",
                            tint = if (isPickup) goldColor else textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Pick-Up (Retiro)",
                                fontSize = 11.sp,
                                fontWeight = if (isPickup) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPickup) textPrimary else textSecondary
                            )
                            Text(
                                text = "15-20 min • Gratis",
                                fontSize = 9.sp,
                                color = if (isPickup) textSecondary else textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive GPS Location Bar with 24dp shape
                Surface(
                    color = goldContainer,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, goldBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLocationPicker() }
                        .testTag("header_location_picker_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (selectedMode == DeliveryMode.DELIVERY) Icons.Default.LocationOn else Icons.Default.Storefront,
                                contentDescription = "Ubicación",
                                tint = goldColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = if (selectedMode == DeliveryMode.DELIVERY) "Entregar en:" else "Retiro en sede:",
                                    color = goldColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedMode == DeliveryMode.DELIVERY) {
                                        savedLocation.address
                                    } else {
                                        "Sede Isaya Sushi, Av. Las Delicias, La Soledad, Maracay"
                                    },
                                    color = textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            color = goldColor,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Mapa GPS",
                                    color = Color.White,
                                    fontSize = 9.sp,
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


