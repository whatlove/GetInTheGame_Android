package com.example.getinthegame.ui

data class GymUiState (
    val isAddingPlayer: Boolean = false,
    val players: List<Player> = emptyList(),
    val teams: List<Team> = emptyList(),
    val playersPerTeam: Int = 6
)