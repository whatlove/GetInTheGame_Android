package com.example.getinthegame.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.getinthegame.ui.theme.teamColors
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class Team(
    val color: Color = teamColors.random().color,
    val colorName: String = "",
    val order: Int = 0,
    var players: List<Player> = emptyList(),
    var court: Int = 0,
    var score: Int = 0,
    val id: UUID = UUID.randomUUID()
) {
    val name: String
        get() = "Team $order - $colorName"

    val isFull: Boolean
        get() = players.size >= uiState.value.playersPerTeam
    private val availableSpots: Int
        get() = uiState.value.playersPerTeam - players.size
    val availableSpotsText: String
        get() = when {
            availableSpots == 0 -> "Team Full"
            availableSpots < 3 -> "Only $availableSpots of ${uiState.value.playersPerTeam} left!"
            else -> "$availableSpots of ${uiState.value.playersPerTeam} players needed"
        }

    val currentOpponent: Team?
        get() = uiState.value.teams.values.find {
            it.court != 0 &&        // Team is assigned to a court
            it.court == court &&    // Team is assigned to the same court
            it.id != id             // Team is not the current team
        }

    companion object {
        lateinit var uiState: StateFlow<GymUiState>
    }

    val nullPlayer = Player("+", 0)
    val darkColor = Color(ColorUtils.blendARGB(color.toArgb(), Color.Black.toArgb(), .2f))
    val textColor = if (color.luminance() > 0.5) Color.Black else Color.White

    fun totalTeam(): List<Player> {
        val teamPlayers = players.toMutableList()
        for (i in 1..availableSpots) {
            teamPlayers += nullPlayer
        }
        return teamPlayers
    }

}

data class TeamColor(val color: Color, val name: String)