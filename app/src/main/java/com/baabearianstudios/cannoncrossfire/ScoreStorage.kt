package com.baabearianstudios.cannoncrossfire

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object ScoreStorage {
    private const val PREFS_NAME = "cannon_crossfire_prefs"
    private const val KEY_HIGH_SCORE = "cannon_crossfire_high_score"
    private const val KEY_LEADERBOARD = "cannon_crossfire_leaderboard"

    private val DEFAULT_LEADERBOARD = listOf(
        OnlineScore("Captain Black", 120),
        OnlineScore("SeaDog99", 95),
        OnlineScore("CannonMaster", 80),
        OnlineScore("Barnaby", 65),
        OnlineScore("PiratePete", 50)
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLocalHighScore(context: Context): Int {
        return getPrefs(context).getInt(KEY_HIGH_SCORE, 0)
    }

    fun saveLocalHighScore(context: Context, score: Int): Boolean {
        val current = getLocalHighScore(context)
        if (score > current) {
            getPrefs(context).edit().putInt(KEY_HIGH_SCORE, score).apply()
            return true
        }
        return false
    }

    fun getLeaderboard(context: Context): List<OnlineScore> {
        val raw = getPrefs(context).getString(KEY_LEADERBOARD, null) ?: return DEFAULT_LEADERBOARD
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<OnlineScore>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(OnlineScore(obj.getString("playerName"), obj.getInt("score")))
            }
            if (list.isNotEmpty()) list.take(5) else DEFAULT_LEADERBOARD
        } catch (e: Exception) {
            DEFAULT_LEADERBOARD
        }
    }

    fun submitScore(context: Context, playerName: String, score: Int): List<OnlineScore> {
        val current = getLeaderboard(context).toMutableList()
        val cleanName = playerName.trim().ifEmpty { "Player" }
        current.add(OnlineScore(cleanName, score))
        current.sortByDescending { it.score }
        val top5 = current.take(5)

        try {
            val jsonArray = JSONArray()
            top5.forEach {
                val obj = JSONObject()
                obj.put("playerName", it.playerName)
                obj.put("score", it.score)
                jsonArray.put(obj)
            }
            getPrefs(context).edit().putString(KEY_LEADERBOARD, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }

        saveLocalHighScore(context, score)
        return top5
    }
}
