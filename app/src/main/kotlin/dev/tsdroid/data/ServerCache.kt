package dev.tsdroid.data

import android.content.Context
import android.util.Log
import dev.tslib.Channel
import dev.tslib.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Caches the last-known channel and user list to disk.
 * On reconnect, cached data is shown immediately while fresh data loads.
 */
class ServerCache(private val context: Context) {

    companion object {
        private const val TAG = "ServerCache"
    }

    private val cacheDir = File(context.filesDir, "server_cache")

    fun saveChannels(address: String, channels: List<Channel>) {
        try {
            cacheDir.mkdirs()
            val arr = JSONArray()
            for (ch in channels) {
                arr.put(JSONObject().apply {
                    put("id", ch.id)
                    put("parentId", ch.parentId)
                    put("name", ch.name ?: "")
                    put("topic", ch.topic ?: "")
                    put("description", ch.description ?: "")
                    put("order", ch.order)
                    put("isPermanent", ch.isPermanent)
                    put("isSemiPermanent", ch.isSemiPermanent)
                    put("isDefault", ch.isDefault)
                    put("hasPassword", ch.hasPassword)
                    put("codec", ch.codec.toInt())
                    put("codecQuality", ch.codecQuality.toInt())
                    put("maxClients", ch.maxClients)
                    put("maxFamilyClients", ch.maxFamilyClients)
                    put("neededTalkPower", ch.neededTalkPower)
                    put("iconId", ch.iconId)
                    put("permissionHints", ch.permissionHints)
                })
            }
            File(cacheDir, sanitize(address) + "_channels.json").writeText(arr.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache channels", e)
        }
    }

    fun loadChannels(address: String): List<Channel>? {
        val file = File(cacheDir, sanitize(address) + "_channels.json")
        if (!file.exists()) return null
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Channel(
                    obj.getLong("id"), obj.getLong("parentId"),
                    obj.optString("name", ""), obj.optString("topic", ""),
                    obj.optString("description", ""), obj.getInt("order"),
                    obj.getBoolean("isPermanent"), obj.getBoolean("isSemiPermanent"),
                    obj.getBoolean("isDefault"), obj.getBoolean("hasPassword"),
                    obj.getInt("codec").toByte(), obj.getInt("codecQuality").toByte(),
                    obj.getInt("maxClients"), obj.getInt("maxFamilyClients"),
                    obj.getInt("neededTalkPower"), obj.getLong("iconId"),
                    obj.getLong("permissionHints"),
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load cached channels", e)
            null
        }
    }

    fun saveUsers(address: String, users: List<User>) {
        try {
            cacheDir.mkdirs()
            val arr = JSONArray()
            for (u in users) {
                arr.put(JSONObject().apply {
                    put("id", u.id)
                    put("uid", u.uid ?: "")
                    put("databaseId", u.databaseId)
                    put("channelId", u.channelId)
                    put("nickname", u.nickname ?: "")
                    put("clientType", u.clientType)
                    put("isTalking", u.isTalking)
                    put("isInputMuted", u.isInputMuted)
                    put("isOutputMuted", u.isOutputMuted)
                    put("isAway", u.isAway)
                    put("isRecording", u.isRecording)
                    put("isPrioritySpeaker", u.isPrioritySpeaker)
                    put("isChannelCommander", u.isChannelCommander)
                    put("isTalker", u.isTalker)
                    put("talkPower", u.talkPower)
                    put("avatarId", u.avatarId ?: "")
                    put("iconId", u.iconId)
                    put("country", u.country ?: "")
                    put("platform", u.platform ?: "")
                    put("version", u.version ?: "")
                })
            }
            File(cacheDir, sanitize(address) + "_users.json").writeText(arr.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache users", e)
        }
    }

    fun loadUsers(address: String): List<User>? {
        val file = File(cacheDir, sanitize(address) + "_users.json")
        if (!file.exists()) return null
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                User(
                    obj.getInt("id"), obj.optString("uid", null),
                    obj.getLong("databaseId"), obj.getLong("channelId"),
                    obj.optString("nickname", ""), obj.getInt("clientType").toByte(),
                    obj.getBoolean("isTalking"), obj.getBoolean("isInputMuted"),
                    obj.getBoolean("isOutputMuted"),
                    true, true, // hasInputHardware, hasOutputHardware
                    obj.getBoolean("isAway"), obj.getBoolean("isRecording"),
                    obj.getBoolean("isPrioritySpeaker"), obj.getBoolean("isChannelCommander"),
                    obj.getBoolean("isTalker"), obj.getInt("talkPower"),
                    null, null, // awayMessage, serverGroups (long[]? = null is fine)
                    0L, // channelGroup (long, not nullable)
                    obj.optString("platform", ""), obj.optString("version", ""),
                    obj.optString("country", ""), obj.optString("description", "") ?: "",
                    obj.optString("avatarId", null), obj.getLong("iconId"),
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load cached users", e)
            null
        }
    }

    fun clear(address: String) {
        File(cacheDir, sanitize(address) + "_channels.json").delete()
        File(cacheDir, sanitize(address) + "_users.json").delete()
    }

    private fun sanitize(address: String) = address.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}
