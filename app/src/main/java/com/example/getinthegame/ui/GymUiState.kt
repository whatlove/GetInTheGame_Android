package com.example.getinthegame.ui

data class GymUiState (
    val isAddingPlayer: Boolean = false,

    val players: List<Player> = emptyList(),
    val teams: List<Team> = emptyList(),
    val playersPerTeam: Int = 6,

    val courtToRemoveTeamFrom: Int? = null, // Court needing a team removed
    val teamToAssign: Team? = null, // Team waiting to be assigned
    val skippedTeam: Team? = null // Team that was skipped over
)