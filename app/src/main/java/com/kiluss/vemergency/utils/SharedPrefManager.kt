package com.kiluss.vemergency.utils

import android.content.Context
import android.content.SharedPreferences
import com.kiluss.vemergency.constant.SHARED_PREFERENCE_KEY

object SharedPrefManager {

    private lateinit var sharedPreferences: SharedPreferences

    internal fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(
            SHARED_PREFERENCE_KEY,
            Context.MODE_PRIVATE
        )
    }

    internal fun getBoolean(key: String, defaultValue: Boolean) = sharedPreferences.getBoolean(key, defaultValue)

    internal fun putBoolean(key: String, saveValue: Boolean) =
        sharedPreferences.edit().putBoolean(key, saveValue).apply()

    internal fun getInt(key: String, defaultValue: Int) = sharedPreferences.getInt(key, defaultValue)

    internal fun putInt(key: String, saveValue: Int) = sharedPreferences.edit().putInt(key, saveValue).apply()

    internal fun getString(key: String, defaultValue: String) = sharedPreferences.getString(key, defaultValue)

    internal fun putString(key: String, saveValue: String) = sharedPreferences.edit().putString(key, saveValue).apply()
}
