package com.example.getinthegame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getinthegame.ui.theme.teamColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class GymViewModel : ViewModel() {
    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow(GymUiState())
    val uiState: StateFlow<GymUiState> = _uiState.asStateFlow()

    private val _teamsMap = MutableStateFlow<Map<UUID, Team>>(emptyMap())
    // Expose a StateFlow of the list of teams
    val teams: StateFlow<List<Team>> = _teamsMap.map { it.values.toList() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _gameLogs = MutableStateFlow<List<GameLog>>(emptyList())
    val gameLogs: StateFlow<List<GameLog>> = _gameLogs.asStateFlow()

    init {
        Team.uiState = uiState // Initialize the companion object property

        addTeam()
        addTeam()
    }


    private fun addTeam() {
        val nextOrder = (_teamsMap.value.values.maxByOrNull { it.order })?.order?.plus(1) ?: 1
        var newColor = teamColors.random()
        while (_teamsMap.value.any { it.value.color == newColor.color }) {
            newColor = teamColors.random()
        }
        val newTeam = Team(order = nextOrder, color = newColor.color, colorName = newColor.name)
        _teamsMap.value += newTeam.id to newTeam
    }
    private fun removeTeam(team: Team) {
        // REMOVE TEAM ASSIGNMENT FROM ALL PLAYERS ASSIGNED TO THIS TEAM
        // and update the _players StateFlow
        _players.value = _players.value.map { player ->
            if (player.teamId == team.id) player.copy(teamId = null) else player
        }

        // REMOVE TEAM FROM _teamsMap
        _teamsMap.value -= team.id
        // ADD A NEW TEAM IF THERE ARE LESS THAN 2 AVAILABLE TEAMS
        if(_teamsMap.value.count() < 2) { addTeam() }
    }

    fun addPlayer(player: Player) {
        _players.value += player
    }
    fun removePlayer(player: Player) {
        _players.value -= player
    }

    fun getAvailableTeams(): List<Team> {
        return teams.value.filter { !it.isFull }
    }
    fun addPlayerToTeam(player: Player, teamId: UUID) {
        val team = _teamsMap.value[teamId] ?: return // Get the Team from the map

        // Update the team's player list
        val updatedTeam = team.copy(players = team.players + player)
        _teamsMap.value += (teamId to updatedTeam)

        // If the team is full AND there are less than 2 available teams, add a new team
        if (updatedTeam.isFull &&
            getAvailableTeams().count() < 2) {
            addTeam()
        }

        // Update the _players StateFlow
        val updatedPlayers = _players.value.map {
            if (it == player) {
                player.copy(teamId = updatedTeam.id) // Create a new Player object with the assigned team
            } else {
                it
            }
        }
        _players.value = updatedPlayers
    }
    fun removePlayerFromTeam(player: Player) {
        val playerTeamId = player.teamId ?: return // If player has no team, do nothing
        val currentTeam = _teamsMap.value[playerTeamId] ?: return //
        println("Player reference team size: ${currentTeam.players.size ?: "null"}")

        val updatedTeamPlayers = currentTeam.players.filter { it.id != player.id }
        currentTeam.players = updatedTeamPlayers // Update the currentTeam
        // Trigger recomposition by updating the StateFlow
        _teamsMap.value -= currentTeam.id
        _teamsMap.value += (currentTeam.id to currentTeam)
        println("team size after update: ${updatedTeamPlayers.size}")

        // Update the _players StateFlow
        val updatedPlayers = _players.value.map {
            if (it == player) {
                player.copy(teamId = null) // Remove team assignment from the player
            } else {
                it
            }
        }
        _players.value = updatedPlayers
    }


    fun updatePlayersPerTeam(playersPerTeam: Int) {
        _uiState.value = _uiState.value.copy(playersPerTeam = playersPerTeam)
    }


    fun getNextAvailableTeam(): Team? {
        return teams.value.filter { it.court == 0 } // Teams that haven't played
            .minByOrNull { it.order } // Team with the lowest order
    }

    fun assignTeamToCourt(team: Team, courtNumber: Int) {
        val teamsOnCourt = _teamsMap.value.values.filter { it.court == courtNumber }

        if (courtNumber == -1) {
            // Team is leaving the court, proceed with removal logic (unchanged)
            removeTeamFromCourt(team)
        } else {
            val nextAvailableTeam = getNextAvailableTeam() // Get the next team in order that hasn't played yet
            if (nextAvailableTeam != null && nextAvailableTeam.id != team.id) {
                // There's a lower order team waiting, prompt the user
                _uiState.value = _uiState.value.copy(
                    skippedTeam = nextAvailableTeam,
                    teamToAssign = team
                )
            } else if (teamsOnCourt.size < 2) {
                // Court has space and the selected team is the next in order, assign the team
                val updatedTeam = team.copy(court = courtNumber)
                _teamsMap.value += (updatedTeam.id to updatedTeam)
            } else {
                // Court is full, trigger a dialog to choose a team to remove (unchanged)
                _uiState.value = _uiState.value.copy(
                    courtToRemoveTeamFrom = courtNumber,
                    teamToAssign = team
                )
            }
        }
    }
    fun removeTeamFromCourt(team: Team) {
        // Log the game
        _gameLogs.value += GameLog(
            team = team,
            score = 0, // ADD TRACK SCORE ABILITY
            court = team.court,
            opponent = team, // ADD OPPONENT
            opponentScore = 0 // ADD TRACK SCORE ABILITY
        )
        removeTeam(team) // Destroy the team
        clearTeamRemovalState()
    }

    fun clearTeamRemovalState() {
        _uiState.value = _uiState.value.copy(
            courtToRemoveTeamFrom = null,
            teamToAssign = null
        )
    }
    fun clearTeamOrderPrompt() {
        _uiState.value = _uiState.value.copy(
            teamToAssign = null,
            skippedTeam = null
        )
    }

}

