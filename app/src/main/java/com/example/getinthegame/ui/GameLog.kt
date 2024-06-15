package com.example.getinthegame.ui

data class GameLog(
    val team: Team,
    val score: Int,
    val court: Int,
    val opponent: Team?,
    val opponentScore: Int?
)
