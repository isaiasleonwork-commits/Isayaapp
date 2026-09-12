import React, { useState } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  StatusBar
} from 'react-native';
import AppClientes from './AppClientes';
import AppPartner from './AppPartner';

export default function App() {
  const [currentApp, setCurrentApp] = useState<'CLIENT' | 'PARTNER'>('CLIENT');
  const [showSwitcher, setShowSwitcher] = useState(true);

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0D0D0D" />
      
      {/* Selector de Aplicación Expo */}
      {showSwitcher ? (
        <SafeAreaView style={styles.switcherWrapper}>
          <View style={styles.switcherContainer}>
            <View style={styles.brandRow}>
              <Text style={styles.brandTitle}>ISAYA SUSHI • EXPO GO</Text>
              <TouchableOpacity
                style={styles.hideButton}
                onPress={() => setShowSwitcher(false)}
              >
                <Text style={styles.hideButtonText}>Ocultar Barra ✕</Text>
              </TouchableOpacity>
            </View>

            <View style={styles.tabsRow}>
              <TouchableOpacity
                style={[
                  styles.tabButton,
                  currentApp === 'CLIENT' && styles.tabButtonActive
                ]}
                onPress={() => setCurrentApp('CLIENT')}
              >
                <Text
                  style={[
                    styles.tabButtonText,
                    currentApp === 'CLIENT' && styles.tabButtonTextActive
                  ]}
                >
                  🍣 App Clientes
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.tabButton,
                  currentApp === 'PARTNER' && styles.tabButtonActive
                ]}
                onPress={() => setCurrentApp('PARTNER')}
              >
                <Text
                  style={[
                    styles.tabButtonText,
                    currentApp === 'PARTNER' && styles.tabButtonTextActive
                  ]}
                >
                  👨‍🍳 App Partner (Cocina)
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        </SafeAreaView>
      ) : (
        <TouchableOpacity
          style={styles.floatingToggleButton}
          onPress={() => setShowSwitcher(true)}
          activeOpacity={0.8}
        >
          <Text style={styles.floatingToggleText}>
            ⇄ Cambiar App ({currentApp === 'CLIENT' ? 'Clientes' : 'Cocina'})
          </Text>
        </TouchableOpacity>
      )}

      {/* Renderizado de la App activa */}
      <View style={styles.screenContainer}>
        {currentApp === 'CLIENT' ? <AppClientes /> : <AppPartner />}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D0D0D'
  },
  switcherWrapper: {
    backgroundColor: '#141414',
    borderBottomWidth: 1,
    borderBottomColor: '#2C2C2E'
  },
  switcherContainer: {
    paddingHorizontal: 12,
    paddingTop: 8,
    paddingBottom: 8
  },
  brandRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6
  },
  brandTitle: {
    color: '#D4AF37',
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1
  },
  hideButton: {
    paddingHorizontal: 6,
    paddingVertical: 2
  },
  hideButtonText: {
    color: '#A0A0A5',
    fontSize: 10
  },
  tabsRow: {
    flexDirection: 'row',
    backgroundColor: '#1C1C1E',
    borderRadius: 8,
    padding: 3
  },
  tabButton: {
    flex: 1,
    paddingVertical: 7,
    alignItems: 'center',
    borderRadius: 6
  },
  tabButtonActive: {
    backgroundColor: '#D4AF37'
  },
  tabButtonText: {
    color: '#A0A0A5',
    fontSize: 12,
    fontWeight: '600'
  },
  tabButtonTextActive: {
    color: '#0D0D0D',
    fontWeight: '800'
  },
  floatingToggleButton: {
    position: 'absolute',
    top: 40,
    right: 12,
    zIndex: 999,
    backgroundColor: '#D4AF37',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.5,
    shadowRadius: 4,
    elevation: 6
  },
  floatingToggleText: {
    color: '#0D0D0D',
    fontSize: 10,
    fontWeight: '800'
  },
  screenContainer: {
    flex: 1
  }
});
