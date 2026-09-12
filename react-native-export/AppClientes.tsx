/**
 * ISAYA SUSHI - APP DE CLIENTES (APK 1)
 * Proyecto independiente de React Native / Expo
 * 
 * Funcionalidades:
 * - Catálogo Menú Nikkei ($6 a $24 USD) con personalización de salsas y extras
 * - Carrito de compras flotante con selector Delivery ($2.50) / Retiro ($0.00)
 * - Checkout Pago Móvil Banesco con comprobante y 4 dígitos de referencia
 * - Seguimiento de pedido en 4 etapas en tiempo real con botón WhatsApp
 * - Recepción de notificaciones push masivas desde Firebase Firestore
 * 
 * Paleta: Negro (#0D0D0D), Dorado (#D4AF37), Rojo (#C8102E)
 */

import React, { useState, useEffect, useMemo } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Modal,
  SafeAreaView,
  StatusBar,
  FlatList,
  Alert,
  Linking
} from 'react-native';
import { db } from './firebaseConfig';
import {
  collection,
  addDoc,
  doc,
  onSnapshot,
  query,
  orderBy,
  limit
} from 'firebase/firestore';

// --- Paleta de Colores Isaya Luxury ---
const COLORS = {
  black: '#0D0D0D',
  surface: '#141414',
  card: '#1C1C1E',
  cardElevated: '#252528',
  border: '#2C2C2E',
  gold: '#D4AF37',
  goldLight: '#F3E5AB',
  goldContainer: '#2A230B',
  red: '#C8102E',
  redLight: '#FF4D6D',
  green: '#2ED573',
  textPrimary: '#F5F5F7',
  textSecondary: '#A0A0A5',
  textMuted: '#6E6E73',
};

// --- Tarifas Oficiales de Delivery por Zona (Maracay y Aragua) ---
export const MARACAY_DELIVERY_ZONES = [
  // $1.00 USD (9 zonas)
  { name: "13 de Enero", fee: 1.00 },
  { name: "Araguama Country", fee: 1.00 },
  { name: "Campo Alegre", fee: 1.00 },
  { name: "Casanova Godoy", fee: 1.00 },
  { name: "La Mulera", fee: 1.00 },
  { name: "Los Cocos", fee: 1.00 },
  { name: "Los Samanes", fee: 1.00 },
  { name: "Madre María", fee: 1.00 },
  { name: "San Rafael", fee: 1.00 },

  // $2.00 USD (25 zonas)
  { name: "23 de Enero", fee: 2.00 },
  { name: "CC Galería Plaza", fee: 2.00 },
  { name: "CC Paseo Estación Central", fee: 2.00 },
  { name: "CC Unicentro", fee: 2.00 },
  { name: "Centro", fee: 2.00 },
  { name: "Cuartel Páez", fee: 2.00 },
  { name: "Francisco de Miranda", fee: 2.00 },
  { name: "Guaruto", fee: 2.00 },
  { name: "Guasimal", fee: 2.00 },
  { name: "Hiper Jumbo", fee: 2.00 },
  { name: "José Félix Ribas", fee: 2.00 },
  { name: "La Barraca", fee: 2.00 },
  { name: "Los Olivos", fee: 2.00 },
  { name: "Luxor", fee: 2.00 },
  { name: "Maracay Plaza", fee: 2.00 },
  { name: "Museo Aeronáutico", fee: 2.00 },
  { name: "Parque Aragua", fee: 2.00 },
  { name: "Piñonal", fee: 2.00 },
  { name: "Plaza Bolívar", fee: 2.00 },
  { name: "Río Blanco", fee: 2.00 },
  { name: "San Agustín", fee: 2.00 },
  { name: "San Ignacio", fee: 2.00 },
  { name: "San José", fee: 2.00 },
  { name: "San Luis", fee: 2.00 },
  { name: "Terminal de Maracay", fee: 2.00 },

  // $3.00 USD (25 zonas)
  { name: "Arsenal", fee: 3.00 },
  { name: "Andrés Bello", fee: 3.00 },
  { name: "Aviación", fee: 3.00 },
  { name: "Base Aragua", fee: 3.00 },
  { name: "Base Sucre", fee: 3.00 },
  { name: "Calicanto", fee: 3.00 },
  { name: "Candelaria", fee: 3.00 },
  { name: "Caña de Azúcar", fee: 3.00 },
  { name: "CC Las Américas", fee: 3.00 },
  { name: "CC Los Aviadores", fee: 3.00 },
  { name: "Coropo 2000", fee: 3.00 },
  { name: "El Toro", fee: 3.00 },
  { name: "Hospital Central", fee: 3.00 },
  { name: "La Cooperativa", fee: 3.00 },
  { name: "La Coromoto", fee: 3.00 },
  { name: "La Floresta", fee: 3.00 },
  { name: "La Pedrera", fee: 3.00 },
  { name: "La Romana", fee: 3.00 },
  { name: "La Soledad", fee: 3.00 },
  { name: "Las Delicias", fee: 3.00 },
  { name: "Montaña Fresca", fee: 3.00 },
  { name: "Morita", fee: 3.00 },
  { name: "Paraparal", fee: 3.00 },
  { name: "San Jacinto", fee: 3.00 },
  { name: "Santa Rita", fee: 3.00 },

  // $4.00 USD (10 zonas)
  { name: "Camburito", fee: 4.00 },
  { name: "Cantarrana", fee: 4.00 },
  { name: "El Castaño", fee: 4.00 },
  { name: "El Limón", fee: 4.00 },
  { name: "Encrucijada de Turmero", fee: 4.00 },
  { name: "Fundacoropo", fee: 4.00 },
  { name: "La Ovallera", fee: 4.00 },
  { name: "Los Hornos", fee: 4.00 },
  { name: "Samán de Güere", fee: 4.00 },
  { name: "Residencias Palo Negro", fee: 4.00 },

  // $5.00 USD (2 zonas)
  { name: "Cagua", fee: 5.00 },
  { name: "Palo Negro", fee: 5.00 },

  // $6.00 USD (2 zonas)
  { name: "Cagua Centro", fee: 6.00 },
  { name: "La Paya", fee: 6.00 }
];

