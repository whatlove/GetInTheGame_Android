package com.example.getinthegame.ui

import java.util.UUID

data class GymUiState (
    val isAddingPlayer: Boolean = false,

    val players: List<Player> = emptyList(),
    val teams: Map<UUID, Team> = emptyMap(),
    val playersPerTeam: Int = 6,

    val courtToRemoveTeamFrom: Int? = null, // Court needing a team removed
    val teamToAssign: Team? = null, // Team waiting to be assigned
    val skippedTeam: Team? = null, // Team that was skipped over
    val selectedTeam: Team? = null, // Team selected from list

    val gameLogs: List<GameLog> = emptyList() // List of all game logs for session history
)