package kin.sdk.internal.storage

import android.content.SharedPreferences

internal class SharedPrefStore(private val sharedPref: SharedPreferences) : Store {

    override fun saveString(key: String, value: String) =
        sharedPref.edit()
            .putString(key, value)
            .apply()

    override fun getString(key: String): String? = sharedPref.getString(key, null)

    override fun clear(key: String) = sharedPref.edit().remove(key).apply()
}
