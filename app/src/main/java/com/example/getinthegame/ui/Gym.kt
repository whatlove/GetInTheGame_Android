package com.example.getinthegame.ui

data class Gym(
    var teams: MutableList<Team> = emptyList<Team>().toMutableList(),
    var players: MutableList<Player> = emptyList<Player>().toMutableList(),
    var playersPerTeam: Int = 6
) {
    init {
    }

    val teamsAvailable: Int
        get() {
            return teams.count { it.players.size < playersPerTeam }
        }

    fun removeTeam(team: Team) {
        teams -= team
    }
    fun addPlayer(player: Player) {
        players += player
    }
    fun removePlayer(player: Player) {
        players -= player
    }

}