// --- Menú Nikkei Oficial Isaya Sushi ---
export const MENU_PRODUCTS = [
  { 
    id: 'prod_combo_sushi_fest', 
    name: 'Combo Sushi Fest (Combo Isaya)', 
    category: 'Combos Especiales', 
    pieces: 20, 
    price: 12.0, 
    originalPrice: 18.0, 
    isComboOfTheDay: true, 
    description: '20 piezas especiales: 10 Ebi Crunch Tempura crujientes + 10 rolls de la casa con salsa teriyaki y spicy mayo de cortesía. Relleno Isaya completo: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.', 
    badge: 'Combo del Día ⭐' 
  },
  { 
    id: 'prod_tempura_roll', 
    name: 'Tempura Roll', 
    category: 'Rolls Tempura', 
    pieces: 10, 
    price: 6.0, 
    originalPrice: 9.0, 
    description: '10 piezas crujientes tempurizadas con relleno Isaya: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.', 
    badge: 'Super Oferta $6 🔥' 
  },
  { 
    id: 'prod_maracay_roll', 
    name: 'Maracay Roll', 
    category: 'Rolls Tempura', 
    pieces: 10, 
    price: 8.0, 
    description: '10 piezas tempurizadas con relleno Isaya, coronadas con generosos toppings de camarones apanados crocantes.', 
    badge: 'Topping Camarón 🍤' 
  },
  { 
    id: 'prod_tempura_mix_20', 
    name: 'Tempura Mix (20 piezas)', 
    category: 'Combos Especiales', 
    pieces: 20, 
    price: 11.0, 
    description: '20 piezas (2 rollos de sushi tempurizados) con relleno completo Isaya: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.', 
    badge: '2 Rollos • $11' 
  },
  { 
    id: 'prod_tempura_mix_30', 
    name: 'Tempura Mix (30 piezas)', 
    category: 'Combos Especiales', 
    pieces: 30, 
    price: 16.0, 
    description: '30 piezas (3 rollos de sushi tempurizados) con relleno completo Isaya: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.', 
    badge: '3 Rollos • $16' 
  },
  { 
    id: 'prod_ultra_tempura_40', 
    name: 'Ultra Tempura (40 piezas)', 
    category: 'Combos Especiales', 
    pieces: 40, 
    price: 22.0, 
    description: '40 piezas (4 rollos de sushi tempurizados) con relleno completo Isaya: pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema.', 
    badge: '4 Rollos • $22' 
  },
  { 
    id: 'prod_sushi_crunchys', 
    name: "Sushi Crunchy's (20 piezas)", 
    category: 'Combos Especiales', 
    pieces: 20, 
    price: 12.0, 
    description: "20 piezas tempurizadas con relleno Isaya, coronadas con abundante topping crujiente de pollo crunchy's dorado.", 
    badge: 'Pollo Crunchy 🍗' 
  },
  { 
    id: 'prod_combo_dinamita', 
    name: 'Combo Dinamita (20 piezas)', 
    category: 'Combos Especiales', 
    pieces: 20, 
    price: 14.0, 
    description: '20 piezas tempurizadas con relleno Isaya, coronadas con doble topping de ensalada dinamita especial de la casa.', 
    badge: 'Doble Dinamita 💥' 
  },
  { 
    id: 'prod_combo_chic', 
    name: 'Combo Chic (20 piezas)', 
    category: 'Combos Especiales', 
    pieces: 20, 
    price: 12.0, 
    description: '20 piezas (el único frío de la casa, 100% cocinado): 5 piezas forradas de plátano maduro, 5 tempuras, 5 maki y 5 uramaki con relleno Isaya: pollo, camarón, surimi, langostinos, aguacate y queso crema.', 
    badge: 'Único Frío • Especial 🍌' 
  },
  { 
    id: 'prod_combo_top', 
    name: 'Combo Top (20 piezas)', 
    category: 'Combos Especiales', 
    pieces: 20, 
    price: 15.0, 
    description: '20 piezas tempurizadas con relleno completo Isaya, coronadas con triple topping de pollo crunchy, camarón apanado y ensalada dinamita.', 
    badge: 'Triple Topping ⭐' 
  },
  { 
    id: 'prod_combo_samurai', 
    name: 'Combo Samurai (50 piezas)', 
    category: 'Combos Especiales', 
    pieces: 50, 
    price: 28.0, 
    description: '50 piezas completas: full rellenos combinados con todo lo del restaurante (pollo, camarón, surimi, langostinos, plátano frito, aguacate y queso crema), sin toppings. ¡El combo más grande y rendidor!', 
    badge: 'El Más Grande • 50 Pzs 👑' 
  },
  { 
    id: 'prod_glup_negro_1l', 
    name: 'Glup Negro 1 Litro', 
    category: 'Bebidas', 
    price: 1.50, 
    description: 'Bebida gaseosa Glup Negro de 1 Litro, bien fría para acompañar todos tus combos y rollos Isaya.', 
    badge: '1 Litro • $1.50 🥤' 
  }
];

const CATEGORIES = ['Todos', 'Combos Especiales', 'Rolls Tempura', 'Bebidas'];

