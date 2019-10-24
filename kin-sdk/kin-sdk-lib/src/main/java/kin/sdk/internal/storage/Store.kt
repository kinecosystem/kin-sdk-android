package kin.sdk.internal.storage

interface Store {

    fun saveString(key: String, value: String)

    fun getString(key: String): String?

    fun clear(key: String)
}
