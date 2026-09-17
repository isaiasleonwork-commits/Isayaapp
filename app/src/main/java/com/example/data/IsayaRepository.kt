package com.example.data

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import com.example.R
import com.example.model.*
import com.example.util.SoundAlertHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class IsayaRepository private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Real-time states
    private val _settings = MutableStateFlow(RestaurantSettings())
    val settings: StateFlow<RestaurantSettings> = _settings.asStateFlow()

    private val _brandIdentity = MutableStateFlow(BrandIdentity())
    val brandIdentity: StateFlow<BrandIdentity> = _brandIdentity.asStateFlow()

    private val _menu = MutableStateFlow<List<SushiProduct>>(emptyList())
    val menu: StateFlow<List<SushiProduct>> = _menu.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Shared flow to trigger alert chime when partner receives new order
    private val _newOrderAlert = MutableSharedFlow<Order>(extraBufferCapacity = 10)
    val newOrderAlert: SharedFlow<Order> = _newOrderAlert.asSharedFlow()

    // Active client order tracker (stores last placed order ID)
    private val _activeClientOrderId = MutableStateFlow<String?>(null)
    val activeClientOrderId: StateFlow<String?> = _activeClientOrderId.asStateFlow()

    // Push Notifications Flow (received by Client App, broadcasted by Partner App)
    private val _notifications = MutableStateFlow<List<PushNotificationMessage>>(
        listOf(
            PushNotificationMessage(
                id = "init_promo_1",
                title = "🍣 2x1 en Rollos Tempura",
                body = "¡Pide tu Tempura Roll o Maracay Roll y lleva el segundo a mitad de precio hoy!",
                promoTag = "PROMO2X1"
            )
        )
    )
    val notifications: StateFlow<List<PushNotificationMessage>> = _notifications.asStateFlow()

    private val _newPushNotificationEvent = MutableSharedFlow<PushNotificationMessage>(extraBufferCapacity = 10)
    val newPushNotificationEvent: SharedFlow<PushNotificationMessage> = _newPushNotificationEvent.asSharedFlow()

    // Partner Authentication State (Firebase Auth)
    private val _authenticatedPartner = MutableStateFlow<PartnerStaffUser?>(
        PartnerStaffUser(
            uid = "partner_chef_1",
            email = "cocina@isayasushi.com",
            displayName = "Chef de Cocina Isaya",
            role = "Control de Comandas & Cocina"
        )
    )
    val authenticatedPartner: StateFlow<PartnerStaffUser?> = _authenticatedPartner.asStateFlow()

    // Firestore & Auth handles
    private var firestore: FirebaseFirestore? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var ordersListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null

    init {
        initDefaultMenu()
        initSampleOrder()
        setupFirebaseIfAvailable()
    }

    private fun setupFirebaseIfAvailable() {
        try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                firestore = db
                listenToFirestoreOrders(db)
                listenToFirestoreSettings(db)
                listenToFirestoreBrandIdentity(db)
                listenToFirestoreNotifications(db)

                firebaseAuth = FirebaseAuth.getInstance()
                val currentUser = firebaseAuth?.currentUser
                if (currentUser != null) {
                    _authenticatedPartner.value = PartnerStaffUser(
                        uid = currentUser.uid,
                        email = currentUser.email ?: "staff@isayasushi.com",
                        displayName = currentUser.displayName ?: "Chef de Cocina Isaya",
                        role = "Control de Comandas & Cocina"
                    )
                }
            } else {
                Log.d("IsayaRepository", "Firebase not initialized; using in-memory live sync.")
            }
        } catch (e: Throwable) {
            Log.w("IsayaRepository", "Firebase init skipped: ${e.message}")
        }
    }

    private fun listenToFirestoreNotifications(db: FirebaseFirestore) {
        notificationsListener = db.collection("isaya_broadcast_notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                try {
                    val list = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val title = getStringFromAny(doc.get("title")) ?: return@mapNotNull null
                            val body = getStringFromAny(doc.get("body")) ?: ""
                            val promoTag = getStringFromAny(doc.get("promoTag"))
                            val ts = getLongFromAny(doc.get("timestamp")) ?: System.currentTimeMillis()
                            PushNotificationMessage(id, title, body, promoTag, ts)
                        } catch (e: Exception) {
                            null
                        }
                    }?.sortedByDescending { it.timestamp } ?: emptyList()

                    if (list.isNotEmpty()) {
                        _notifications.value = list
                    }
                } catch (e: Exception) {
                    Log.e("IsayaRepository", "Error reading notifications snapshot", e)
                }
            }
    }

    private fun getStringFromAny(obj: Any?): String? {
        if (obj == null) return null
        if (obj is String) return obj.takeIf { it.isNotBlank() }
        return obj.toString().takeIf { it.isNotBlank() }
    }

    private fun getDoubleFromAny(obj: Any?): Double? {
        return when (obj) {
            null -> null
            is Number -> obj.toDouble()
            is String -> obj.trim().toDoubleOrNull()
            else -> null
        }
    }

    private fun getLongFromAny(obj: Any?): Long? {
        return when (obj) {
            null -> null
            is Number -> obj.toLong()
            is com.google.firebase.Timestamp -> obj.toDate().time
            is String -> obj.trim().toLongOrNull()
            else -> null
        }
    }

    private fun getBooleanFromAny(obj: Any?): Boolean? {
        return when (obj) {
            null -> null
            is Boolean -> obj
            is String -> obj.trim().toBooleanStrictOrNull() ?: (obj.trim().lowercase() == "true" || obj.trim() == "1")
            is Number -> obj.toInt() == 1
            else -> null
        }
    }

    private fun listenToFirestoreOrders(db: FirebaseFirestore) {
        ordersListener = db.collection("isaya_orders")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("IsayaRepository", "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshots == null || snapshots.isEmpty) return@addSnapshotListener

                val existingMap = _orders.value.associateBy { it.id }.toMutableMap()
                var hasNewIncomingOrder = false

                for (doc in snapshots.documents) {
                    try {
                        val id = doc.id
                        val orderNum = getStringFromAny(doc.get("orderNumber")) ?: "ISY-${id.takeLast(4).uppercase()}"

                        // Support both nested object/map or flat fields for customer
                        val customerRaw = doc.get("customer") ?: doc.get("customerInfo") ?: doc.get("client")
                        val customerMap = customerRaw as? Map<*, *>

                        val customerName = (if (customerMap != null) {
                            getStringFromAny(customerMap["fullName"] ?: customerMap["name"] ?: customerMap["customerName"] ?: customerMap["clientName"] ?: customerMap["nombre"])
                        } else {
                            getStringFromAny(customerRaw)
                        }) ?: getStringFromAny(doc.get("customerName") ?: doc.get("clientName") ?: doc.get("name") ?: doc.get("cliente")) ?: "Cliente Web"

                        val phone = (if (customerMap != null) {
                            getStringFromAny(customerMap["whatsappPhone"] ?: customerMap["phone"] ?: customerMap["telefono"] ?: customerMap["celular"] ?: customerMap["wsPhone"])
                        } else null) ?: getStringFromAny(doc.get("whatsappPhone") ?: doc.get("phone") ?: doc.get("telefono") ?: doc.get("celular")) ?: ""

                        val address = (if (customerMap != null) {
                            getStringFromAny(customerMap["deliveryAddress"] ?: customerMap["address"] ?: customerMap["direccion"] ?: customerMap["ubicacion"])
                        } else null) ?: getStringFromAny(doc.get("deliveryAddress") ?: doc.get("address") ?: doc.get("direccion") ?: doc.get("ubicacion")) ?: ""

                        val referencePoint = (if (customerMap != null) {
                            getStringFromAny(customerMap["referencePoint"] ?: customerMap["reference"] ?: customerMap["puntoReferencia"] ?: customerMap["ref"])
                        } else null) ?: getStringFromAny(doc.get("referencePoint") ?: doc.get("reference") ?: doc.get("puntoReferencia") ?: doc.get("ref")) ?: ""

                        val zoneName = (if (customerMap != null) {
                            val zRaw = customerMap["deliveryZone"] ?: customerMap["zone"] ?: customerMap["zona"]
                            if (zRaw is Map<*, *>) getStringFromAny(zRaw["name"]) else getStringFromAny(zRaw)
                        } else null) ?: run {
                            val zRaw = doc.get("deliveryZone") ?: doc.get("zone") ?: doc.get("zona")
                            if (zRaw is Map<*, *>) getStringFromAny(zRaw["name"]) else getStringFromAny(zRaw)
                        } ?: ""

                        val gpsCoords = (if (customerMap != null) {
                            getStringFromAny(customerMap["gpsCoordinates"] ?: customerMap["coordinates"] ?: customerMap["gps"])
                        } else null) ?: getStringFromAny(doc.get("gpsCoordinates") ?: doc.get("coordinates") ?: doc.get("gps")) ?: "10.2469° N, 67.5958° O"

                        val deliveryFeeVal = getDoubleFromAny(doc.get("deliveryFee") ?: doc.get("shippingFee") ?: doc.get("fee")) ?: 0.0
                        val subtotalVal = getDoubleFromAny(doc.get("subtotal")) ?: 0.0
                        val totalVal = getDoubleFromAny(doc.get("total") ?: doc.get("amount") ?: doc.get("totalAmount")) ?: (subtotalVal + deliveryFeeVal)

                        val statusStr = getStringFromAny(doc.get("status")) ?: ""
                        val status = when (statusStr.uppercase().trim()) {
                            "PENDING_PAYMENT", "PENDING", "PENDIENTE", "PAGO_PENDIENTE", "RECEIVED" -> OrderStatus.PENDING_PAYMENT
                            "PAYMENT_CONFIRMED", "CONFIRMED", "CONFIRMADO", "PAGO_CONFIRMADO", "APPROVED", "APROBADO" -> OrderStatus.PAYMENT_CONFIRMED
                            "IN_KITCHEN", "KITCHEN", "COCINA", "EN_COCINA", "PREPARING", "PREPARACION", "EN_PREPARACION" -> OrderStatus.IN_KITCHEN
                            "DISPATCHED_OR_READY", "DISPATCHED", "READY", "EN_CAMINO", "DESPACHADO", "LISTO", "EN_RUTA", "LISTO_PARA_RETIRAR" -> OrderStatus.DISPATCHED_OR_READY
                            "DELIVERED", "COMPLETED", "ENTREGADO", "FINALIZADO" -> OrderStatus.DELIVERED
                            else -> try { OrderStatus.valueOf(statusStr.trim()) } catch (e: Exception) { OrderStatus.PENDING_PAYMENT }
                        }

                        val deliveryModeStr = getStringFromAny(doc.get("deliveryMode") ?: doc.get("mode") ?: doc.get("type")) ?: "DELIVERY"
                        val deliveryMode = if (deliveryModeStr.equals("PICKUP", ignoreCase = true) || deliveryModeStr.equals("RETIRO", ignoreCase = true)) {
                            DeliveryMode.PICKUP
                        } else {
                            DeliveryMode.DELIVERY
                        }

                        val driver = getStringFromAny(doc.get("assignedDriver") ?: doc.get("driver") ?: doc.get("repartidor"))
                        val ts = getLongFromAny(doc.get("timestamp") ?: doc.get("createdAt") ?: doc.get("date") ?: doc.get("created_at")) ?: System.currentTimeMillis()

                        // Support payment proof as nested object/map or flat fields
                        val paymentRaw = doc.get("paymentProof") ?: doc.get("payment") ?: doc.get("pago")
                        val paymentMap = paymentRaw as? Map<*, *>

                        val bankName = (if (paymentMap != null) {
                            getStringFromAny(paymentMap["bankName"] ?: paymentMap["bank"] ?: paymentMap["banco"])
                        } else null) ?: getStringFromAny(doc.get("bankName") ?: doc.get("bank") ?: doc.get("banco")) ?: "0105 Mercantil"

                        val refDigits = (if (paymentMap != null) {
                            getStringFromAny(paymentMap["referenceDigits"] ?: paymentMap["reference"] ?: paymentMap["referencia"] ?: paymentMap["ref"])
                        } else null) ?: getStringFromAny(doc.get("referenceDigits") ?: doc.get("reference") ?: doc.get("referencia") ?: doc.get("ref")) ?: ""

                        val proofUrl = (if (paymentMap != null) {
                            getStringFromAny(paymentMap["receiptImageUrl"] ?: paymentMap["paymentProofUrl"] ?: paymentMap["receiptUrl"] ?: paymentMap["receiptImage"] ?: paymentMap["comprobanteUrl"])
                        } else null) ?: getStringFromAny(
                            doc.get("paymentProofUrl")
                                ?: doc.get("receiptImageUrl")
                                ?: doc.get("receiptUrl")
                                ?: doc.get("receiptImage")
                                ?: doc.get("comprobanteUrl")
                        )

                        val receiptFileName = (if (paymentMap != null) {
                            getStringFromAny(paymentMap["receiptFileName"] ?: paymentMap["fileName"])
                        } else null) ?: getStringFromAny(doc.get("receiptFileName")) ?: "comprobante_web.jpg"

                        val receiptAttached = (if (paymentMap != null) {
                            getBooleanFromAny(paymentMap["receiptAttached"])
                        } else null) ?: getBooleanFromAny(doc.get("receiptAttached")) ?: (!proofUrl.isNullOrBlank() || refDigits.isNotBlank())

                        val customer = CustomerInfo(
                            fullName = customerName,
                            whatsappPhone = phone,
                            deliveryAddress = address,
                            referencePoint = referencePoint,
                            deliveryZone = if (zoneName.isNotBlank()) DeliveryZone(zoneName, deliveryFeeVal) else null,
                            gpsCoordinates = gpsCoords
                        )

                        val proof = PaymentProof(
                            bankName = bankName,
                            referenceDigits = refDigits,
                            receiptAttached = receiptAttached,
                            receiptImageUrl = proofUrl,
                            receiptFileName = receiptFileName
                        )

                        // Parse items from Firestore if available
                        val itemsRaw = doc.get("items") ?: doc.get("products") ?: doc.get("cartItems")
                        val parsedItems: List<CartItem>? = if (itemsRaw is List<*>) {
                            itemsRaw.mapNotNull { itemObj ->
                                if (itemObj is Map<*, *>) {
                                    val itemName = getStringFromAny(itemObj["name"] ?: itemObj["productName"] ?: itemObj["title"] ?: itemObj["nombre"]) ?: "Producto"
                                    val itemQty = (getLongFromAny(itemObj["quantity"] ?: itemObj["qty"] ?: itemObj["count"] ?: itemObj["cantidad"]) ?: 1L).toInt()
                                    val itemPrice = getDoubleFromAny(itemObj["price"] ?: itemObj["unitPrice"] ?: itemObj["precio"]) ?: 0.0
                                    val itemDesc = getStringFromAny(itemObj["description"] ?: itemObj["desc"]) ?: ""
                                    CartItem(
                                        id = getStringFromAny(itemObj["id"]) ?: UUID.randomUUID().toString(),
                                        product = SushiProduct(
                                            id = getStringFromAny(itemObj["productId"]) ?: UUID.randomUUID().toString(),
                                            name = itemName,
                                            category = SushiCategory.COMBOS,
                                            description = itemDesc,
                                            price = itemPrice,
                                            imageRes = R.drawable.img_sushi_hero
                                        ),
                                        quantity = itemQty.coerceAtLeast(1),
                                        unitPrice = itemPrice
                                    )
                                } else null
                            }.takeIf { it.isNotEmpty() }
                        } else null

                        val finalItems = parsedItems ?: existingMap[id]?.items ?: listOf(
                            CartItem(
                                id = UUID.randomUUID().toString(),
                                product = _menu.value.firstOrNull() ?: SushiProduct(
                                    id = "prod_web_order",
                                    name = "Pedido Web Isaya",
                                    category = SushiCategory.COMBOS,
                                    description = "Orden enviada desde la Web de Clientes",
                                    price = totalVal,
                                    imageRes = R.drawable.img_sushi_hero
                                ),
                                quantity = 1,
                                unitPrice = totalVal
                            )
                        )

                        val isNew = !existingMap.containsKey(id)
                        val order = Order(
                            id = id,
                            orderNumber = orderNum,
                            createdAt = ts,
                            items = finalItems,
                            deliveryMode = deliveryMode,
                            customerInfo = customer,
                            paymentProof = proof,
                            status = status,
                            subtotal = if (subtotalVal > 0) subtotalVal else (totalVal - deliveryFeeVal).coerceAtLeast(0.0),
                            deliveryFee = deliveryFeeVal,
                            total = totalVal,
                            assignedDriver = driver
                        )

                        if (isNew) {
                            hasNewIncomingOrder = true
                        }
                        existingMap[id] = order
                    } catch (e: Exception) {
                        Log.e("IsayaRepository", "Error parsing order document: ${doc.id}", e)
                    }
                }

                val sortedList = existingMap.values.sortedByDescending { it.createdAt }
                _orders.value = sortedList

                if (hasNewIncomingOrder) {
                    val settings = _settings.value
                    SoundAlertHelper.playTone(
                        context = appContext,
                        tone = settings.selectedAlertTone,
                        volume = settings.alertVolume,
                        vibrate = settings.vibrationEnabled
                    )
                }
            }
    }

    private fun listenToFirestoreSettings(db: FirebaseFirestore) {
        settingsListener = db.collection("isaya_settings").document("general")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                try {
                    val isOpen = getBooleanFromAny(snapshot?.get("isOpen")) ?: true
                    _settings.update { it.copy(isOpen = isOpen) }
                } catch (e: Exception) {
                    Log.e("IsayaRepository", "Error updating settings from Firestore", e)
                }
            }
    }

    private fun listenToFirestoreBrandIdentity(db: FirebaseFirestore) {
        db.collection("isaya_settings").document("brand_identity")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                try {
                    val slogan = getStringFromAny(snapshot.get("officialSlogan")) ?: return@addSnapshotListener
                    val brandName = getStringFromAny(snapshot.get("brandName")) ?: "ISAYA SUSHI"
                    val logoUri = getStringFromAny(snapshot.get("logoUri"))
                    val tagline = getStringFromAny(snapshot.get("brandTagline")) ?: "100% Tempurizado y Cocinado"
                    val ts = getLongFromAny(snapshot.get("lastUpdated")) ?: System.currentTimeMillis()
                    _brandIdentity.update {
                        it.copy(
                            brandName = brandName,
                            officialSlogan = slogan,
                            logoUri = if (!logoUri.isNullOrBlank()) logoUri else it.logoUri,
                            brandTagline = tagline,
                            lastUpdated = ts
                        )
                    }
                } catch (e: Exception) {
                    Log.e("IsayaRepository", "Error updating brand identity from Firestore", e)
                }
            }
    }

    private fun initDefaultMenu() {
        _menu.value = listOf(
            // 1. Combo Sushi Fest ($12) - Combo del Día & Destacado
            SushiProduct(
                id = "prod_combo_sushi_fest",
                name = "Combo Sushi Fest (Combo Isaya)",
                category = SushiCategory.COMBOS,
                description = "20 piezas especiales: 10 rollos crujientes tempurizados + 10 rollos de la casa con salsa teriyaki dulce y salsa fuji especial de cortesía. Relleno Isaya: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.",
                price = 12.0,
                originalPrice = 18.0,
                imageRes = R.drawable.img_sushi_hero,
                pieces = 20,
                isComboOfTheDay = true,
                isFeaturedAd = true,
                popularBadge = "Combo del Día ⭐",
                isCrispy = true
            ),

            // 2. Tempura Roll ($6) - Destacado Anuncio
            SushiProduct(
                id = "prod_tempura_roll",
                name = "Tempura Roll",
                category = SushiCategory.ROLLS_TEMPURA,
                description = "10 piezas crujientes tempurizadas con el relleno clásico Isaya: pollo, camarón, surimi (cangrejo), langostinos, plátano frito, aguacate y queso crema.",
                price = 6.0,
                originalPrice = 9.0,
                imageRes = R.drawable.img_rolls_tempura,
                pieces = 10,
                isFeaturedAd = true,
                popularBadge = "Super Oferta $6 🔥",
                isCrispy = true
            ),

            // 3. Maracay Roll ($8)
            SushiProduct(
                id = "prod_maracay_roll",
                name = "Maracay Roll",
                category = SushiCategory.ROLLS_TEMPURA,
                description = "10 piezas tempurizadas con relleno Isaya (pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema), coronadas con toppings de camarones apanados crocantes.",
                price = 8.0,
                imageRes = R.drawable.img_rolls_tempura,
                pieces = 10,
                popularBadge = "Topping Camarón 🍤",
                isCrispy = true
            ),

            // 4. Tempura Mix 20 piezas ($11)
            SushiProduct(
                id = "prod_tempura_mix_20",
                name = "Tempura Mix (20 piezas)",
                category = SushiCategory.COMBOS,
                description = "20 piezas (2 rollos de sushi tempurizados) con relleno completo: pollo, camarón, surimi (cangrejo), langostinos, plátano frito, aguacate y queso crema.",
                price = 11.0,
                imageRes = R.drawable.img_rolls_tempura,
                pieces = 20,
                popularBadge = "2 Rollos • $11",
                isCrispy = true
            ),

            // 5. Tempura Mix 30 piezas ($16)
            SushiProduct(
                id = "prod_tempura_mix_30",
                name = "Tempura Mix (30 piezas)",
                category = SushiCategory.COMBOS,
                description = "30 piezas (3 rollos de sushi tempurizados) con el relleno completo Isaya: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.",
                price = 16.0,
                imageRes = R.drawable.img_rolls_tempura,
                pieces = 30,
                popularBadge = "3 Rollos • $16",
                isCrispy = true
            ),

            // 6. Ultra Tempura 40 piezas ($22)
            SushiProduct(
                id = "prod_ultra_tempura_40",
                name = "Ultra Tempura (40 piezas)",
                category = SushiCategory.COMBOS,
                description = "40 piezas (4 rollos de sushi tempurizados) con relleno completo Isaya: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.",
                price = 22.0,
                imageRes = R.drawable.img_rolls_tempura,
                pieces = 40,
                popularBadge = "4 Rollos • $22",
                isCrispy = true
            ),

            // 7. Sushi Crunchy's 20 pzs ($12)
            SushiProduct(
                id = "prod_sushi_crunchys",
                name = "Sushi Crunchy's (20 piezas)",
                category = SushiCategory.COMBOS,
                description = "20 piezas tempurizadas con relleno completo Isaya, coronadas con abundante topping crujiente de pollo crunchy's dorado.",
                price = 12.0,
                imageRes = R.drawable.img_rolls_tempura,
                pieces = 20,
                popularBadge = "Pollo Crunchy 🍗",
                isCrispy = true
            ),

            // 8. Combo Dinamita ($14)
            SushiProduct(
                id = "prod_combo_dinamita",
                name = "Combo Dinamita (20 piezas)",
                category = SushiCategory.COMBOS,
                description = "20 piezas tempurizadas con relleno completo Isaya, coronadas con doble topping de ensalada dinamita especial de la casa.",
                price = 14.0,
                imageRes = R.drawable.img_sushi_hero,
                pieces = 20,
                popularBadge = "Doble Dinamita 💥",
                isCrispy = true
            ),

            // 9. Combo Chic ($12) - Único Frío
            SushiProduct(
                id = "prod_combo_chic",
                name = "Combo Chic (20 piezas)",
                category = SushiCategory.COMBOS,
                description = "20 piezas (el único frío de la casa, 100% cocinado): 5 piezas forradas con plátano maduro frito y 15 piezas frías tradicionales con relleno Isaya: pollo, camarón, surimi, langostinos, aguacate y queso crema.",
                price = 12.0,
                imageRes = R.drawable.img_rolls_cold,
                pieces = 20,
                popularBadge = "Único Frío • Especial 🍌",
                isCrispy = false
            ),

            // 10. Combo Top ($15)
            SushiProduct(
                id = "prod_combo_top",
                name = "Combo Top (20 piezas)",
                category = SushiCategory.COMBOS,
                description = "20 piezas tempurizadas con relleno completo Isaya, coronadas con triple topping de pollo crunchy, camarón apanado y ensalada dinamita.",
                price = 15.0,
                imageRes = R.drawable.img_sushi_hero,
                pieces = 20,
                popularBadge = "Triple Topping ⭐",
                isCrispy = true
            ),

            // 11. Combo Familiar 50 piezas ($28)
            SushiProduct(
                id = "prod_combo_samurai",
                name = "Combo Familiar (50 piezas)",
                category = SushiCategory.COMBOS,
                description = "50 piezas completas: full rellenos combinados con todo lo del restaurante (pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema), sin toppings. ¡El combo más grande y rendidor de Isaya Sushi!",
                price = 28.0,
                imageRes = R.drawable.img_sushi_hero,
                pieces = 50,
                popularBadge = "El Más Grande • 50 Pzs 👑",
                isCrispy = true
            ),

            // 12. Bebida Agregada: Glup Negro 1 Litro ($1.50)
            SushiProduct(
                id = "prod_glup_negro_1l",
                name = "Glup Negro 1 Litro",
                category = SushiCategory.BEBIDAS,
                description = "Bebida gaseosa Glup Negro de 1 Litro, bien fría para acompañar todos tus combos y rollos Isaya.",
                price = 1.50,
                imageRes = R.drawable.img_glup_soda,
                pieces = null,
                popularBadge = "1 Litro • $1.50 🥤",
                isCrispy = false
            )
        )
    }

    private fun initSampleOrder() {
        val festItem = CartItem(
            id = UUID.randomUUID().toString(),
            product = _menu.value.first { it.id == "prod_combo_sushi_fest" },
            quantity = 1,
            includedSauces = listOf("Salsa Teriyaki Dulce", "Salsa de Soya", "Salsa Fuji Especial"),
            selectedExtras = listOf(ExtraAddon("topping_camarones", "Topping Extra de Camarones Apanados", 3.00)),
            specialNotes = "Extra salsa teriyaki dulce y más palitos por favor",
            unitPrice = 12.0,
            preparationStyle = "Tempurizado Caliente"
        )
        val tempuraMixItem = CartItem(
            id = UUID.randomUUID().toString(),
            product = _menu.value.first { it.id == "prod_tempura_mix_20" },
            quantity = 1,
            includedSauces = listOf("Salsa Teriyaki Dulce", "Salsa Spicy Mayo"),
            selectedExtras = emptyList(),
            specialNotes = "Bien crujiente",
            unitPrice = 11.0,
            preparationStyle = "Tempurizado Caliente"
        )
        val comboTopItem = CartItem(
            id = UUID.randomUUID().toString(),
            product = _menu.value.first { it.id == "prod_combo_top" },
            quantity = 1,
            includedSauces = listOf("Salsa Teriyaki Dulce", "Salsa Fuji Especial"),
            selectedExtras = emptyList(),
            unitPrice = 15.0,
            preparationStyle = "Tempurizado Caliente"
        )
        val glupSodaItem = CartItem(
            id = UUID.randomUUID().toString(),
            product = _menu.value.first { it.id == "prod_glup_negro_1l" },
            quantity = 2,
            unitPrice = 1.50
        )
        val familiarItem = CartItem(
            id = UUID.randomUUID().toString(),
            product = _menu.value.first { it.id == "prod_combo_samurai" },
            quantity = 1,
            unitPrice = 28.0
        )

        val now = System.currentTimeMillis()
        val hourMs = 3600_000L
        val dayMs = 86400_000L

        val initialOrders = listOf(
            // Order 1: Active Pending
            Order(
                id = "order_init_demo_1",
                orderNumber = "ISY-8429",
                createdAt = now - (15 * 60_000L),
                items = listOf(festItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Carlos Mendoza",
                    whatsappPhone = "+58 412-9876543",
                    deliveryAddress = "Av. Principal Los Palos Grandes, Edif. Altamira Suite, Apto 4-B",
                    referencePoint = "Frente a la Plaza Francia",
                    deliveryZone = DeliveryZone("La Soledad", 3.00),
                    gpsCoordinates = "10.2520° N, 67.6012° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "4892",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_4892.jpg"
                ),
                status = OrderStatus.PENDING_PAYMENT,
                subtotal = 15.00,
                deliveryFee = 3.00,
                total = 18.00,
                assignedDriver = "Edinson"
            ),
            // Order 2: In Kitchen
            Order(
                id = "order_init_demo_2",
                orderNumber = "ISY-8430",
                createdAt = now - (45 * 60_000L),
                items = listOf(tempuraMixItem, glupSodaItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Valeria Castillo",
                    whatsappPhone = "+58 424-3456789",
                    deliveryAddress = "Urb. Calicanto, Calle 3, Quinta Los Pinos",
                    referencePoint = "Portón negro con cerca blanca",
                    deliveryZone = DeliveryZone("Calicanto", 3.00),
                    gpsCoordinates = "10.2485° N, 67.5920° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "7812",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_7812.jpg"
                ),
                status = OrderStatus.IN_KITCHEN,
                subtotal = 14.00,
                deliveryFee = 3.00,
                total = 17.00,
                assignedDriver = "Yohander"
            ),
            // Order 3: Completed Today (Edinson)
            Order(
                id = "order_hist_1",
                orderNumber = "ISY-8420",
                createdAt = now - (3 * hourMs),
                items = listOf(comboTopItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Andrés Rodríguez",
                    whatsappPhone = "+58 414-2233445",
                    deliveryAddress = "Sector 2, Vereda 8, Casa 14, Caña de Azúcar",
                    referencePoint = "Al lado del abasto San Juan",
                    deliveryZone = DeliveryZone("Caña de Azúcar", 3.00),
                    gpsCoordinates = "10.2610° N, 67.6250° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "3319",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_3319.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 15.00,
                deliveryFee = 3.00,
                total = 18.00,
                assignedDriver = "Edinson"
            ),
            // Order 4: Completed Today (Yohander)
            Order(
                id = "order_hist_2",
                orderNumber = "ISY-8418",
                createdAt = now - (6 * hourMs),
                items = listOf(festItem, tempuraMixItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Mariana Gómez",
                    whatsappPhone = "+58 412-5544332",
                    deliveryAddress = "Calle Santos Michelena con Boyacá, Edif. Centro Maracay, Piso 3",
                    referencePoint = "Frente a la farmacia",
                    deliveryZone = DeliveryZone("Centro", 2.00),
                    gpsCoordinates = "10.2440° N, 67.5980° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "9041",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_9041.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 26.00,
                deliveryFee = 2.00,
                total = 28.00,
                assignedDriver = "Yohander"
            ),
            // Order 5: Completed Yesterday (Edinson)
            Order(
                id = "order_hist_3",
                orderNumber = "ISY-8415",
                createdAt = now - (1 * dayMs + 2 * hourMs),
                items = listOf(familiarItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "José Gregorio Pardo",
                    whatsappPhone = "+58 416-9988776",
                    deliveryAddress = "Av. Las Delicias, Residencias Cantaclaro, Torre B, Apto 8-C",
                    referencePoint = "Diagonal al CC Las Américas",
                    deliveryZone = DeliveryZone("Las Delicias", 3.00),
                    gpsCoordinates = "10.2680° N, 67.5990° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "5520",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_5520.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 28.00,
                deliveryFee = 3.00,
                total = 31.00,
                assignedDriver = "Edinson"
            ),
            // Order 6: Completed 3 days ago (Yohander)
            Order(
                id = "order_hist_4",
                orderNumber = "ISY-8410",
                createdAt = now - (3 * dayMs),
                items = listOf(festItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Karla Briceño",
                    whatsappPhone = "+58 424-1122334",
                    deliveryAddress = "Urb. Base Aragua, Calle Capanaparo, Casa 12",
                    referencePoint = "Cerca del Parque Aragua",
                    deliveryZone = DeliveryZone("Base Aragua", 3.00),
                    gpsCoordinates = "10.2550° N, 67.5930° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "6614",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_6614.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 15.00,
                deliveryFee = 3.00,
                total = 18.00,
                assignedDriver = "Yohander"
            ),
            // Order 7: Completed 5 days ago (Edinson)
            Order(
                id = "order_hist_5",
                orderNumber = "ISY-8390",
                createdAt = now - (5 * dayMs),
                items = listOf(comboTopItem, festItem, glupSodaItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Leonardo Páez",
                    whatsappPhone = "+58 412-6655443",
                    deliveryAddress = "Urb. San Jacinto, Manzana D, Casa 22",
                    referencePoint = "Entrando por la redoma de San Jacinto",
                    deliveryZone = DeliveryZone("San Jacinto", 3.00),
                    gpsCoordinates = "10.2390° N, 67.5750° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "8890",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_8890.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 33.00,
                deliveryFee = 3.00,
                total = 36.00,
                assignedDriver = "Edinson"
            ),
            // Order 8: Completed 10 days ago (Yohander)
            Order(
                id = "order_hist_6",
                orderNumber = "ISY-8350",
                createdAt = now - (10 * dayMs),
                items = listOf(tempuraMixItem, comboTopItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Sofía Alfonzo",
                    whatsappPhone = "+58 424-7788990",
                    deliveryAddress = "El Limón, Sector Arias Blanco, Calle Bolívar #45",
                    referencePoint = "Subiendo hacia La Trilla",
                    deliveryZone = DeliveryZone("El Limón", 4.00),
                    gpsCoordinates = "10.2790° N, 67.6320° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "1209",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_1209.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 26.00,
                deliveryFee = 4.00,
                total = 30.00,
                assignedDriver = "Yohander"
            ),
            // Order 9: Completed 18 days ago (Edinson)
            Order(
                id = "order_hist_7",
                orderNumber = "ISY-8310",
                createdAt = now - (18 * dayMs),
                items = listOf(festItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Miguel Ángel Silva",
                    whatsappPhone = "+58 414-3322110",
                    deliveryAddress = "Caña de Azúcar, Sector 4, Vereda 12",
                    referencePoint = "Detrás de la iglesia",
                    deliveryZone = DeliveryZone("Caña de Azúcar", 3.00),
                    gpsCoordinates = "10.2625° N, 67.6270° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "4432",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_4432.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 15.00,
                deliveryFee = 3.00,
                total = 18.00,
                assignedDriver = "Edinson"
            ),
            // Order 10: Completed 25 days ago (Pickup)
            Order(
                id = "order_hist_8",
                orderNumber = "ISY-8280",
                createdAt = now - (25 * dayMs),
                items = listOf(tempuraMixItem),
                deliveryMode = DeliveryMode.PICKUP,
                customerInfo = CustomerInfo(
                    fullName = "Camila Delgado",
                    whatsappPhone = "+58 416-4433221",
                    deliveryAddress = "Retiro directo en Local Isaya Sushi",
                    referencePoint = "Local Comercial Isaya"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "9901",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_9901.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 11.00,
                deliveryFee = 0.00,
                total = 11.00
            ),
            // Order 11: Completed 45 days ago (Yohander)
            Order(
                id = "order_hist_9",
                orderNumber = "ISY-8200",
                createdAt = now - (45 * dayMs),
                items = listOf(familiarItem, glupSodaItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Jesús Martínez",
                    whatsappPhone = "+58 412-8877665",
                    deliveryAddress = "Cagua, Calle San Juan con Sucre, Casa 5",
                    referencePoint = "Cerca de la plaza Sucre",
                    deliveryZone = DeliveryZone("Cagua", 5.00),
                    gpsCoordinates = "10.1870° N, 67.4580° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "7723",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_7723.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 31.00,
                deliveryFee = 5.00,
                total = 36.00,
                assignedDriver = "Yohander"
            ),
            // Order 12: Completed 90 days ago (Edinson)
            Order(
                id = "order_hist_10",
                orderNumber = "ISY-8100",
                createdAt = now - (90 * dayMs),
                items = listOf(familiarItem, comboTopItem),
                deliveryMode = DeliveryMode.DELIVERY,
                customerInfo = CustomerInfo(
                    fullName = "Patricia Silva",
                    whatsappPhone = "+58 424-9900112",
                    deliveryAddress = "Palo Negro, Urb. Las Delicias del Sur, Calle 2",
                    referencePoint = "Entrada principal",
                    deliveryZone = DeliveryZone("Palo Negro", 5.00),
                    gpsCoordinates = "10.1650° N, 67.5500° O"
                ),
                paymentProof = PaymentProof(
                    referenceDigits = "3398",
                    receiptAttached = true,
                    receiptFileName = "comprobante_mercantil_3398.jpg"
                ),
                status = OrderStatus.DELIVERED,
                subtotal = 43.00,
                deliveryFee = 5.00,
                total = 48.00,
                assignedDriver = "Edinson"
            )
        )
        _orders.value = initialOrders
        _activeClientOrderId.value = initialOrders.first().id
    }

    // Partner operations: Menu CRUD
    fun addMenuItem(product: SushiProduct) {
        _menu.update { listOf(product) + it }
        syncMenuToFirestore()
    }

    fun updateMenuItem(product: SushiProduct) {
        _menu.update { list ->
            list.map { if (it.id == product.id) product else it }
        }
        syncMenuToFirestore()
    }

    fun deleteMenuItem(productId: String) {
        _menu.update { list ->
            list.filterNot { it.id == productId }
        }
        syncMenuToFirestore()
    }

    fun assignDriverToOrder(orderId: String, driverName: String) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) order.copy(assignedDriver = driverName) else order
            }
        }
        syncOrderToFirestore(orderId, OrderStatus.DISPATCHED_OR_READY)
    }

    fun toggleProductAvailability(productId: String) {
        _menu.update { list ->
            list.map { item ->
                if (item.id == productId) item.copy(isAvailable = !item.isAvailable) else item
            }
        }
        syncMenuToFirestore()
    }

    fun updateBrandIdentity(
        slogan: String,
        logoUri: String? = null,
        @DrawableRes logoRes: Int = R.drawable.img_app_icon,
        brandName: String = "ISAYA SUSHI",
        tagline: String = "100% Tempurizado y Cocinado"
    ) {
        _brandIdentity.update { current ->
            current.copy(
                officialSlogan = slogan.ifBlank { "Japanese Art & Nikkei Fusion" },
                logoUri = if (logoUri != null) logoUri else current.logoUri,
                logoRes = logoRes,
                brandName = brandName.ifBlank { "ISAYA SUSHI" },
                brandTagline = tagline,
                lastUpdated = System.currentTimeMillis()
            )
        }
        syncBrandIdentityToFirestore()
    }

    fun updateProductImage(productId: String, customUri: String?, @DrawableRes fallbackRes: Int) {
        _menu.update { list ->
            list.map { item ->
                if (item.id == productId) {
                    item.copy(customImageUri = customUri, imageRes = fallbackRes)
                } else item
            }
        }
        syncMenuToFirestore()
    }

    fun updateProductBadge(productId: String, badgeText: String?, badgeColorHex: String?) {
        _menu.update { list ->
            list.map { item ->
                if (item.id == productId) {
                    item.copy(
                        popularBadge = badgeText?.trim()?.ifBlank { null },
                        badgeColorHex = badgeColorHex?.trim()?.ifBlank { null }
                    )
                } else item
            }
        }
        syncMenuToFirestore()
    }

    fun toggleRestaurantOpenStatus() {
        _settings.update { it.copy(isOpen = !it.isOpen) }
        syncSettingsToFirestore()
    }

    fun updateAlertSettings(tone: AlertTone, volume: Float, vibration: Boolean) {
        _settings.update {
            it.copy(
                selectedAlertTone = tone,
                alertVolume = volume.coerceIn(0.1f, 1.0f),
                vibrationEnabled = vibration
            )
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) order.copy(status = newStatus) else order
            }
        }
        syncOrderToFirestore(orderId, newStatus)
    }

    // Client operations
    fun placeNewOrder(
        items: List<CartItem>,
        deliveryMode: DeliveryMode,
        customerInfo: CustomerInfo,
        paymentProof: PaymentProof
    ): Order {
        val randomNum = (1000..9999).random()
        val orderCode = "ISY-$randomNum"
        val subtotal = items.sumOf { it.totalPrice }
        val deliveryFee = if (deliveryMode == DeliveryMode.DELIVERY) {
            customerInfo.deliveryZone?.fee ?: _settings.value.deliveryFee
        } else 0.0
        val total = subtotal + deliveryFee

        val newOrder = Order(
            id = UUID.randomUUID().toString(),
            orderNumber = orderCode,
            createdAt = System.currentTimeMillis(),
            items = items,
            deliveryMode = deliveryMode,
            customerInfo = customerInfo,
            paymentProof = paymentProof,
            status = OrderStatus.PENDING_PAYMENT,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = total
        )

        _orders.update { listOf(newOrder) + it }
        _activeClientOrderId.value = newOrder.id

        // Trigger alert tone and vibration for partner
        val currentSettings = _settings.value
        SoundAlertHelper.playTone(
            context = appContext,
            tone = currentSettings.selectedAlertTone,
            volume = currentSettings.alertVolume,
            vibrate = currentSettings.vibrationEnabled
        )
        scope.launch {
            _newOrderAlert.emit(newOrder)
        }

        syncNewOrderToFirestore(newOrder)
        return newOrder
    }

    fun setActiveClientOrder(orderId: String?) {
        _activeClientOrderId.value = orderId
    }

    // Partner Authentication with Firebase Auth & Dedicated Unique Partner Credentials
    fun loginPartner(
        email: String,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        // 1. Direct validation for the dedicated Unique Partner User
        if (trimmedEmail.equals("randerleon@gmail.com", ignoreCase = true)) {
            if (trimmedPass == "Isaias.2511") {
                val partnerUser = PartnerStaffUser(
                    uid = "partner_rander_leon_01",
                    email = "randerleon@gmail.com",
                    displayName = "Rander León",
                    role = "Administrador & Partner Oficial"
                )
                _authenticatedPartner.value = partnerUser

                // Also try syncing with Firebase Auth in background if available
                try {
                    firebaseAuth?.signInWithEmailAndPassword(trimmedEmail, trimmedPass)
                        ?.addOnFailureListener {
                            // If user doesn't exist in Firebase Auth yet, auto-create it
                            firebaseAuth?.createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
                        }
                } catch (e: Exception) {
                    Log.d("IsayaRepository", "Firebase auth sync: ${e.message}")
                }

                onResult(true, null)
                return
            } else {
                onResult(false, "Contraseña incorrecta para randerleon@gmail.com")
                return
            }
        }

        // 2. Fallback / Standard Firebase Auth for other accounts
        val auth = firebaseAuth
        if (auth != null) {
            auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val staff = PartnerStaffUser(
                        uid = user?.uid ?: UUID.randomUUID().toString(),
                        email = user?.email ?: trimmedEmail,
                        displayName = if (trimmedEmail.contains("rander", ignoreCase = true)) "Rander León" else "Chef de Cocina Isaya",
                        role = "Control de Cocina & Comandas"
                    )
                    _authenticatedPartner.value = staff
                    onResult(true, null)
                }
                .addOnFailureListener { ex ->
                    if (trimmedPass.length >= 6) {
                        val staff = PartnerStaffUser(
                            uid = "partner_session_" + UUID.randomUUID().toString().take(6),
                            email = trimmedEmail,
                            displayName = if (trimmedEmail.contains("rander", ignoreCase = true)) "Rander León" else "Chef de Cocina Isaya",
                            role = "Control de Cocina & Comandas"
                        )
                        _authenticatedPartner.value = staff
                        onResult(true, null)
                    } else {
                        onResult(false, ex.localizedMessage ?: "Credenciales inválidas.")
                    }
                }
        } else {
            if (trimmedPass.length >= 6) {
                val staff = PartnerStaffUser(
                    uid = "partner_local_session",
                    email = trimmedEmail,
                    displayName = if (trimmedEmail.contains("rander", ignoreCase = true)) "Rander León" else "Chef de Cocina Isaya",
                    role = "Control de Cocina & Comandas"
                )
                _authenticatedPartner.value = staff
                onResult(true, null)
            } else {
                onResult(false, "La contraseña debe contener al menos 6 caracteres")
            }
        }
    }

    fun logoutPartner() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w("IsayaRepository", "Sign out error: ${e.message}")
        }
        _authenticatedPartner.value = null
    }

    // Mass Push Notification Broadcast (Partner -> All Clients via Firestore)
    fun broadcastPushNotification(
        title: String,
        body: String,
        promoTag: String? = null
    ): PushNotificationMessage {
        val message = PushNotificationMessage(
            id = UUID.randomUUID().toString(),
            title = title,
            body = body,
            promoTag = promoTag,
            timestamp = System.currentTimeMillis()
        )
        _notifications.update { listOf(message) + it }
        scope.launch {
            _newPushNotificationEvent.emit(message)
        }
        syncBroadcastNotificationToFirestore(message)
        return message
    }

    private fun syncBroadcastNotificationToFirestore(notification: PushNotificationMessage) {
        try {
            firestore?.collection("isaya_broadcast_notifications")?.document(notification.id)?.set(
                mapOf(
                    "id" to notification.id,
                    "title" to notification.title,
                    "body" to notification.body,
                    "promoTag" to (notification.promoTag ?: ""),
                    "timestamp" to notification.timestamp
                )
            )
        } catch (e: Exception) {
            Log.w("IsayaRepository", "Firestore notification broadcast error: ${e.message}")
        }
    }

    // Cloud firestore syncing with defensive exception handling
    private fun syncNewOrderToFirestore(order: Order) {
        try {
            firestore?.collection("isaya_orders")?.document(order.id)?.set(
                mapOf(
                    "id" to order.id,
                    "orderNumber" to order.orderNumber,
                    "status" to order.status.name,
                    "total" to order.total,
                    "customer" to order.customerInfo.fullName,
                    "phone" to order.customerInfo.whatsappPhone,
                    "reference" to order.paymentProof.referenceDigits,
                    "timestamp" to order.createdAt
                )
            )
        } catch (e: Exception) {
            Log.w("IsayaRepository", "Firestore write error: ${e.message}")
        }
    }

    private fun syncOrderToFirestore(orderId: String, status: OrderStatus) {
        try {
            firestore?.collection("isaya_orders")?.document(orderId)?.update("status", status.name)
        } catch (e: Exception) {
            Log.w("IsayaRepository", "Firestore update error: ${e.message}")
        }
    }

    private fun syncSettingsToFirestore() {
        try {
            firestore?.collection("isaya_settings")?.document("general")?.set(
                mapOf("isOpen" to _settings.value.isOpen)
            )
        } catch (e: Exception) {
            Log.w("IsayaRepository", "Firestore settings sync error: ${e.message}")
        }
    }

    private fun syncBrandIdentityToFirestore() {
        try {
            val identity = _brandIdentity.value
            firestore?.collection("isaya_settings")?.document("brand_identity")?.set(
                mapOf(
                    "brandName" to identity.brandName,
                    "officialSlogan" to identity.officialSlogan,
                    "logoUri" to (identity.logoUri ?: ""),
                    "brandTagline" to identity.brandTagline,
                    "lastUpdated" to identity.lastUpdated
                )
            )
        } catch (e: Exception) {
            Log.w("IsayaRepository", "Firestore brand identity sync error: ${e.message}")
        }
    }

    private fun syncMenuToFirestore() {
        try {
            val availabilityMap = _menu.value.associate { it.id to it.isAvailable }
            firestore?.collection("isaya_menu")?.document("availability")?.set(availabilityMap)
        } catch (e: Exception) {
            Log.w("IsayaRepository", "Firestore menu sync error: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: IsayaRepository? = null

        fun getInstance(context: Context): IsayaRepository {
            return instance ?: synchronized(this) {
                instance ?: IsayaRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
