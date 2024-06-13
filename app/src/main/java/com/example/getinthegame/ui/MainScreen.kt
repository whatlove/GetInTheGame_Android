package com.example.getinthegame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.getinthegame.ui.theme.GetInTheGameTheme
import com.example.getinthegame.ui.theme.nullTeamColor


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    gymViewModel: GymViewModel = viewModel()
) {
    val teams = gymViewModel.teams.collectAsState()
    val players = gymViewModel.players.collectAsState()
    val uiState = gymViewModel.uiState.collectAsState()

    Column(modifier = modifier.padding(paddingValues)) {
        // Top Half - TeamList
        Column(modifier = Modifier.weight(1f)) {
            TeamList(
                teams = teams.value,
                playersPerTeam = uiState.value.playersPerTeam,
                gymViewModel = gymViewModel
            )
        }

        // Center Section - Buttons Row
        Row(modifier = Modifier
            .padding(2.dp)
            .align(Alignment.End)
            .fillMaxWidth() // Make buttons row take full width
        ) {
            Spacer(modifier = Modifier.weight(1f))
            ElevatedButton(
                onClick = {
                    val names = listOf("Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "George", "Hannah", "Ivan", "Julia", "Kevin", "Lily", "Michael", "Nancy", "Oliver")
                    gymViewModel.addPlayer(Player(name = names.random(), pin = 1234))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = nullTeamColor
                ),
                modifier = Modifier
                    .padding(2.dp)
            ) {
                Text(text = "New Player")
            }
            CreatePlayerButton(
                onAddPlayer = { newPlayer ->
                    gymViewModel.addPlayer(newPlayer)
                }
            )
        }

        // Bottom Half - PlayerList
        Column(modifier = Modifier.weight(1f)) {
            PlayerList(
                players = players.value,
                gymViewModel = gymViewModel
            )
        }
    }
}


// Team UI
@Composable
private fun TeamPlayerCard(
    player: Player,
    color: Color,
    modifier: Modifier = Modifier
) {
    val initial: String = player.name.first().toString()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Text(modifier = modifier
            .background(color = color)
            .padding(2.dp),
            text = initial,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TeamCard(
    team: Team,
    playersPerTeam: Int,
    gymViewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier
        .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier
            .background(color = team.color)
            .padding(6.dp)
        ) {
            LazyRow (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(team.totalTeam(playersPerTeam)) { player ->
                    val showColor = if(player == team.nullPlayer) nullTeamColor else team.darkColor
                    TeamPlayerCard(player = player, color = showColor)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CourtButton(team = team, courtNumber = 1, gymViewModel = gymViewModel)
                Spacer(modifier = modifier.weight(1f))
                Text(
                    text = "Team ${team.order} - ${team.name}",
                    color = contentColorFor(backgroundColor = team.color) // Dynamic text color
                )
                Spacer(modifier = modifier.weight(1f))
                CourtButton(team = team, courtNumber = 2, gymViewModel = gymViewModel)

            }
        }
    }
}

@Composable
fun TeamList(
    teams: List<Team>,
    playersPerTeam: Int,
    gymViewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val activeTeamsByCourt = teams.filter { it.court > 0 }
        .groupBy { it.court }
        .toSortedMap() // Sort by court number
    val inactiveTeams = teams.filter { it.court == 0 }
        .sortedBy { it.order } // Sort inactive teams by order

    LazyColumn(modifier = modifier.padding(4.dp)) {
        // Active Teams
        activeTeamsByCourt.forEach { (court, teamsOnCourt) ->
            items(teamsOnCourt) { team ->
                TeamCard(
                    team = team,
                    playersPerTeam = playersPerTeam,
                    gymViewModel = gymViewModel
                )
            }
        }

        // Inactive Teams (if any)
        if (inactiveTeams.isNotEmpty()) {
            items(inactiveTeams) { team ->
                TeamCard(
                    team = team,
                    playersPerTeam = playersPerTeam,
                    gymViewModel = gymViewModel
                )
            }
        }

        // Empty State
        if (teams.isEmpty()) {
            item {
                Text("No more teams! Go home.")
            }
        }
    }
}

@Composable
fun CourtButton(
    team: Team,
    courtNumber: Int,
    gymViewModel: GymViewModel,
    modifier: Modifier = Modifier

) {
    var showEnterCourtDialog by remember { mutableStateOf(false) }
    var showLeaveCourtDialog by remember { mutableStateOf(false) }
    var selectedCourt by remember { mutableStateOf<Int?>(null) } // Track selected court

    ElevatedButton(
        onClick = {
            selectedCourt = courtNumber
            if (team.court > 0) { showLeaveCourtDialog = true} else {showEnterCourtDialog = true }
        },
        enabled = team.court == 0 || team.court == courtNumber, // Disable if team is on the other court or has already played
        colors = ButtonDefaults.buttonColors(
            containerColor = if (team.court == courtNumber) team.darkColor else nullTeamColor
        ),
        modifier = modifier
            .padding(2.dp)
    ) {
        Text(text = "Court $courtNumber")
    }

    if (showEnterCourtDialog) {
        ConfirmEnterCourtDialog(
            team = team,
            courtNumber = selectedCourt!!,
            onConfirm = {
                gymViewModel.assignTeamToCourt(team, selectedCourt!!)
                showEnterCourtDialog = false
            },
            onDismiss = { showEnterCourtDialog = false }
        )
    }

    if (showLeaveCourtDialog) {
        ConfirmLeaveCourtDialog(
            team = team,
            onConfirm = {
                gymViewModel.removeTeamFromCourt(team)
                showLeaveCourtDialog = false
            },
            onConfirmAndNext = {
                gymViewModel.removeTeamFromCourt(team)
                val nextTeam = gymViewModel.getNextAvailableTeam()
                if (nextTeam != null) {
                    gymViewModel.assignTeamToCourt(nextTeam, courtNumber) // Assign next team to the same court
                }
                showLeaveCourtDialog = false
            },
            onDismiss = { showLeaveCourtDialog = false }
        )
    }

}

@Composable
fun ConfirmEnterCourtDialog(
    team: Team,
    courtNumber: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ready to play?") },
        text = { Text("${team.name} team playing on court $courtNumber?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Play!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmLeaveCourtDialog(
    team: Team,
    onConfirm: () -> Unit,
    onConfirmAndNext: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game over?") },
        text = { Text("${team.name} team finished on court ${team.court}?") },
        confirmButton = {
            Button(onClick = onConfirmAndNext) {
                Text("Next Team")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(onClick = onConfirm) {
                    Text("Finished")
                }
            }
        }
    )
}


// Player UI
@Composable
private fun PlayerCard(
    modifier: Modifier = Modifier,
    gymViewModel: GymViewModel = viewModel(),
    player: Player
) {
    Card(modifier = modifier
        .padding(4.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(modifier = Modifier
                .padding(4.dp),
                text = player.name,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            PlayerTeamButton(
                player = player,
                onAssignTeam = { team ->
                    gymViewModel.addPlayerToTeam(player, team.id)
                },
                onRemoveTeam = { player ->
                    gymViewModel.removePlayerFromTeam(player)
                }
            )
        }
    }
}

@Composable
fun PlayerList(
    modifier: Modifier = Modifier,
    gymViewModel: GymViewModel = viewModel(), // Receive ViewModel
    players: List<Player>
) {
    LazyColumn(modifier = modifier
        .padding(4.dp)
    ) {
        items(players) { player ->
            PlayerCard(
                player = player,
                gymViewModel = gymViewModel // Pass ViewModel
            )
        }
    }
}


@Composable
fun CreatePlayerButton(
    onAddPlayer: (Player) -> Unit, // Callback to handle adding the player
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    ElevatedButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Text("New Player")
    }

    if (showDialog) {
        PlayerInputDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, pin ->
                onAddPlayer(Player(name, pin))
                showDialog = false
            }
        )
    }
}

@Composable
fun PlayerInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, pin: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hi! What's your name?") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, pin.toIntOrNull() ?: 0) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PlayerTeamButton(
    modifier: Modifier = Modifier,
    gymViewModel: GymViewModel = viewModel(),
    player: Player,
    onAssignTeam: (Team) -> Unit, // Callback to handle adding the player to a team
    onRemoveTeam: (Player) -> Unit // Callback to handle removing the player from a team
) {
    var showAssignDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    ElevatedButton(
        onClick = {
            if (player.teamId == null) { showAssignDialog = true }
            else { showRemoveDialog = true }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = gymViewModel.teams.value.find { it.id == player.teamId}?.color ?: nullTeamColor
        ),
        modifier = modifier
    ) {
        Text(text = if (player.teamId == null) {"+"} else {" "})
    }

    if (showAssignDialog) {
        AssignPlayerToTeamDialog(
            player = player,
            availableTeams = gymViewModel.getAvailableTeams(),
            onDismiss = { showAssignDialog = false },
            onTeamSelected = { t ->
                onAssignTeam(t)
                showAssignDialog = false
            },
            gymViewModel = gymViewModel
        )
    }
    if (showRemoveDialog) {
        RemovePlayerFromTeamDialog(
            player = player,
            onDismiss = { showRemoveDialog = false },
            onConfirm = { p ->
                onRemoveTeam(p)
                showRemoveDialog = false
            }
        )
    }
}

