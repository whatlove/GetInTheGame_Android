package com.example.getinthegame.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.getinthegame.ui.theme.teamColors
import java.util.UUID

data class Team(
    val name: String = "",
    val color: Color = teamColors.random().color,
    val order: Int = 0,
    var players: List<Player> = emptyList(),
    var court: Int = 0,
    var score: Int = 0,
    val id: UUID = UUID.randomUUID()
) {

    val darkColor = Color(ColorUtils.blendARGB(color.toArgb(), Color.Black.toArgb(), .2f))
    val nullPlayer = Player("+", 0)

    fun totalTeam(numPlayers: Int): List<Player> {
        val neededPlayers = numPlayers - players.size
        val teamPlayers = players.toMutableList()
        for (i in 1..neededPlayers) {
            teamPlayers += nullPlayer
        }
        return teamPlayers
    }


    fun isFull(numPlayers: Int): Boolean {
        return players.size >= numPlayers
    }


}

data class TeamColor(val color: Color, val name: String)