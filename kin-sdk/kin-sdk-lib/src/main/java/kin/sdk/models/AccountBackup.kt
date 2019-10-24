package kin.sdk.models

import org.json.JSONObject

data class AccountBackup(
    val publicAddress: String,
    val saltHexString: String,
    val encryptedSeedHexString: String
) {
    private companion object {
        private const val JSON_KEY_PUBLIC_KEY = "pkey"
        private const val JSON_KEY_SEED = "seed"
        private const val JSON_KEY_SALT = "salt"
        private const val OUTPUT_JSON_INDENT_SPACES = 2
    }

    private constructor(jsonObject: JSONObject) : this(
        jsonObject.getString(JSON_KEY_PUBLIC_KEY),
        jsonObject.getString(JSON_KEY_SALT),
        jsonObject.getString(JSON_KEY_SEED)
    )

    constructor(jsonString: String) : this(JSONObject(jsonString))

    val jsonString: String by lazy {
        JSONObject()
            .apply {
                put(JSON_KEY_PUBLIC_KEY, publicAddress)
                put(JSON_KEY_SEED, encryptedSeedHexString)
                put(JSON_KEY_SALT, saltHexString)
            }
            .toString(OUTPUT_JSON_INDENT_SPACES)
    }

    override fun toString(): String {
        return jsonString
    }
}