@Composable
fun AssignPlayerToTeamDialog(
    player: Player,
    availableTeams: List<Team>,
    onDismiss: () -> Unit,
    onTeamSelected: (Team) -> Unit,
    gymViewModel: GymViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${player.name}, choose your team!") },
        text = {
            LazyColumn {
                items(availableTeams.sortedBy { it.order }) { team ->
                    val playersPerTeam = gymViewModel.uiState.collectAsState().value.playersPerTeam
                    val teamSpotsAvailable = playersPerTeam - team.players.size

                    TextButton(onClick = { onTeamSelected(team) }) {
                        // Added surface for background color, rounded corners and depth
                        Surface(
                            color = team.color,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .shadow(elevation = 4.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "Team ${team.order} - ${team.name} ($teamSpotsAvailable of $playersPerTeam available)",
                                color = contentColorFor(backgroundColor = team.color), // Dynamic text color
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun RemovePlayerFromTeamDialog(
    player: Player,
    onDismiss: () -> Unit,
    onConfirm: (Player) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Removal") },
        text = { Text("${player.name}, are you sure you want to leave your team?") },
        confirmButton = {
            Button(onClick = { onConfirm(player) }) {
                Text("Leave")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GetInTheGameTheme {
        MainScreen()
    }
}


@Preview(showBackground = true)
@Composable
fun PlayersPreview() {
    GetInTheGameTheme {
        val player1 = Player("Al", 1111)
        val player2 = Player("Bob", 2222)
        val player3 = Player("Carol", 3333)
        val player4 = Player("Dan", 4444)

        val players = listOf(
            player1,
            player2,
            player3,
            player4
        )
        PlayerList(players = players)
    }
}


@Preview(showBackground = true)
@Composable
fun TeamsPreview() {
    GetInTheGameTheme {
        val player1 = Player("Al", 1111)
        val player2 = Player("Bob", 2222)
        val player3 = Player("Carol", 3333)
        val player4 = Player("Dan", 4444)

        val team1 = Team(color = Color.Red, players = listOf(player1, player2))
        val team2 = Team(color = Color.Blue, players = listOf(player3, player4))

        val teams = listOf(
            team1,
            team2
        )
        TeamList(teams, 6, gymViewModel = viewModel())
    }
}