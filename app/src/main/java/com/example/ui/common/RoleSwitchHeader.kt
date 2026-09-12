package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class AppRole {
    CLIENTE,
    PARTNER_ADMIN
}

@Composable
fun RoleSwitchHeader(
    currentRole: AppRole,
    onRoleChange: (AppRole) -> Unit,
    pendingOrdersCount: Int,
    isStoreOpen: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = IosLightSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, IosLightBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live sync & store badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isStoreOpen) StatusGreen else IosRedPrimary)
                    )
                    Text(
                        text = if (isStoreOpen) "Sincronizado • Abierto" else "Sincronizado • Cerrado",
                        color = if (isStoreOpen) StatusGreen else IosRedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Cloud / Real-time badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sincronización en tiempo real",
                        tint = IosGoldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Firestore Live Sync",
                        color = IosGoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Role Toggle Buttons Segmented Control with 24dp rounded corners
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(IosLightCardSecondary)
                    .border(1.dp, IosLightBorderSubtle, RoundedCornerShape(24.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Cliente Tab
                val isClientSelected = currentRole == AppRole.CLIENTE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isClientSelected) IosGoldPrimary else Color.Transparent)
                        .clickable { onRoleChange(AppRole.CLIENTE) }
                        .padding(vertical = 9.dp)
                        .testTag("role_client_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = "Cliente",
                            tint = if (isClientSelected) Color.White else IosTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Vista Cliente",
                            fontSize = 13.sp,
                            fontWeight = if (isClientSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isClientSelected) Color.White else IosTextSecondary
                        )
                    }
                }

                // Partner Tab
                val isPartnerSelected = currentRole == AppRole.PARTNER_ADMIN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isPartnerSelected) IosRedPrimary else Color.Transparent)
                        .clickable { onRoleChange(AppRole.PARTNER_ADMIN) }
                        .padding(vertical = 9.dp)
                        .testTag("role_partner_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Partner Admin",
                            tint = if (isPartnerSelected) Color.White else IosTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "App Partner",
                            fontSize = 13.sp,
                            fontWeight = if (isPartnerSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPartnerSelected) Color.White else IosTextSecondary
                        )

                        if (pendingOrdersCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isPartnerSelected) Color.White else IosRedPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$pendingOrdersCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isPartnerSelected) IosRedPrimary else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

