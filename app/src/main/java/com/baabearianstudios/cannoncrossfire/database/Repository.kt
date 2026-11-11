package com.baabearianstudios.cannoncrossfire.database

import android.util.Log
import com.baabearianstudios.cannoncrossfire.network.ApiService
import com.baabearianstudios.cannoncrossfire.network.OnlineScore
import kotlinx.coroutines.flow.Flow

/**
 * A repository that handles data operations for scores. It abstracts the data sources,
 * providing a clean API for the rest of the app to interact with.
 *
 * @param highScoreDao The Data Access Object for the local high score database.
 * @param apiService The service for interacting with the online leaderboard API.
 */
class ScoreRepository(private val highScoreDao: HighScoreDao, private val apiService: ApiService) {

    /**
     * Retrieves the local high score from the database.
     *
     * @return A Flow that emits the local [HighScore], or null if none exists.
     */
    fun getLocalHighScore(): Flow<HighScore?> = highScoreDao.getHighScore()

    /**
     * Saves a new high score to the local database.
     *
     * @param score The score to save.
     */
    suspend fun saveLocalHighScore(score: Int) {
        highScoreDao.insertHighScore(HighScore(score = score))
    }

    /**
     * Fetches the top 5 scores from the online leaderboard.
     * Online leaderboard is disabled; returns an empty list.
     *
     * @return An empty list.
     */
    suspend fun getOnlineLeaderboard(): List<OnlineScore> {
        return emptyList()
    }

    /**
     * Submits a score to the online leaderboard for a given player.
     * Online leaderboard is disabled; this operation is a no-op.
     *
     * @param playerName The name of the player.
     * @param score The score to submit.
     */
    suspend fun submitOnlineScore(playerName: String, score: Int) {
        // Online leaderboard submission is disabled
    }
}