export default function AppClientes() {
  const [selectedCategory, setSelectedCategory] = useState('Todos');
  const [deliveryMode, setDeliveryMode] = useState<'DELIVERY' | 'PICKUP'>('DELIVERY');
  const [cart, setCart] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState<'MENU' | 'TRACKER'>('MENU');
  
  // Modales
  const [selectedProduct, setSelectedProduct] = useState<any | null>(null);
  const [isCheckoutVisible, setIsCheckoutVisible] = useState(false);
  const [isNotificationsVisible, setIsNotificationsVisible] = useState(false);

  // Datos Checkout Pago Móvil
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [customerAddress, setCustomerAddress] = useState('');
  const [selectedZone, setSelectedZone] = useState<any | null>(null);
  const [isZonePickerVisible, setIsZonePickerVisible] = useState(false);
  const [zoneSearch, setZoneSearch] = useState('');
  const [referenceDigits, setReferenceDigits] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Pedido activo & Notificaciones Push
  const [activeOrder, setActiveOrder] = useState<any | null>(null);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [bannerPush, setBannerPush] = useState<any | null>(null);

  // Escuchar notificaciones masivas desde Firestore
  useEffect(() => {
    try {
      const notifQuery = query(collection(db, 'isaya_broadcast_notifications'), orderBy('timestamp', 'desc'), limit(10));
      const unsubscribe = onSnapshot(notifQuery, (snapshot) => {
        const list: any[] = [];
        snapshot.forEach((doc) => {
          list.push({ id: doc.id, ...doc.data() });
        });
        if (list.length > 0) {
          setNotifications(list);
          // Mostrar banner emergente del push más reciente
          setBannerPush(list[0]);
        }
      });
      return () => unsubscribe();
    } catch (e) {
      console.log('Modo preview / sin conexión directa a Firestore');
    }
  }, []);

  // Escuchar estatus del pedido activo en vivo si existe
  useEffect(() => {
    if (!activeOrder?.id) return;
    try {
      const orderRef = doc(db, 'isaya_orders', activeOrder.id);
      const unsubscribe = onSnapshot(orderRef, (docSnap) => {
        if (docSnap.exists()) {
          setActiveOrder((prev: any) => ({ ...prev, ...docSnap.data() }));
        }
      });
      return () => unsubscribe();
    } catch (e) {
      console.log('Offline tracker listener');
    }
  }, [activeOrder?.id]);

  const deliveryFee = deliveryMode === 'DELIVERY' ? (selectedZone ? selectedZone.fee : 0.0) : 0.0;
  const subtotal = useMemo(() => cart.reduce((sum, item) => sum + (item.price * item.quantity), 0), [cart]);
  const total = subtotal + (cart.length > 0 ? deliveryFee : 0);

  const filteredProducts = useMemo(() => {
    if (selectedCategory === 'Todos') return MENU_PRODUCTS;
    return MENU_PRODUCTS.filter(p => p.category === selectedCategory);
  }, [selectedCategory]);

  const [activeAdIndex, setActiveAdIndex] = useState(0);

  // Estados de Personalización del Modal
  const [prepStyle, setPrepStyle] = useState('Tempurizado');
  const [toppingCamaron, setToppingCamaron] = useState(false);
  const [toppingPollo, setToppingPollo] = useState(false);
  const [dinamitaFria, setDinamitaFria] = useState(false);
  const [dinamitaTempura, setDinamitaTempura] = useState(false);
  const [extraGlup, setExtraGlup] = useState(false);
  const [specialNotes, setSpecialNotes] = useState('');

  const featuredAds = useMemo(() => {
    const isaya = MENU_PRODUCTS.find(p => p.id === 'prod_combo_sushi_fest') || MENU_PRODUCTS[0];
    const tempura = MENU_PRODUCTS.find(p => p.id === 'prod_tempura_roll') || MENU_PRODUCTS[1];
    return [isaya, tempura];
  }, []);

  const activeAd = featuredAds[activeAdIndex % featuredAds.length];

  // Cálculo del precio de toppings Dinamita ($2 individual, $3 ambas)
  const dinamitaPrice = useMemo(() => {
    if (dinamitaFria && dinamitaTempura) return 3.0;
    if (dinamitaFria || dinamitaTempura) return 2.0;
    return 0.0;
  }, [dinamitaFria, dinamitaTempura]);

  const customizationTotal = useMemo(() => {
    if (!selectedProduct) return 0;
    let sum = selectedProduct.price;
    if (toppingCamaron) sum += 3.0;
    if (toppingPollo) sum += 2.0;
    sum += dinamitaPrice;
    if (extraGlup) sum += 1.50;
    return sum;
  }, [selectedProduct, toppingCamaron, toppingPollo, dinamitaPrice, extraGlup]);

  const openProductCustomization = (product: any) => {
    setSelectedProduct(product);
    setPrepStyle(product.id === 'prod_combo_chic' ? 'Frío' : 'Tempurizado');
    setToppingCamaron(false);
    setToppingPollo(false);
    setDinamitaFria(false);
    setDinamitaTempura(false);
    setExtraGlup(false);
    setSpecialNotes('');
  };

  const handleAddToCart = (product: any, qty: number) => {
    const extras: string[] = [];
    if (toppingCamaron) extras.push('Topping Camarón (+$3)');
    if (toppingPollo) extras.push('Topping Pollo Crunchy (+$2)');
    if (dinamitaFria && dinamitaTempura) extras.push('Dinamita Fría + Tempura (+$3)');
    else if (dinamitaFria) extras.push('Dinamita Fría (+$2)');
    else if (dinamitaTempura) extras.push('Dinamita Tempura (+$2)');
    if (extraGlup) extras.push('Glup Negro 1L (+$1.50)');

    const cartEntry = {
      ...product,
      price: customizationTotal,
      quantity: qty,
      prepStyle,
      extras,
      notes: specialNotes.trim()
    };

    setCart(prev => [...prev, cartEntry]);
    setSelectedProduct(null);
  };

  const handleConfirmOrder = async () => {
    if (!customerName || !customerPhone || referenceDigits.length < 4) {
      Alert.alert('Datos Incompletos', 'Por favor completa nombre, teléfono y los 4 dígitos de referencia.');
      return;
    }
    if (deliveryMode === 'DELIVERY' && !selectedZone) {
      Alert.alert('Zona de Envío requerida', 'Por favor selecciona tu Zona de Envío para calcular el costo de flete automático.');
      return;
    }
    if (deliveryMode === 'DELIVERY' && !customerAddress) {
      Alert.alert('Dirección Requerida', 'Por favor ingresa tu dirección exacta de entrega.');
      return;
    }
    setIsSubmitting(true);
    const orderNumber = `ISY-${Math.floor(1000 + Math.random() * 9000)}`;
    const newOrderData = {
      orderNumber,
      items: cart,
      subtotal,
      deliveryFee,
      total,
      deliveryMode,
      customer: {
        name: customerName,
        phone: customerPhone,
        address: customerAddress,
        deliveryZone: selectedZone
      },
      payment: { bank: 'Banesco (0134)', reference: referenceDigits },
      status: 'PENDING_PAYMENT',
      statusLabel: 'Pedido Recibido',
      timestamp: Date.now()
    };

    try {
      const docRef = await addDoc(collection(db, 'isaya_orders'), newOrderData);
      const createdOrder = { id: docRef.id, ...newOrderData };
      setActiveOrder(createdOrder);
    } catch (e) {
      // Fallback local
      const fallbackOrder = { id: `local_${Date.now()}`, ...newOrderData };
      setActiveOrder(fallbackOrder);
    }

    setCart([]);
    setIsSubmitting(false);
    setIsCheckoutVisible(false);
    setActiveTab('TRACKER');
    Alert.alert('¡Pedido Enviado!', `Tu orden #${orderNumber} fue recibida. La cocina verificará tu comprobante de inmediato.`);
  };

  const openWhatsAppHelp = () => {
    const orderCode = activeOrder?.orderNumber || 'CONSULTA';
    const text = encodeURIComponent(`Hola Isaya Sushi! Quisiera consultar el estatus de mi orden #${orderCode}.`);
    Linking.openURL(`https://wa.me/584141234567?text=${text}`);
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={COLORS.black} />

      {/* Header Superior */}
      <View style={styles.header}>
        <View>
          <Text style={styles.brandTitle}>ISAYA SUSHI</Text>
          <Text style={styles.brandSubtitle}>Rolls Nikkei • Delivery Gourmet</Text>
        </View>
        <TouchableOpacity
          style={styles.bellButton}
          onPress={() => setIsNotificationsVisible(true)}
        >
          <Text style={styles.bellIcon}>🔔</Text>
          {notifications.length > 0 && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{notifications.length}</Text>
            </View>
          )}
        </TouchableOpacity>
      </View>

      {/* Banner de Notificación Push Recibida */}
      {bannerPush && (
        <View style={styles.pushBanner}>
          <Text style={styles.pushBannerTitle}>{bannerPush.title}</Text>
          <Text style={styles.pushBannerBody}>{bannerPush.body}</Text>
          <TouchableOpacity onPress={() => setBannerPush(null)} style={styles.pushCloseBtn}>
            <Text style={styles.pushCloseText}>✕</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* Switch Delivery / Retiro */}
      <View style={styles.modeSwitchContainer}>
        <TouchableOpacity
          style={[styles.modeTab, deliveryMode === 'DELIVERY' && styles.modeTabActive]}
          onPress={() => setDeliveryMode('DELIVERY')}
        >
          <Text style={[styles.modeTabText, deliveryMode === 'DELIVERY' && styles.modeTabTextActive]}>
            🛵 Delivery (+$2.50)
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.modeTab, deliveryMode === 'PICKUP' && styles.modeTabActive]}
          onPress={() => setDeliveryMode('PICKUP')}
        >
          <Text style={[styles.modeTabText, deliveryMode === 'PICKUP' && styles.modeTabTextActive]}>
            🛍️ Retiro en Tienda (Gratis)
          </Text>
        </TouchableOpacity>
      </View>

      {/* Contenido Principal según Tab */}
      {activeTab === 'MENU' ? (
        <ScrollView 
          style={{ flex: 1 }} 
          contentContainerStyle={{ paddingBottom: 120 }}
          showsVerticalScrollIndicator={false}
        >
          {/* Selector de Categorías */}
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.categoriesBar}>
            {CATEGORIES.map(cat => (
              <TouchableOpacity
                key={cat}
                style={[styles.catChip, selectedCategory === cat && styles.catChipActive]}
                onPress={() => setSelectedCategory(cat)}
              >
                <Text style={[styles.catChipText, selectedCategory === cat && styles.catChipTextActive]}>
                  {cat}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>

          {/* Anuncio Destacado Rotativo: Combo Isaya ($12) <-> Tempura Roll ($6) */}
          {activeAd && (
            <View style={styles.adBannerCard}>
              <View style={styles.adBannerHeader}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                  <View style={styles.adTag}>
                    <Text style={styles.adTagText}>ANUNCIO DESTACADO</Text>
                  </View>
                  <Text style={styles.adHeaderText}>
                    {activeAd.id === 'prod_combo_sushi_fest' ? '⭐ COMBO ISAYA' : '🍤 TEMPURA ROLL'}
                  </Text>
                </View>

                {/* Botón para rotar/cambiar el anuncio */}
                <TouchableOpacity
                  style={styles.adRotateBtn}
                  onPress={() => setActiveAdIndex(prev => (prev + 1) % featuredAds.length)}
                >
                  <Text style={styles.adRotateBtnText}>Cambiar Anuncio 🔄</Text>
                </TouchableOpacity>
              </View>

              {/* Selector de pestañas directas en el anuncio */}
              <View style={styles.adTabsRow}>
                {featuredAds.map((ad, idx) => {
                  const isSel = (activeAdIndex % featuredAds.length) === idx;
                  return (
                    <TouchableOpacity
                      key={ad.id}
                      style={[styles.adTabItem, isSel && styles.adTabItemActive]}
                      onPress={() => setActiveAdIndex(idx)}
                    >
                      <Text style={[styles.adTabText, isSel && styles.adTabTextActive]}>
                        {ad.id === 'prod_combo_sushi_fest' ? '🍱 Combo Isaya ($12)' : '🍤 Tempura Roll ($6)'}
                      </Text>
                    </TouchableOpacity>
                  );
                })}
              </View>

              <TouchableOpacity
                style={styles.adBannerBody}
                onPress={() => openProductCustomization(activeAd)}
                activeOpacity={0.85}
              >
                <View style={{ flex: 1 }}>
                  <Text style={styles.adComboTitle}>{activeAd.name}</Text>
                  <Text style={styles.adComboDesc} numberOfLines={2}>{activeAd.description}</Text>
                  <View style={styles.adPriceRow}>
                    <Text style={styles.adPriceText}>${activeAd.price.toFixed(2)} USD</Text>
                    {activeAd.originalPrice && (
                      <Text style={styles.adOriginalPriceText}>${activeAd.originalPrice.toFixed(2)} USD</Text>
                    )}
                  </View>
                </View>
                <View style={styles.adActionBtn}>
                  <Text style={styles.adActionText}>¡Pedir Ahora! 🍱</Text>
                </View>
              </TouchableOpacity>
            </View>
          )}

          {/* Relleno Isaya Hallmark Info Banner */}
          <View style={styles.hallmarkBanner}>
            <Text style={styles.hallmarkTitle}>🥢 Relleno Único Isaya en todos los combos y rollos:</Text>
            <Text style={styles.hallmarkText}>
              Pollo, camarón, surimi (cangrejo), langostinos, plátano frito, aguacate y queso crema • 100% Cocinado y Crujiente.
            </Text>
          </View>

          {/* Lista de Rolls y Productos */}
          <View style={styles.listContent}>
            {filteredProducts.map(item => (
              <TouchableOpacity
                key={item.id}
                style={styles.productCard}
                onPress={() => openProductCustomization(item)}
              >
                <View style={styles.productInfo}>
                  <View style={styles.badgeRow}>
                    {item.badge && <Text style={styles.popularBadge}>{item.badge}</Text>}
                    {item.pieces && <Text style={styles.piecesBadge}>{item.pieces} Piezas</Text>}
                  </View>
                  <Text style={styles.productName}>{item.name}</Text>
                  <Text style={styles.productDesc} numberOfLines={2}>{item.description}</Text>
                  <Text style={styles.productPrice}>${item.price.toFixed(2)} USD</Text>
                </View>
                <TouchableOpacity
                  style={styles.addButton}
                  onPress={() => openProductCustomization(item)}
                >
                  <Text style={styles.addButtonText}>+ Personalizar</Text>
                </TouchableOpacity>
              </TouchableOpacity>
            ))}
          </View>
        </ScrollView>
      ) : (
        /* Vista de Seguimiento en Tiempo Real */
        <ScrollView style={styles.trackerContainer}>
          <View style={styles.trackerCard}>
            <Text style={styles.trackerHeader}>SEGUIMIENTO DE ORDEN</Text>
            <Text style={styles.trackerOrderNumber}>#{activeOrder?.orderNumber || 'ISY-8429'}</Text>

            <View style={styles.stepsBox}>
              {[
                { label: '1. Pedido Recibido', desc: 'Comprobante en verificación' },
                { label: '2. Pago Confirmado', desc: 'Verificado por el restaurante' },
                { label: '3. En Cocina', desc: 'Chefs preparando tus rolls' },
                { label: '4. En Camino / Listo', desc: 'Rumbo a tu dirección' }
              ].map((step, idx) => (
                <View key={idx} style={styles.stepItem}>
                  <View style={[styles.stepDot, idx <= 1 && styles.stepDotActive]}>
                    <Text style={styles.stepDotText}>{idx + 1}</Text>
                  </View>
                  <View style={{ marginLeft: 12 }}>
                    <Text style={styles.stepLabel}>{step.label}</Text>
                    <Text style={styles.stepDesc}>{step.desc}</Text>
                  </View>
                </View>
              ))}
            </View>

            <TouchableOpacity style={styles.whatsappButton} onPress={openWhatsAppHelp}>
              <Text style={styles.whatsappButtonText}>💬 Contactar Soporte WhatsApp</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.backToMenuBtn} onPress={() => setActiveTab('MENU')}>
              <Text style={styles.backToMenuText}>Volver al Menú</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      )}

      {/* Barra de Carrito Flotante */}
      {cart.length > 0 && activeTab === 'MENU' && (
        <View style={styles.floatingCartBar}>
          <View>
            <Text style={styles.cartItemsCount}>{cart.reduce((s, i) => s + i.quantity, 0)} Items en Carrito</Text>
            <Text style={styles.cartTotalText}>Total: ${total.toFixed(2)} USD</Text>
          </View>
          <TouchableOpacity
            style={styles.checkoutBtn}
            onPress={() => setIsCheckoutVisible(true)}
          >
            <Text style={styles.checkoutBtnText}>Ordenar Ahora →</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* Botones de Navegación Inferior */}
      <View style={styles.bottomNav}>
        <TouchableOpacity
          style={[styles.navItem, activeTab === 'MENU' && styles.navItemActive]}
          onPress={() => setActiveTab('MENU')}
        >
          <Text style={styles.navIcon}>🍣</Text>
          <Text style={[styles.navLabel, activeTab === 'MENU' && styles.navLabelActive]}>Menú</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.navItem, activeTab === 'TRACKER' && styles.navItemActive]}
          onPress={() => setActiveTab('TRACKER')}
        >
          <Text style={styles.navIcon}>📍</Text>
          <Text style={[styles.navLabel, activeTab === 'TRACKER' && styles.navLabelActive]}>Mi Pedido</Text>
        </TouchableOpacity>
      </View>

      {/* Modal de Personalización de Producto */}
      <Modal visible={!!selectedProduct} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <ScrollView style={styles.checkoutModalScroll} contentContainerStyle={styles.modalContent}>
            <Text style={styles.modalTitle}>{selectedProduct?.name}</Text>
            <Text style={styles.modalDesc}>{selectedProduct?.description}</Text>
            <Text style={styles.modalPrice}>Base: ${selectedProduct?.price.toFixed(2)} USD</Text>

            {/* Selector de Preparación: Tempurizado vs Frío */}
            <Text style={styles.modalSubheading}>Estilo de Preparación:</Text>
            <View style={styles.prepRow}>
              <TouchableOpacity
                style={[styles.prepOption, prepStyle === 'Tempurizado' && styles.prepOptionActive]}
                onPress={() => setPrepStyle('Tempurizado')}
              >
                <Text style={[styles.prepText, prepStyle === 'Tempurizado' && styles.prepTextActive]}>
                  🔥 Tempurizado (Caliente & Crocante)
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.prepOption, prepStyle === 'Frío' && styles.prepOptionActive]}
                onPress={() => setPrepStyle('Frío')}
              >
                <Text style={[styles.prepText, prepStyle === 'Frío' && styles.prepTextActive]}>
                  ❄️ Frío (100% Cocinado)
                </Text>
              </TouchableOpacity>
            </View>

            {/* Toppings Extra */}
            <Text style={styles.modalSubheading}>Toppings Extra:</Text>
            <TouchableOpacity
              style={[styles.toppingOption, toppingCamaron && styles.toppingOptionActive]}
              onPress={() => setToppingCamaron(!toppingCamaron)}
            >
              <Text style={styles.toppingLabel}>🍤 Topping de Camarones Apanados</Text>
              <Text style={styles.toppingPrice}>+$3.00 USD</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.toppingOption, toppingPollo && styles.toppingOptionActive]}
              onPress={() => setToppingPollo(!toppingPollo)}
            >
              <Text style={styles.toppingLabel}>🍗 Topping de Pollo Crunchy's</Text>
              <Text style={styles.toppingPrice}>+$2.00 USD</Text>
            </TouchableOpacity>

            {/* Mini Interfaz Ensalada Dinamita */}
            <Text style={styles.modalSubheading}>Topping Ensalada Dinamita:</Text>
            <Text style={styles.modalNotesHint}>$2 individual • $3 llevando ambas</Text>
            <View style={styles.dinamitaRow}>
              <TouchableOpacity
                style={[styles.dinamitaBtn, dinamitaFria && styles.dinamitaBtnActive]}
                onPress={() => setDinamitaFria(!dinamitaFria)}
              >
                <Text style={[styles.dinamitaText, dinamitaFria && styles.dinamitaTextActive]}>
                  {dinamitaFria ? '✓ ' : '+ '}Dinamita Fría ($2)
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.dinamitaBtn, dinamitaTempura && styles.dinamitaBtnActive]}
                onPress={() => setDinamitaTempura(!dinamitaTempura)}
              >
                <Text style={[styles.dinamitaText, dinamitaTempura && styles.dinamitaTextActive]}>
                  {dinamitaTempura ? '✓ ' : '+ '}Dinamita Tempura ($2)
                </Text>
              </TouchableOpacity>
            </View>
            {dinamitaFria && dinamitaTempura && (
              <Text style={styles.dinamitaPromoText}>🎉 ¡Combo Dinamita Doble aplicado por solo $3 USD!</Text>
            )}

            {/* Agregado Bebida Glup Negro 1L */}
            <Text style={styles.modalSubheading}>Bebida Agregada:</Text>
            <TouchableOpacity
              style={[styles.toppingOption, extraGlup && styles.toppingOptionActive]}
              onPress={() => setExtraGlup(!extraGlup)}
            >
              <Text style={styles.toppingLabel}>🥤 Glup Negro 1 Litro (Bien Fría)</Text>
              <Text style={styles.toppingPrice}>+$1.50 USD</Text>
            </TouchableOpacity>

            {/* Bandeja de Texto: Instrucciones Especiales & Alergias */}
            <Text style={styles.modalSubheading}>Instrucciones Especiales y Alergias:</Text>
            <Text style={styles.modalNotesHint}>Especifique si desea sin salsas, extra salsa, palitos o alguna alergia:</Text>
            <TextInput
              style={[styles.input, { height: 60, marginTop: 6 }]}
              placeholder="Ej. sin salsa teriyaki, más palitos, alergia a mariscos..."
              placeholderTextColor={COLORS.textMuted}
              value={specialNotes}
              onChangeText={setSpecialNotes}
              multiline
            />

            {/* Quick chips para instrucciones */}
            <View style={styles.chipsRow}>
              {['Sin Salsa', 'Extra Salsa', 'Más Palitos', 'Menos Palitos', 'Alergia'].map(chip => (
                <TouchableOpacity
                  key={chip}
                  style={styles.suggestionChip}
                  onPress={() => {
                    const separator = specialNotes.trim().length > 0 ? ', ' : '';
                    setSpecialNotes(prev => `${prev}${separator}${chip}`);
                  }}
                >
                  <Text style={styles.suggestionChipText}>+ {chip}</Text>
                </TouchableOpacity>
              ))}
            </View>

            {/* Botones de Acción */}
            <View style={styles.modalButtonsRow}>
              <TouchableOpacity style={styles.cancelModalBtn} onPress={() => setSelectedProduct(null)}>
                <Text style={styles.cancelModalText}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.addModalBtn}
                onPress={() => handleAddToCart(selectedProduct, 1)}
              >
                <Text style={styles.addModalText}>
                  Agregar (${customizationTotal.toFixed(2)} USD)
                </Text>
              </TouchableOpacity>
            </View>
          </ScrollView>
        </View>
      </Modal>

      {/* Modal de Checkout Pago Móvil Banesco */}
      <Modal visible={isCheckoutVisible} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <ScrollView style={styles.checkoutModalScroll} contentContainerStyle={styles.checkoutModalContent}>
            <Text style={styles.checkoutTitle}>CHECKOUT PAGO MÓVIL</Text>

            {/* Datos Bancarios Banesco */}
            <View style={styles.bankCard}>
              <Text style={styles.bankTitle}>Datos Pago Móvil Banesco:</Text>
              <Text style={styles.bankRow}>• Banco: Banesco (0134)</Text>
              <Text style={styles.bankRow}>• RIF: J-40892314-1</Text>
              <Text style={styles.bankRow}>• Teléfono: 0414-1234567</Text>
              <Text style={styles.bankRow}>• Titular: Isaya Sushi Gourmet C.A.</Text>
            </View>

            {/* Formulario de Entrega */}
            <TextInput
              style={styles.input}
              placeholder="Nombre y Apellido"
              placeholderTextColor={COLORS.textMuted}
              value={customerName}
              onChangeText={setCustomerName}
            />
            <TextInput
              style={styles.input}
              placeholder="Teléfono WhatsApp (Ej. 0414-1234567)"
              placeholderTextColor={COLORS.textMuted}
              value={customerPhone}
              onChangeText={setCustomerPhone}
              keyboardType="phone-pad"
            />
            {deliveryMode === 'DELIVERY' && (
              <>
                {/* Selector de Zona de Envío */}
                <TouchableOpacity
                  style={styles.zoneSelectorBtn}
                  onPress={() => setIsZonePickerVisible(true)}
                >
                  <View style={{ flex: 1 }}>
                    <Text style={styles.zoneSelectorLabel}>Zona de Envío (Maracay / Aragua) *</Text>
                    <Text style={[styles.zoneSelectorValue, !selectedZone && { color: COLORS.textMuted }]}>
                      {selectedZone ? `📍 ${selectedZone.name} - $${selectedZone.fee.toFixed(2)} USD` : 'Toca para seleccionar tu sector (73 zonas)'}
                    </Text>
                  </View>
                  <Text style={styles.zoneSelectorArrow}>▼</Text>
                </TouchableOpacity>

                <TextInput
                  style={[styles.input, { height: 60 }]}
                  placeholder="Dirección exacta de entrega y punto de referencia"
                  placeholderTextColor={COLORS.textMuted}
                  value={customerAddress}
                  onChangeText={setCustomerAddress}
                  multiline
                />
              </>
            )}
            <TextInput
              style={styles.input}
              placeholder="Últimos 4 dígitos de Referencia Pago Móvil"
              placeholderTextColor={COLORS.textMuted}
              value={referenceDigits}
              onChangeText={setReferenceDigits}
              keyboardType="numeric"
              maxLength={4}
            />

            {/* Resumen de Montos */}
            <View style={styles.summaryBox}>
              <Text style={styles.summaryRow}>Subtotal: ${subtotal.toFixed(2)} USD</Text>
              <Text style={styles.summaryRow}>
                {deliveryMode === 'DELIVERY' 
                  ? `Delivery (${selectedZone ? selectedZone.name : 'Selecciona zona'}): $${deliveryFee.toFixed(2)} USD`
                  : 'Retiro en Local: GRATIS ($0.00 USD)'}
              </Text>
              <Text style={styles.summaryTotal}>Total a Pagar: ${total.toFixed(2)} USD</Text>
            </View>

            <TouchableOpacity
              style={styles.submitOrderBtn}
              onPress={handleConfirmOrder}
              disabled={isSubmitting}
            >
              <Text style={styles.submitOrderText}>
                {isSubmitting ? 'Procesando...' : 'Confirmar y Enviar Pedido'}
              </Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.cancelCheckoutBtn} onPress={() => setIsCheckoutVisible(false)}>
              <Text style={styles.cancelCheckoutText}>Cerrar</Text>
            </TouchableOpacity>
          </ScrollView>
        </View>
      </Modal>

      {/* Modal Selector de Zona de Envío */}
      <Modal visible={isZonePickerVisible} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.zoneModalContent}>
            <View style={styles.zoneModalHeader}>
              <Text style={styles.zoneModalTitle}>Selecciona tu Zona de Envío</Text>
              <TouchableOpacity onPress={() => setIsZonePickerVisible(false)}>
                <Text style={styles.closeNotifText}>✕</Text>
              </TouchableOpacity>
            </View>
            <TextInput
              style={styles.zoneSearchInput}
              placeholder="Buscar sector (ej. Cagua, Delicias, Base Aragua...)"
              placeholderTextColor={COLORS.textMuted}
              value={zoneSearch}
              onChangeText={setZoneSearch}
            />
            <FlatList
              data={MARACAY_DELIVERY_ZONES.filter(z => 
                !zoneSearch.trim() || z.name.toLowerCase().includes(zoneSearch.toLowerCase().trim())
              )}
              keyExtractor={item => item.name}
              renderItem={({ item }) => {
                const isSelected = selectedZone?.name === item.name;
                return (
                  <TouchableOpacity
                    style={[styles.zoneItem, isSelected && styles.zoneItemActive]}
                    onPress={() => {
                      setSelectedZone(item);
                      setIsZonePickerVisible(false);
                    }}
                  >
                    <Text style={[styles.zoneItemName, isSelected && styles.zoneItemNameActive]}>
                      {item.name}
                    </Text>
                    <View style={[styles.zoneItemBadge, isSelected && styles.zoneItemBadgeActive]}>
                      <Text style={[styles.zoneItemPrice, isSelected && styles.zoneItemPriceActive]}>
                        ${item.fee.toFixed(2)} USD
                      </Text>
                    </View>
                  </TouchableOpacity>
                );
              }}
            />
          </View>
        </View>
      </Modal>

      {/* Modal Historial Notificaciones Push */}
      <Modal visible={isNotificationsVisible} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.notifModalContent}>
            <Text style={styles.notifModalTitle}>Notificaciones Push Isaya</Text>
            <FlatList
              data={notifications}
              keyExtractor={item => item.id}
              ListEmptyComponent={<Text style={{ color: COLORS.textMuted }}>No hay notificaciones recientes.</Text>}
              renderItem={({ item }) => (
                <View style={styles.notifCard}>
                  <Text style={styles.notifItemTitle}>{item.title}</Text>
                  <Text style={styles.notifItemBody}>{item.body}</Text>
                  {item.promoTag && <Text style={styles.notifItemTag}>Cupón: {item.promoTag}</Text>}
                </View>
              )}
            />
            <TouchableOpacity style={styles.closeNotifBtn} onPress={() => setIsNotificationsVisible(false)}>
              <Text style={styles.closeNotifText}>Cerrar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.black },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: COLORS.surface,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border
  },
  brandTitle: { color: COLORS.gold, fontSize: 18, fontWeight: '900', letterSpacing: 2 },
  brandSubtitle: { color: COLORS.textSecondary, fontSize: 11, marginTop: 2 },
  bellButton: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: COLORS.cardElevated,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.goldContainer
  },
  bellIcon: { fontSize: 16 },
  badge: {
    position: 'absolute',
    top: -4,
    right: -4,
    backgroundColor: COLORS.gold,
    borderRadius: 9,
    paddingHorizontal: 5,
    paddingVertical: 1
  },
  badgeText: { color: COLORS.black, fontSize: 9, fontWeight: 'bold' },
  pushBanner: {
    backgroundColor: COLORS.gold,
    padding: 12,
    marginHorizontal: 16,
    marginTop: 8,
    borderRadius: 8,
    position: 'relative'
  },
  pushBannerTitle: { color: COLORS.black, fontWeight: 'bold', fontSize: 12 },
  pushBannerBody: { color: COLORS.black, fontSize: 11, marginTop: 2 },
  pushCloseBtn: { position: 'absolute', top: 6, right: 8 },
  pushCloseText: { color: COLORS.black, fontSize: 14, fontWeight: 'bold' },
  modeSwitchContainer: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: COLORS.surface,
    gap: 8
  },
  modeTab: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 8,
    alignItems: 'center',
    backgroundColor: COLORS.card
  },
  modeTabActive: {
    backgroundColor: COLORS.goldContainer,
    borderWidth: 1,
    borderColor: COLORS.gold
  },
  modeTabText: { color: COLORS.textSecondary, fontSize: 11, fontWeight: '600' },
  modeTabTextActive: { color: COLORS.gold, fontWeight: 'bold' },
  categoriesBar: { paddingHorizontal: 16, paddingVertical: 10, maxHeight: 54 },
  catChip: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 20,
    backgroundColor: COLORS.card,
    marginRight: 8,
    borderWidth: 1,
    borderColor: COLORS.border
  },
  catChipActive: { backgroundColor: COLORS.gold, borderColor: COLORS.gold },
  catChipText: { color: COLORS.textSecondary, fontSize: 12 },
  catChipTextActive: { color: COLORS.black, fontWeight: 'bold' },
  listContent: { paddingHorizontal: 16, paddingTop: 6, paddingBottom: 120 },
  productCard: {
    flexDirection: 'row',
    backgroundColor: COLORS.card,
    borderRadius: 12,
    padding: 14,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: COLORS.border,
    alignItems: 'center'
  },
  productInfo: { flex: 1 },
  badgeRow: { flexDirection: 'row', gap: 6, marginBottom: 4 },
  popularBadge: {
    backgroundColor: COLORS.red,
    color: '#FFF',
    fontSize: 9,
    fontWeight: 'bold',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4
  },
  piecesBadge: {
    backgroundColor: COLORS.goldContainer,
    color: COLORS.gold,
    fontSize: 9,
    fontWeight: 'bold',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4
  },
  productName: { color: COLORS.textPrimary, fontSize: 14, fontWeight: 'bold' },
  productDesc: { color: COLORS.textSecondary, fontSize: 11, marginTop: 4 },
  productPrice: { color: COLORS.gold, fontSize: 13, fontWeight: 'bold', marginTop: 6 },
  addButton: {
    backgroundColor: COLORS.cardElevated,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: COLORS.gold
  },
  addButtonText: { color: COLORS.gold, fontSize: 11, fontWeight: 'bold' },
  floatingCartBar: {
    position: 'absolute',
    bottom: 60,
    left: 16,
    right: 16,
    backgroundColor: COLORS.cardElevated,
    borderRadius: 14,
    padding: 14,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.gold,
    elevation: 8
  },
  cartItemsCount: { color: COLORS.textSecondary, fontSize: 11 },
  cartTotalText: { color: COLORS.gold, fontSize: 14, fontWeight: 'bold' },
  checkoutBtn: {
    backgroundColor: COLORS.red,
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8
  },
  checkoutBtnText: { color: '#FFF', fontWeight: 'bold', fontSize: 12 },
  bottomNav: {
    flexDirection: 'row',
    height: 56,
    backgroundColor: COLORS.surface,
    borderTopWidth: 1,
    borderTopColor: COLORS.border,
    justifyContent: 'space-around',
    alignItems: 'center'
  },
  navItem: { alignItems: 'center' },
  navItemActive: {},
  navIcon: { fontSize: 16 },
  navLabel: { color: COLORS.textSecondary, fontSize: 10, marginTop: 2 },
  navLabelActive: { color: COLORS.gold, fontWeight: 'bold' },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.85)',
    justifyContent: 'center',
    padding: 20
  },
  modalContent: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: COLORS.gold
  },
  modalTitle: { color: COLORS.gold, fontSize: 16, fontWeight: 'bold' },
  modalDesc: { color: COLORS.textSecondary, fontSize: 12, marginTop: 6 },
  modalPrice: { color: COLORS.textPrimary, fontSize: 14, fontWeight: 'bold', marginTop: 10 },
  modalSubheading: { color: COLORS.textPrimary, fontSize: 12, fontWeight: 'bold', marginTop: 12 },
  modalNotesHint: { color: COLORS.textMuted, fontSize: 11, marginTop: 4 },
  modalButtonsRow: { flexDirection: 'row', justifyContent: 'flex-end', gap: 10, marginTop: 20 },
  cancelModalBtn: { padding: 10 },
  cancelModalText: { color: COLORS.textSecondary, fontSize: 12 },
  addModalBtn: { backgroundColor: COLORS.gold, paddingHorizontal: 16, paddingVertical: 10, borderRadius: 8 },
  addModalText: { color: COLORS.black, fontWeight: 'bold', fontSize: 12 },
  checkoutModalScroll: { maxHeight: '90%' },
  checkoutModalContent: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: COLORS.border
  },
  checkoutTitle: { color: COLORS.gold, fontSize: 16, fontWeight: '900', textAlign: 'center', marginBottom: 14 },
  bankCard: {
    backgroundColor: COLORS.card,
    borderRadius: 10,
    padding: 12,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: COLORS.goldContainer
  },
  bankTitle: { color: COLORS.gold, fontWeight: 'bold', fontSize: 12, marginBottom: 4 },
  bankRow: { color: COLORS.textPrimary, fontSize: 11, marginVertical: 1 },
  input: {
    backgroundColor: COLORS.cardElevated,
    color: COLORS.textPrimary,
    borderRadius: 8,
    padding: 10,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: COLORS.border,
    fontSize: 12
  },
  summaryBox: { paddingVertical: 10, borderTopWidth: 1, borderTopColor: COLORS.border, marginTop: 6 },
  summaryRow: { color: COLORS.textSecondary, fontSize: 12, marginVertical: 2 },
  summaryTotal: { color: COLORS.gold, fontSize: 14, fontWeight: 'bold', marginTop: 4 },
  submitOrderBtn: {
    backgroundColor: COLORS.red,
    padding: 14,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 12
  },
  submitOrderText: { color: '#FFF', fontWeight: 'bold', fontSize: 13 },
  cancelCheckoutBtn: { padding: 10, alignItems: 'center', marginTop: 6 },
  cancelCheckoutText: { color: COLORS.textMuted, fontSize: 12 },
  trackerContainer: { flex: 1, padding: 16 },
  trackerCard: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: COLORS.border
  },
  trackerHeader: { color: COLORS.textSecondary, fontSize: 11, fontWeight: 'bold', textAlign: 'center' },
  trackerOrderNumber: { color: COLORS.gold, fontSize: 20, fontWeight: '900', textAlign: 'center', marginVertical: 8 },
  stepsBox: { marginVertical: 16 },
  stepItem: { flexDirection: 'row', alignItems: 'center', marginBottom: 14 },
  stepDot: {
    width: 26,
    height: 26,
    borderRadius: 13,
    backgroundColor: COLORS.cardElevated,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.border
  },
  stepDotActive: { backgroundColor: COLORS.gold, borderColor: COLORS.gold },
  stepDotText: { color: COLORS.black, fontSize: 11, fontWeight: 'bold' },
  stepLabel: { color: COLORS.textPrimary, fontSize: 12, fontWeight: 'bold' },
  stepDesc: { color: COLORS.textSecondary, fontSize: 10 },
  whatsappButton: {
    backgroundColor: '#25D366',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 10
  },
  whatsappButtonText: { color: '#FFF', fontWeight: 'bold', fontSize: 12 },
  backToMenuBtn: { padding: 12, alignItems: 'center', marginTop: 8 },
  backToMenuText: { color: COLORS.gold, fontSize: 12 },
  notifModalContent: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: COLORS.gold,
    maxHeight: 400
  },
  notifModalTitle: { color: COLORS.gold, fontSize: 15, fontWeight: 'bold', marginBottom: 12 },
  notifCard: {
    backgroundColor: COLORS.card,
    borderRadius: 8,
    padding: 10,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: COLORS.border
  },
  notifItemTitle: { color: COLORS.gold, fontSize: 12, fontWeight: 'bold' },
  notifItemBody: { color: COLORS.textPrimary, fontSize: 11, marginTop: 2 },
  notifItemTag: { color: COLORS.redLight, fontSize: 9, fontWeight: 'bold', marginTop: 4 },
  closeNotifBtn: { marginTop: 10, padding: 8, alignItems: 'center' },
  closeNotifText: { color: COLORS.textSecondary, fontSize: 12 },
  // Ad Banner Styles
  adBannerCard: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    marginHorizontal: 16,
    marginVertical: 10,
    borderWidth: 1.5,
    borderColor: COLORS.gold,
    overflow: 'hidden'
  },
  adBannerHeader: {
    backgroundColor: COLORS.cardElevated,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border
  },
  adTag: {
    backgroundColor: COLORS.gold,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4
  },
  adTagText: { color: COLORS.black, fontSize: 9, fontWeight: '900' },
  adHeaderText: { color: COLORS.textPrimary, fontSize: 11, fontWeight: 'bold' },
  adSavingsTag: {
    backgroundColor: 'rgba(200, 16, 46, 0.25)',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4
  },
  adSavingsText: { color: COLORS.redLight, fontSize: 9, fontWeight: 'bold' },
  adBannerBody: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 14,
    justifyContent: 'space-between'
  },
  adComboTitle: { color: COLORS.gold, fontSize: 15, fontWeight: 'bold' },
  adComboDesc: { color: COLORS.textSecondary, fontSize: 11, marginVertical: 4 },
  adPriceRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  adPriceText: { color: COLORS.gold, fontSize: 17, fontWeight: '900' },
  adOriginalPriceText: { color: COLORS.textMuted, fontSize: 12, textDecorationLine: 'line-through' },
  adActionBtn: {
    backgroundColor: COLORS.gold,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    marginLeft: 10
  },
  adActionText: { color: COLORS.black, fontSize: 12, fontWeight: 'bold' },
  adRotateBtn: {
    backgroundColor: 'rgba(212, 175, 55, 0.2)',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: COLORS.gold
  },
  adRotateBtnText: { color: COLORS.goldLight, fontSize: 10, fontWeight: 'bold' },
  adTabsRow: {
    flexDirection: 'row',
    backgroundColor: COLORS.card,
    paddingHorizontal: 8,
    paddingVertical: 4,
    gap: 6
  },
  adTabItem: {
    flex: 1,
    paddingVertical: 6,
    borderRadius: 6,
    alignItems: 'center',
    backgroundColor: COLORS.cardElevated
  },
  adTabItemActive: { backgroundColor: COLORS.gold },
  adTabText: { color: COLORS.textSecondary, fontSize: 11, fontWeight: '600' },
  adTabTextActive: { color: COLORS.black, fontWeight: '900' },
  hallmarkBanner: {
    backgroundColor: COLORS.cardElevated,
    marginHorizontal: 16,
    marginVertical: 6,
    padding: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(212, 175, 55, 0.35)'
  },
  hallmarkTitle: { color: COLORS.gold, fontSize: 11, fontWeight: 'bold' },
  hallmarkText: { color: COLORS.textPrimary, fontSize: 10, marginTop: 2, lineHeight: 14 },
  prepRow: { flexDirection: 'row', gap: 8, marginTop: 6 },
  prepOption: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    backgroundColor: COLORS.card,
    borderWidth: 1,
    borderColor: COLORS.border,
    alignItems: 'center'
  },
  prepOptionActive: {
    borderColor: COLORS.gold,
    backgroundColor: COLORS.goldContainer
  },
  prepText: { color: COLORS.textSecondary, fontSize: 10, fontWeight: '600', textAlign: 'center' },
  prepTextActive: { color: COLORS.gold, fontWeight: 'bold' },
  toppingOption: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: COLORS.card,
    borderRadius: 8,
    padding: 12,
    marginTop: 6,
    borderWidth: 1,
    borderColor: COLORS.border
  },
  toppingOptionActive: {
    borderColor: COLORS.gold,
    backgroundColor: 'rgba(212, 175, 55, 0.15)'
  },
  toppingLabel: { color: COLORS.textPrimary, fontSize: 12 },
  toppingPrice: { color: COLORS.gold, fontSize: 12, fontWeight: 'bold' },
  dinamitaRow: { flexDirection: 'row', gap: 8, marginTop: 6 },
  dinamitaBtn: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    backgroundColor: COLORS.card,
    borderWidth: 1,
    borderColor: COLORS.border,
    alignItems: 'center'
  },
  dinamitaBtnActive: {
    borderColor: COLORS.gold,
    backgroundColor: COLORS.goldContainer
  },
  dinamitaText: { color: COLORS.textSecondary, fontSize: 11, fontWeight: '600' },
  dinamitaTextActive: { color: COLORS.gold, fontWeight: 'bold' },
  dinamitaPromoText: { color: COLORS.goldLight, fontSize: 11, fontWeight: 'bold', marginTop: 4 },
  chipsRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 6 },
  suggestionChip: {
    backgroundColor: COLORS.card,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: COLORS.border
  },
  suggestionChipText: { color: COLORS.textSecondary, fontSize: 10 },
  zoneSelectorBtn: {
    backgroundColor: COLORS.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: COLORS.gold,
    padding: 12,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  zoneSelectorLabel: { color: COLORS.goldLight, fontSize: 11, fontWeight: '700', marginBottom: 2 },
  zoneSelectorValue: { color: COLORS.textPrimary, fontSize: 13, fontWeight: '600' },
  zoneSelectorArrow: { color: COLORS.gold, fontSize: 12, marginLeft: 8 },
  zoneModalContent: {
    backgroundColor: COLORS.surface,
    borderRadius: 16,
    padding: 16,
    maxHeight: '85%',
    width: '94%',
    borderWidth: 1,
    borderColor: COLORS.gold
  },
  zoneModalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  zoneModalTitle: { color: COLORS.gold, fontSize: 16, fontWeight: '800' },
  zoneSearchInput: {
    backgroundColor: COLORS.card,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: COLORS.border,
    color: COLORS.textPrimary,
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 13,
    marginBottom: 10
  },
  zoneItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 12,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
    borderRadius: 8
  },
  zoneItemActive: { backgroundColor: COLORS.goldContainer, borderColor: COLORS.gold, borderWidth: 1 },
  zoneItemName: { color: COLORS.textPrimary, fontSize: 13, fontWeight: '600' },
  zoneItemNameActive: { color: COLORS.gold, fontWeight: '800' },
  zoneItemBadge: { backgroundColor: COLORS.cardElevated, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6 },
  zoneItemBadgeActive: { backgroundColor: COLORS.gold },
  zoneItemPrice: { color: COLORS.goldLight, fontSize: 11, fontWeight: '700' },
  zoneItemPriceActive: { color: COLORS.black, fontWeight: '900' }
});
