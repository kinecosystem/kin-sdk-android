package kin.sdk.internal.utils

import org.json.JSONArray
import org.json.JSONObject

fun String.hexStringToByteArray(): ByteArray =
    ByteArray(length / 2)
        .also {
            for (i in 0 until length step 2) {
                it[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            }
        }

fun ByteArray.bytesToHex(): String =
    StringBuilder()
        .also { sb ->
            forEach {
                sb.append(String.format("%02x", it))
            }
        }
        .toString()

fun <R> JSONArray.forEachJsonObject(action: (JSONObject) -> R): List<R> {
    return toJsonObjectList()
        .map { action(it) }
}

fun JSONArray.toJsonObjectList(): List<JSONObject> =
    (0 until length())
        .map { getJSONObject(it) }
