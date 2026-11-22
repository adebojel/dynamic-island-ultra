package com.adebojel.dynamicislandultra.backup

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/** Whitelisted local preferences only; purchase entitlements and OS permissions are excluded. */
internal object BackupArchive {
    private const val FORMAT = "dynamic-island-ultra-backup"
    private const val VERSION = 1
    private val STORES = listOf("island", "offline_notes", "browser_settings", "notification_privacy", "reminders", "clipboard_history")

    fun create(context: Context): ByteArray {
        val stores = JSONObject()
        STORES.forEach { name ->
            val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
            val values = JSONObject()
            prefs.all.forEach valuesLoop@ { (key, value) ->
                if (name == "island" && key == "enabled") return@valuesLoop
                val encoded = encodeValue(value) ?: return@valuesLoop
                values.put(key, encoded)
            }
            stores.put(name, values)
        }
        return JSONObject().apply {
            put("format", FORMAT)
            put("version", VERSION)
            put("createdAt", System.currentTimeMillis())
            put("stores", stores)
        }.toString().toByteArray(Charsets.UTF_8)
    }

    fun restore(context: Context, bytes: ByteArray): Int {
        val root = JSONObject(bytes.toString(Charsets.UTF_8))
        require(root.optString("format") == FORMAT) { "Wrong backup file." }
        require(root.optInt("version") == VERSION) { "Unsupported backup version." }
        val stores = root.optJSONObject("stores") ?: error("Backup stores missing.")
        var restored = 0
        STORES.forEach { name ->
            val values = stores.optJSONObject(name) ?: return@forEach
            val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
            val keys = values.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (name == "island" && key == "enabled") continue
                val wrapper = values.optJSONObject(key) ?: continue
                decodeValue(wrapper, editor, key)
                restored++
            }
            if (name == "island") editor.putBoolean("enabled", false)
            check(editor.commit()) { "Could not restore preference store: $name" }
        }
        return restored
    }

    private fun encodeValue(value: Any?): JSONObject? = when (value) {
        is String -> JSONObject().put("type", "string").put("value", value)
        is Boolean -> JSONObject().put("type", "boolean").put("value", value)
        is Int -> JSONObject().put("type", "int").put("value", value)
        is Long -> JSONObject().put("type", "long").put("value", value)
        is Float -> JSONObject().put("type", "float").put("value", value.toDouble())
        is Set<*> -> {
            val strings = value.filterIsInstance<String>()
            if (strings.size != value.size) null
            else JSONObject().put("type", "string_set").put("value", JSONArray(strings))
        }
        else -> null
    }

    private fun decodeValue(wrapper: JSONObject, editor: SharedPreferences.Editor, key: String) {
        when (wrapper.optString("type")) {
            "string" -> editor.putString(key, wrapper.optString("value"))
            "boolean" -> editor.putBoolean(key, wrapper.optBoolean("value"))
            "int" -> editor.putInt(key, wrapper.optInt("value"))
            "long" -> editor.putLong(key, wrapper.optLong("value"))
            "float" -> editor.putFloat(key, wrapper.optDouble("value").toFloat())
            "string_set" -> {
                val array = wrapper.optJSONArray("value") ?: JSONArray()
                val values = buildSet { for (index in 0 until array.length()) add(array.optString(index)) }
                editor.putStringSet(key, values)
            }
        }
    }
}
