package com.example.model

import androidx.annotation.DrawableRes
import com.example.R
import java.util.UUID

enum class SushiCategory(val title: String, val icon: String) {
    COMBOS("Combos Especiales", "🍱"),
    ROLLS_TEMPURA("Rollos Tempura", "🍤"),
    ROLLS_FRIOS("Rollos Fríos", "🍣"),
    BEBIDAS("Bebidas", "🥤"),
    ENTRADAS("Entradas", "🥟")
}

data class ExtraAddon(
    val id: String,
    val name: String,
    val price: Double
)

data class BrandIdentity(
    val brandName: String = "ISAYA SUSHI",
    val officialSlogan: String = "Japanese Art & Nikkei Fusion",
    val logoUri: String? = null,
    @DrawableRes val logoRes: Int = R.drawable.img_app_icon,
    val brandTagline: String = "100% Tempurizado y Cocinado",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class SushiProduct(
    val id: String,
    val name: String,
    val category: SushiCategory,
    val description: String,
    val price: Double,
    @DrawableRes val imageRes: Int,
    val customImageUri: String? = null,
    val pieces: Int? = null,
    val isAvailable: Boolean = true,
    val popularBadge: String? = null,
    val badgeColorHex: String? = null,
    val isSpicy: Boolean = false,
    val isCrispy: Boolean = true,
    val isComboOfTheDay: Boolean = false,
    val isFeaturedAd: Boolean = false,
    val originalPrice: Double? = null
)

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val product: SushiProduct,
    val quantity: Int,
    val includedSauces: List<String> = emptyList(),
    val selectedExtras: List<ExtraAddon> = emptyList(),
    val specialNotes: String = "",
    val unitPrice: Double = product.price,
    val preparationStyle: String = "Tempurizado Caliente"
) {
    val totalPrice: Double
        get() {
            val extrasCost = selectedExtras.sumOf { it.price }
            return (unitPrice + extrasCost) * quantity
        }
}

enum class DeliveryMode(val label: String, val fee: Double, val icon: String) {
    DELIVERY("Envío a Domicilio", 2.50, "🛵"),
    PICKUP("Retiro en Local", 0.00, "🛍️")
}

enum class OrderStatus(val step: Int, val label: String, val description: String) {
    PENDING_PAYMENT(1, "Pedido Recibido", "Verificando comprobante de Pago Móvil"),
    PAYMENT_CONFIRMED(2, "Pago Confirmado", "Pago verificado exitosamente"),
    IN_KITCHEN(3, "En Cocina", "El equipo de cocina prepara tu orden"),
    DISPATCHED_OR_READY(4, "En Camino / Listo para Retirar", "Repartidor en camino o listo para recoger"),
    DELIVERED(5, "Entregado con Éxito", "Pedido entregado y completado")
}

data class DeliveryZone(
    val name: String,
    val fee: Double
)

