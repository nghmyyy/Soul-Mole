package com.example.soulmole.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.soulmole.model.GameSession
import com.example.soulmole.model.Player
import java.text.SimpleDateFormat
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "game_database.db"
        private const val DATABASE_VERSION = 12

        // Player table
        const val TABLE_PLAYER = "Player"
        const val COLUMN_PLAYER_ID = "player_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_SCORE = "score"
        const val COLUMN_HEALTH = "health"
        const val COLUMN_DEPTH = "depth"

        // GameSession table
        const val TABLE_GAME_SESSIONS = "GameSessions"
        const val COLUMN_SESSION_ID = "session_id"
        const val COLUMN_TIME_SESSION = "time_session" // Thời gian chơi (giây)
        const val COLUMN_DATE_SESSION = "date_session" // Ngày giờ chơi
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPlayerTable = """
            CREATE TABLE $TABLE_PLAYER (
                $COLUMN_PLAYER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_SCORE INTEGER DEFAULT 0,
                $COLUMN_HEALTH INTEGER DEFAULT 100,
                $COLUMN_DEPTH INTEGER DEFAULT 0
            )
        """
        db.execSQL(createPlayerTable)

        val createGameSessionsTable = """
            CREATE TABLE $TABLE_GAME_SESSIONS (
                $COLUMN_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PLAYER_ID INTEGER NOT NULL,
                $COLUMN_TIME_SESSION INTEGER NOT NULL,
                $COLUMN_DATE_SESSION TEXT NOT NULL,
                FOREIGN KEY($COLUMN_PLAYER_ID) REFERENCES $TABLE_PLAYER($COLUMN_PLAYER_ID)
            )
        """.trimIndent()
        db.execSQL(createGameSessionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAME_SESSIONS")
        onCreate(db)
    }

    /**
     * Thêm người chơi mới
     */
    fun insertPlayer(player: Player): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, player.username)
            put(COLUMN_SCORE, player.score)
            put(COLUMN_HEALTH, player.health)
            put(COLUMN_DEPTH, player.depth)
        }
        val id = db.insert(TABLE_PLAYER, null, values)
        db.close()
        return id
    }

    /**
     * Thêm một phiên chơi mới (không cập nhật)
     */
    fun insertGameSession(gameSession: GameSession): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLAYER_ID, gameSession.player.playerId)
            put(COLUMN_TIME_SESSION, gameSession.timeSession)
            put(COLUMN_DATE_SESSION, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(gameSession.dateSession))
        }
        val newId = db.insert(TABLE_GAME_SESSIONS, null, values)
        db.close()
        return newId
    }

    /**
     * Lấy danh sách người chơi dựa trên username
     */
    fun getPlayerByUsername(username: String): Player? {
        val db = readableDatabase
        var player: Player? = null
        val cursor = db.query(
            TABLE_PLAYER,
            null,
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            val playerId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_ID))
            val score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE))
            val health = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HEALTH))
            val depth = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DEPTH))
            player = Player(playerId, username, score, health, depth)
        }
        cursor.close()
        Log.d("DatabaseHelper", "getPlayerByUsername: ${player != null}")
        return player
    }

    /**
     * Lấy danh sách top 5 người chơi theo điểm số, thời gian chơi và ngày chơi
     */
    fun getTopPlayers(limit: Int): List<GameSession> {
        val sessions = mutableListOf<GameSession>()
        val db = readableDatabase

        val query = """
        SELECT P.$COLUMN_PLAYER_ID, P.$COLUMN_USERNAME, P.$COLUMN_SCORE, 
               GS.$COLUMN_TIME_SESSION, GS.$COLUMN_DATE_SESSION
        FROM $TABLE_PLAYER P
        INNER JOIN $TABLE_GAME_SESSIONS GS ON P.$COLUMN_PLAYER_ID = GS.$COLUMN_PLAYER_ID
        ORDER BY P.$COLUMN_SCORE DESC, GS.$COLUMN_TIME_SESSION DESC, GS.$COLUMN_DATE_SESSION ASC
        LIMIT $limit
    """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val playerId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_ID))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE))
                val timeSession = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME_SESSION))
                val dateSession = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_SESSION))
                )

                val player = Player(playerId, username, score)
                val session = GameSession(0, player, timeSession, dateSession!!)
                sessions.add(session)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return sessions
    }

    fun getAllPlayersWithRanking(): List<GameSession> {
        val topSessions = mutableListOf<GameSession>()
        val db = readableDatabase

        val query = """
            SELECT P.$COLUMN_PLAYER_ID, P.$COLUMN_USERNAME, P.$COLUMN_SCORE, 
                   GS.$COLUMN_TIME_SESSION, GS.$COLUMN_DATE_SESSION
            FROM $TABLE_PLAYER P
            INNER JOIN $TABLE_GAME_SESSIONS GS ON P.$COLUMN_PLAYER_ID = GS.$COLUMN_PLAYER_ID
            ORDER BY P.$COLUMN_SCORE DESC, GS.$COLUMN_TIME_SESSION DESC, GS.$COLUMN_DATE_SESSION ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val playerId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_ID))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE))
                val timeSession = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME_SESSION))
                val dateSession = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_SESSION))
                )

                val player = Player(playerId, username, score)
                val session = GameSession(0, player, timeSession, dateSession!!)
                topSessions.add(session)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return topSessions
    }
    fun getAllPlayers(): List<GameSession> {
        return getTopPlayers(Int.MAX_VALUE) // Không giới hạn
    }

    /**
     * Xóa tất cả người chơi trong bảng Player và các phiên chơi liên quan
     */
    fun deleteAllPlayers(): Int {
        val db = writableDatabase
        return db.delete(TABLE_PLAYER, null, null)
    }
    /**
     * Lưu điểm và thông tin phiên chơi vào cơ sở dữ liệu
     */
    fun saveGameSessionAndPlayerScore(gameSession: GameSession) {
        val db = writableDatabase
        try {
            db.beginTransaction()

            // Cập nhật điểm số, độ sâu của người chơi
            val playerValues = ContentValues().apply {
                put(COLUMN_SCORE, gameSession.player.score)
                put(COLUMN_DEPTH, gameSession.player.depth)
            }
            db.update(
                TABLE_PLAYER,
                playerValues,
                "$COLUMN_PLAYER_ID = ?",
                arrayOf(gameSession.player.playerId.toString())
            )

            // Thêm thông tin phiên chơi vào bảng GameSessions
            val sessionValues = ContentValues().apply {
                put(COLUMN_PLAYER_ID, gameSession.player.playerId)
                put(COLUMN_TIME_SESSION, gameSession.timeSession)
                put(
                    COLUMN_DATE_SESSION,
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(gameSession.dateSession)
                )
            }
            db.insert(TABLE_GAME_SESSIONS, null, sessionValues)

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

}