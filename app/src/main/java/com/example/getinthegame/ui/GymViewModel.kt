package com.example.getinthegame.ui

import androidx.lifecycle.ViewModel
import com.example.getinthegame.ui.theme.teamColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class GymViewModel : ViewModel() {
    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow(GymUiState())
    val uiState: StateFlow<GymUiState> = _uiState.asStateFlow()

    init {
        Team.uiState = uiState // Initialize the companion object property

        addTeam()
        addTeam()
    }


    private fun addTeam() {
        val nextOrder = (_uiState.value.teams.values.maxByOrNull { it.order })?.order?.plus(1) ?: 1
        var newColor = teamColors.random()
        while (_uiState.value.teams.values.any { it.color == newColor.color }) {
            newColor = teamColors.random()
        }
        val newTeam = Team(order = nextOrder, color = newColor.color, colorName = newColor.name)
        // Update the teams list in the UiState
        _uiState.value = _uiState.value.copy(
            teams = _uiState.value.teams + (newTeam.id to newTeam)
        )
    }
    private fun removeTeam(team: Team) {
        // REMOVE TEAM ASSIGNMENT FROM ALL PLAYERS ASSIGNED TO THIS TEAM
        // and REMOVE TEAM
        // and update the uiStateFlow
        _uiState.value = _uiState.value.copy(
            players = _uiState.value.players.map { player ->
                if (player.teamId == team.id) player.copy(teamId = null) else player
            },
            teams = _uiState.value.teams - team.id // Remove the team with the specified ID
        )
        // ADD A NEW TEAM IF THERE ARE LESS THAN 2 AVAILABLE TEAMS
        if(uiState.value.teams.values.count() < 2) { addTeam() }
    }

    fun selectTeam(team: Team) {
        _uiState.value = _uiState.value.copy(selectedTeam = team)
    }
    fun clearSelectedTeam() {
        _uiState.value = _uiState.value.copy(selectedTeam = null)
    }

    fun addPlayer(player: Player) {
        _uiState.value = _uiState.value.copy(
            players = _uiState.value.players + player
        )
    }
    fun removePlayer(player: Player) {
        _uiState.value = _uiState.value.copy(
            players = _uiState.value.players - player
        )
    }

    fun getAvailableTeams(): List<Team> {
        return uiState.value.teams.values.filter { !it.isFull }
    }
    fun addPlayerToTeam(player: Player, teamId: UUID) {
        val team = _uiState.value.teams[teamId] ?: return // Get the Team from the map

        // Update the team's player list
        val updatedTeam = team.copy(players = team.players + player)
        // Update the teams list in the UiState
        _uiState.value = _uiState.value.copy(
            teams = _uiState.value.teams + (updatedTeam.id to updatedTeam)
        )

        // If there are less than 2 available teams, add a new team
        if (getAvailableTeams().count() < 2) {
            addTeam()
        }

        // Update the players list in the uiState
        _uiState.value = _uiState.value.copy(
            players = _uiState.value.players.map {
                if (it == player) {
                    it.copy(teamId = teamId) // Assign the teamId to the player
                } else {
                    it
                }
            }
        )
    }
    fun removePlayerFromTeam(player: Player) {
        val playerTeamId = player.teamId ?: return // If player has no team, do nothing
        val currentTeam = uiState.value.teams[playerTeamId] ?: return  // If team doesn't exist, do nothing
        println("Team size (uiState): ${currentTeam.players.size}")

        _uiState.value = _uiState.value.copy(
            teams = _uiState.value.teams.toMutableMap().apply {
                val updatedTeam = this[currentTeam.id]?.copy(
                    players = this[currentTeam.id]?.players?.filter { it.id != player.id } ?: emptyList()
                )
                if (updatedTeam != null) {
                    this[currentTeam.id] = updatedTeam
                }
            },
            players = _uiState.value.players.map {
                if (it == player) {
                    it.copy(teamId = null) // Remove team assignment from the player
                } else {
                    it
                }
            }
        )
        println("Team size after update (uiState): ${uiState.value.teams[playerTeamId]?.players?.size ?: "None"}")
    }


    fun increasePlayersPerTeam() {
        val playersPerTeam = _uiState.value.playersPerTeam + 1
        _uiState.value = _uiState.value.copy(playersPerTeam = playersPerTeam)
    }fun decreasePlayersPerTeam() {
        val playersPerTeam = _uiState.value.playersPerTeam - 1
        _uiState.value = _uiState.value.copy(playersPerTeam = playersPerTeam)
    }


    fun getNextAvailableTeam(): Team? {
        return uiState.value.teams.values.filter { it.court == 0 } // Teams that haven't played
            .minByOrNull { it.order } // Team with the lowest order
    }

    fun assignTeamToCourt(team: Team, courtNumber: Int) {
        val teamsOnCourt = uiState.value.teams.values.filter { it.court == courtNumber }

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
                _uiState.value = _uiState.value.copy(
                    teams = _uiState.value.teams.toMutableMap().apply {
                        val updatedTeam = this[team.id]?.copy(court = courtNumber)
                        if (updatedTeam != null) {
                            this[team.id] = updatedTeam
                        }
                    }
                )
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
        _uiState.value = _uiState.value.copy(
            gameLogs = _uiState.value.gameLogs + GameLog(
                team = team,
                score = 0, // ADD TRACK SCORE ABILITY
                court = team.court,
                opponent = team.currentOpponent,
                opponentScore = team.currentOpponent?.score // ADD TRACK SCORE ABILITY
            )
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

