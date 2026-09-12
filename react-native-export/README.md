# Isaya Sushi - Arquitectura de Dos Proyectos / APKs Independientes

Este directorio contiene la arquitectura y código fuente desacoplado en dos proyectos independientes de **React Native** conectados a la misma base de datos de **Firebase Firestore** y **Firebase Authentication**:

---

## 📱 1. App de Clientes (APK 1) -> `AppClientes.tsx`
Aplicación pública para consumidores finales de Isaya Sushi.
- **Catálogo Nikkei ($6 a $24 USD)**:
  - Alaska Especial ($11), Tokyo Fusion ($12), California ($8)
  - Ebi Crunch Tempura ($14), Fuji Volcano ($16)
  - Combo Dúo (18 pcs - $15), Mega Combo (30 pcs - $22), Golden Dragon (24 pcs - $24)
  - Gyozas ($6), Ebi Tempura ($8), Té Matcha ($6), Asahi ($8)
- **Personalización de Rolls**: Salsas (Soya, Teriyaki, Spicy Mayo), wasabi, jengibre y notas al chef.
- **Selector de Modo**: Delivery con recargo ($2.50) o Retiro en Tienda (Gratis).
- **Checkout Pago Móvil Banesco**: Datos de cuenta Banesco (0134, J-40892314-1, 0414-1234567), campo de 4 dígitos de referencia y comprobante.
- **Seguimiento en Vivo**: 4 etapas con actualización en tiempo real desde Firestore y botón directo de atención por WhatsApp.
- **Recepción de Notificaciones Push**: Indicador visual (campana con contador), banner emergente en la parte superior y modal con promociones vigentes.

---

## 👨‍🍳 2. App Partner / Equipo (APK 2) -> `AppPartner.tsx`
Aplicación privada y protegida para el equipo de cocina y administración de Isaya Sushi.
- **Protegido con Firebase Authentication**:
  - Pantalla de inicio de sesión con Email y Contraseña.
  - Credenciales de prueba: `cocina@isayasushi.com` / `isaya2026`.
- **Pantalla de Cocina & Monitor de Comandas**:
  - Alerta de sonido (chime) y vibración al recibir nuevas órdenes.
  - Botón "Probar Alerta Sonora".
  - Verificación detallada de comprobantes Pago Móvil y montos transferidos.
  - Botones de acción de estatus (*Confirmar Pago*, *En Cocina*, *Listo / Envío*).
- **Control de Menú y Horarios**:
  - Interruptor maestro de Tienda (*Abierto / Cerrado*).
  - Interruptores On/Off individuales para marcar rolls como **Agotado** en tiempo real.
- **Disparador de Notificaciones Push Masivas**:
  - Panel para redactar título, cuerpo y código de descuento promocional (ej. `PROMO2X1`).
  - Disparo masivo instantáneo sincronizado con Firestore que notifica a todos los clientes.

---

## 🚀 Cómo Ejecutar con Expo (Desarrollo & QR)

El proyecto en este directorio ya está 100% configurado y listo para correr con **Expo Go**:

### 1. Iniciar en Modo Desarrollo (Genera tu QR local)
```bash
cd react-native-export
npm install
npx expo start
```
Esto abrirá Metro Bundler y mostrará en tu terminal el **código QR interactivo**. Escanéalo desde tu teléfono con la aplicación **Expo Go** (disponible gratis en Google Play Store y Apple App Store).

### 2. Opciones de Ejecución:
- **`npx expo start`**: Inicia en red local (Wi-Fi).
- **`npx expo start --tunnel`**: Inicia a través del túnel ngrok de Expo (útil si tu teléfono y computadora están en redes distintas o datos móviles).
- **`npx expo start --android`**: Ejecuta directamente en un emulador Android local.
- **`npx expo start --web`**: Abre la versión web en tu navegador.

### 3. Selector Integrado en `App.tsx`:
Al abrir la app en Expo Go verás un selector en la parte superior:
- 🍣 **App Clientes**: Prueba la experiencia de compra, catálogo de rolls, personalización, cálculo automático de flete de 73 zonas de Maracay y checkout Pago Móvil.
- 👨‍🍳 **App Partner (Cocina)**: Monitoreo de comandas en vivo, alerta acústica y vibración, actualización de pedidos a cocina/listo, interruptores de rolls agotados y disparador de promociones push.

### 4. Compilación de APKs de Producción (EAS Build):
```bash
# Instalar EAS CLI si no lo tienes:
npm install -g eas-cli

# Compilar APK de Clientes:
eas build -p android --profile preview

# Compilar APK de Partner/Cocina:
eas build -p android --profile preview
```

---

## 🎨 Paleta de Diseño Oficial Isaya Sushi
- **Negro Profundo**: `#0D0D0D`
- **Dorado Imperial**: `#D4AF37`
- **Rojo Carmesí / Sushi**: `#C8102E`
