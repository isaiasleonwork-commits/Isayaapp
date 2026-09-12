import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

// Configuración común de Firebase para ambas aplicaciones (APK 1 y APK 2)
export const firebaseConfig = {
  apiKey: process.env.EXPO_PUBLIC_FIREBASE_API_KEY || "AIzaSyDemoKeyIsayaSushi2026",
  authDomain: "isaya-sushi-gourmet.firebaseapp.com",
  projectId: "isaya-sushi-gourmet",
  storageBucket: "isaya-sushi-gourmet.appspot.com",
  messagingSenderId: "93575845240",
  appId: "1:93575845240:web:842918491823"
};

// Inicializar Firebase una sola vez
export const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();
export const db = getFirestore(app);
export const auth = getAuth(app);

// Colecciones compartidas en Firestore:
// - isaya_orders: Pedidos en tiempo real (creados por APK 1, gestionados por APK 2)
// - isaya_menu: Disponibilidad On/Off de platillos
// - isaya_settings: Horario y estado Abierto/Cerrado del restaurante
// - isaya_broadcast_notifications: Notificaciones masivas push emitidas por APK 2 y recibidas por APK 1