val MARACAY_DELIVERY_ZONES: List<DeliveryZone> = listOf(
    // $1.00 USD (9 zonas)
    DeliveryZone("13 de Enero", 1.00),
    DeliveryZone("Araguama Country", 1.00),
    DeliveryZone("Campo Alegre", 1.00),
    DeliveryZone("Casanova Godoy", 1.00),
    DeliveryZone("La Mulera", 1.00),
    DeliveryZone("Los Cocos", 1.00),
    DeliveryZone("Los Samanes", 1.00),
    DeliveryZone("Madre María", 1.00),
    DeliveryZone("San Rafael", 1.00),

    // $2.00 USD (25 zonas)
    DeliveryZone("23 de Enero", 2.00),
    DeliveryZone("CC Galería Plaza", 2.00),
    DeliveryZone("CC Paseo Estación Central", 2.00),
    DeliveryZone("CC Unicentro", 2.00),
    DeliveryZone("Centro", 2.00),
    DeliveryZone("Cuartel Páez", 2.00),
    DeliveryZone("Francisco de Miranda", 2.00),
    DeliveryZone("Guaruto", 2.00),
    DeliveryZone("Guasimal", 2.00),
    DeliveryZone("Hiper Jumbo", 2.00),
    DeliveryZone("José Félix Ribas", 2.00),
    DeliveryZone("La Barraca", 2.00),
    DeliveryZone("Los Olivos", 2.00),
    DeliveryZone("Luxor", 2.00),
    DeliveryZone("Maracay Plaza", 2.00),
    DeliveryZone("Museo Aeronáutico", 2.00),
    DeliveryZone("Parque Aragua", 2.00),
    DeliveryZone("Piñonal", 2.00),
    DeliveryZone("Plaza Bolívar", 2.00),
    DeliveryZone("Río Blanco", 2.00),
    DeliveryZone("San Agustín", 2.00),
    DeliveryZone("San Ignacio", 2.00),
    DeliveryZone("San José", 2.00),
    DeliveryZone("San Luis", 2.00),
    DeliveryZone("Terminal de Maracay", 2.00),

    // $3.00 USD (25 zonas)
    DeliveryZone("Arsenal", 3.00),
    DeliveryZone("Andrés Bello", 3.00),
    DeliveryZone("Aviación", 3.00),
    DeliveryZone("Base Aragua", 3.00),
    DeliveryZone("Base Sucre", 3.00),
    DeliveryZone("Calicanto", 3.00),
    DeliveryZone("Candelaria", 3.00),
    DeliveryZone("Caña de Azúcar", 3.00),
    DeliveryZone("CC Las Américas", 3.00),
    DeliveryZone("CC Los Aviadores", 3.00),
    DeliveryZone("Coropo 2000", 3.00),
    DeliveryZone("El Toro", 3.00),
    DeliveryZone("Hospital Central", 3.00),
    DeliveryZone("La Cooperativa", 3.00),
    DeliveryZone("La Coromoto", 3.00),
    DeliveryZone("La Floresta", 3.00),
    DeliveryZone("La Pedrera", 3.00),
    DeliveryZone("La Romana", 3.00),
    DeliveryZone("La Soledad", 3.00),
    DeliveryZone("Las Delicias", 3.00),
    DeliveryZone("Montaña Fresca", 3.00),
    DeliveryZone("Morita", 3.00),
    DeliveryZone("Paraparal", 3.00),
    DeliveryZone("San Jacinto", 3.00),
    DeliveryZone("Santa Rita", 3.00),

    // $4.00 USD (10 zonas)
    DeliveryZone("Camburito", 4.00),
    DeliveryZone("Cantarrana", 4.00),
    DeliveryZone("El Castaño", 4.00),
    DeliveryZone("El Limón", 4.00),
    DeliveryZone("Encrucijada de Turmero", 4.00),
    DeliveryZone("Fundacoropo", 4.00),
    DeliveryZone("La Ovallera", 4.00),
    DeliveryZone("Los Hornos", 4.00),
    DeliveryZone("Samán de Güere", 4.00),
    DeliveryZone("Residencias Palo Negro", 4.00),

    // $5.00 USD (2 zonas)
    DeliveryZone("Cagua", 5.00),
    DeliveryZone("Palo Negro", 5.00),

    // $6.00 USD (2 zonas)
    DeliveryZone("Cagua Centro", 6.00),
    DeliveryZone("La Paya", 6.00)
)

data class CustomerInfo(
    val fullName: String = "",
    val whatsappPhone: String = "",
    val deliveryAddress: String = "",
    val referencePoint: String = "",
    val deliveryZone: DeliveryZone? = null,
    val gpsCoordinates: String = "10.2469° N, 67.5958° O"
)

data class PaymentProof(
    val bankName: String = "0105 Mercantil",
    val rif: String = "17016897",
    val phone: String = "04220034452",
    val accountHolder: String = "Isaya Sushi Gourmet C.A.",
    val referenceDigits: String = "",
    val receiptAttached: Boolean = true,
    val receiptFileName: String = "pago_movil_capture.jpg",
    val receiptImageUrl: String? = null,
    @DrawableRes val receiptImageRes: Int = com.example.R.drawable.img_pago_movil_capture
)

data class Order(
    val id: String,
    val orderNumber: String,
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<CartItem>,
    val deliveryMode: DeliveryMode,
    val customerInfo: CustomerInfo,
    val paymentProof: PaymentProof,
    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val assignedDriver: String? = null
) {
    val grossSale: Double get() = total
    val netSale: Double get() = (subtotal * 0.65).coerceAtLeast(0.0)
}

data class DeliveryRiderProfile(
    val id: String,
    val name: String,
    val phone: String,
    val vehicle: String = "Moto Bera 150cc",
    val status: String = "Activo en Ruta",
    val completedDeliveriesCount: Int = 0,
    val totalCollectedDeliveryFees: Double = 0.0
)

enum class AlertTone(val title: String, val description: String, val icon: String) {
    CAMPANA("Campana Acústica", "Tono de campana suave y claro", "🔔"),
    ALERTA_FUERTE("Alerta Fuerte", "Sirena y doble tono de alta prioridad", "🚨"),
    CHIME_IOS("Chime iOS", "Tríada armónica estilo iOS Apple", "✨")
}

data class RestaurantSettings(
    val isOpen: Boolean = true,
    val deliveryFee: Double = 2.50,
    val openingHoursNotice: String = "12:00 PM - 11:00 PM",
    val restaurantPhone: String = "584141234567",
    val selectedAlertTone: AlertTone = AlertTone.CAMPANA,
    val alertVolume: Float = 0.85f,
    val vibrationEnabled: Boolean = true
)

data class PushNotificationMessage(
    val id: String,
    val title: String,
    val body: String,
    val promoTag: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class PartnerStaffUser(
    val uid: String,
    val email: String,
    val displayName: String = "Chef de Cocina Isaya",
    val role: String = "Control de Comandas & Cocina"
)
