package com.example.util

import android.content.Context
import android.content.SharedPreferences

data class SavedUserLocation(
    val name: String = "",
    val phone: String = "",
    val address: String = "Av. Las Delicias, La Soledad, Maracay",
    val latitude: Double = 10.2520,
    val longitude: Double = -67.5980,
    val isConfirmed: Boolean = false
)

object UserLocationPreferences {
    private const val PREFS_NAME = "isaya_user_location_prefs"
    private const val KEY_NAME = "key_user_name"
    private const val KEY_PHONE = "key_user_phone"
    private const val KEY_ADDRESS = "key_user_address"
    private const val KEY_LATITUDE = "key_user_lat"
    private const val KEY_LONGITUDE = "key_user_lng"
    private const val KEY_CONFIRMED = "key_is_confirmed"
    private const val KEY_DARK_MODE = "key_dark_mode_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isDarkMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }

    fun getUserLocation(context: Context): SavedUserLocation {
        val prefs = getPrefs(context)
        return SavedUserLocation(
            name = prefs.getString(KEY_NAME, "") ?: "",
            phone = prefs.getString(KEY_PHONE, "") ?: "",
            address = prefs.getString(KEY_ADDRESS, "Av. Las Delicias, Urb. La Soledad, Maracay") ?: "Av. Las Delicias, Urb. La Soledad, Maracay",
            latitude = prefs.getString(KEY_LATITUDE, "10.2520")?.toDoubleOrNull() ?: 10.2520,
            longitude = prefs.getString(KEY_LONGITUDE, "-67.5980")?.toDoubleOrNull() ?: -67.5980,
            isConfirmed = prefs.getBoolean(KEY_CONFIRMED, false)
        )
    }

    fun saveUserLocation(
        context: Context,
        name: String,
        phone: String,
        address: String,
        latitude: Double = 10.2520,
        longitude: Double = -67.5980,
        isConfirmed: Boolean = true
    ) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_PHONE, phone)
            .putString(KEY_ADDRESS, address)
            .putString(KEY_LATITUDE, latitude.toString())
            .putString(KEY_LONGITUDE, longitude.toString())
            .putBoolean(KEY_CONFIRMED, isConfirmed)
            .apply()
    }
}

