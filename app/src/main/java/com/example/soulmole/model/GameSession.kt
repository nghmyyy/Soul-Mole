package com.example.soulmole.model

import java.util.Date

data class GameSession(
    val sessionId: Int = 0,
    val player: Player,
    val timeSession: Int = 0,
    val dateSession: Date = Date(),
)
