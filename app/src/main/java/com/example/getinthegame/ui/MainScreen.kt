package com.example.getinthegame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.getinthegame.ui.theme.GetInTheGameTheme
import com.example.getinthegame.ui.theme.nullTeamColor
import com.example.getinthegame.ui.theme.nullTeamTextColor


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    gymViewModel: GymViewModel = viewModel()
) {
    val uiState = gymViewModel.uiState.collectAsState()
    val teams = uiState.value.teams.values.toList()
    val players = uiState.value.players

    Column(modifier = modifier.padding(paddingValues)) {
        PlayerCountControl(
            playersPerTeam = uiState.value.playersPerTeam,
            onIncrease = { gymViewModel.increasePlayersPerTeam() },
            onDecrease = { gymViewModel.decreasePlayersPerTeam() }
        )

        // Top Half - TeamList
        Column(modifier = Modifier.weight(1f)) {
            TeamList(
                teams = teams,
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
                players = players,
                gymViewModel = gymViewModel
            )
        }
    }

    // Show the ChooseTeamToRemoveDialog if needed
    val courtToRemoveTeamFrom = uiState.value.courtToRemoveTeamFrom
    val teamToAssign = uiState.value.teamToAssign
    if (courtToRemoveTeamFrom != null && teamToAssign != null) {
        val teamsOnCourt = teams.filter { it.court == courtToRemoveTeamFrom }
        ChooseTeamToRemoveDialog(
            courtNumber = courtToRemoveTeamFrom,
            teamsOnCourt = teamsOnCourt,
            onTeamSelected = { teamToRemove ->
                gymViewModel.removeTeamFromCourt(teamToRemove)
                gymViewModel.assignTeamToCourt(teamToAssign, courtToRemoveTeamFrom)
            },
            onDismiss = { gymViewModel.clearTeamRemovalState() }
        )
    }

    // Show the NextTeamDialog if needed
    val teamToAssignNext = uiState.value.teamToAssign
    val skippedTeam = uiState.value.skippedTeam
    if (teamToAssignNext != null && skippedTeam != null) {
        SkippedTeamDialog(
            nextTeam = teamToAssignNext,
            skippedTeam = skippedTeam,
            onDismiss = { gymViewModel.clearTeamOrderPrompt() }
        )
    }
}

@Composable
fun PlayerCountControl(
    playersPerTeam: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDecrease,
            enabled = playersPerTeam > 6,
            shape = RoundedCornerShape(
                topStart = 16.dp,        // Top left corner (square)
                topEnd = 0.dp,         // Top right corner (rounded)
                bottomEnd = 0.dp,      // Bottom right corner (rounded)
                bottomStart = 16.dp     // Bottom left corner (square)
            )
        ) {
            Text(text = "-")
        }
        Text(text = "Players per Team: $playersPerTeam")
        Button(
            onClick = onIncrease,
            enabled = playersPerTeam < 8,
            shape = RoundedCornerShape(
                topStart = 0.dp,        // Top left corner (square)
                topEnd = 16.dp,         // Top right corner (rounded)
                bottomEnd = 16.dp,      // Bottom right corner (rounded)
                bottomStart = 0.dp     // Bottom left corner (square)
            )
        ) {
            Text(text = "+")
        }
    }
}


// Team UI
@Composable
private fun TeamPlayerCard(
    player: Player,
    color: Color,
    textColor: Color,
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
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TeamCard(
    team: Team,
    gymViewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by gymViewModel.uiState.collectAsState()
    val isSelected = uiState.selectedTeam == team

    Card(modifier = modifier
        .padding(4.dp)
        .clickable {
            if (isSelected) {
                gymViewModel.clearSelectedTeam() // Clear selection if already selected
            } else {
                if (team.players.isNotEmpty()) {
                    gymViewModel.selectTeam(team) // Select if not selected and has players
                }
            }
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 16.dp else 12.dp)
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
                items(team.totalTeam()) { player ->
                    val showColor = if(player == team.nullPlayer) nullTeamColor else team.darkColor
                    val textColor = if(player == team.nullPlayer) nullTeamTextColor else team.textColor
                    TeamPlayerCard(player = player, color = showColor, textColor = textColor)
                }
            }
            // Conditionally display the player list
            if (isSelected) {
                PlayerPreviewList(team)
            }
            Text(
                text = team.name,
                color = team.textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CourtButton(team = team, courtNumber = 1, gymViewModel = gymViewModel)
                CourtButton(team = team, courtNumber = 2, gymViewModel = gymViewModel)
            }
            Text(
                text = team.currentOpponent?.name ?: "",
                color = team.textColor,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PlayerPreviewList(
    team: Team
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        team.players.forEach { player ->
            Text(
                text = "- ${player.name}",
                color = team.textColor
            )
        }
    }
}

@Composable
fun TeamList(
    teams: List<Team>,
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
                    gymViewModel = gymViewModel
                )
            }
        }

        // Inactive Teams (if any)
        if (inactiveTeams.isNotEmpty()) {
            items(inactiveTeams) { team ->
                TeamCard(
                    team = team,
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

    val oppositeCourt = if (courtNumber == 1) 2 else 1
    val courtText = when {
        team.court == courtNumber -> "ON\nCourt $courtNumber"
        team.currentOpponent?.court == oppositeCourt -> "vs"
        else -> "Court $courtNumber"
    }
    val courtColor = when {
        team.court == courtNumber -> team.darkColor
        team.currentOpponent?.court == oppositeCourt -> team.currentOpponent?.darkColor ?: nullTeamColor
        else -> nullTeamColor
    }
    val textColor = when {
        team.court == courtNumber -> team.textColor
        team.currentOpponent?.court == oppositeCourt -> team.currentOpponent?.textColor ?: nullTeamTextColor
        else -> nullTeamTextColor
    }

    Column {
        ElevatedButton(
            onClick = {
                selectedCourt = courtNumber
                when {
                    team.court == courtNumber -> showLeaveCourtDialog = true // Team is on this court, show Leave dialog
                    team.court == oppositeCourt -> {} // Do nothing if no opponent
                    team.currentOpponent?.court == oppositeCourt -> {} // Do nothing if opponent button selected
                    else -> showEnterCourtDialog = true // Team is not on any court, show Enter dialog
                }
            },
            // enabled = team.court == 0 || team.court == courtNumber, // Disable if team is on the other court or has already played
            colors = ButtonDefaults.buttonColors(containerColor = courtColor),
            modifier = modifier
                .padding(2.dp)
        ) {
            Text(
                text = courtText,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
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
        text = {
            Column {
                Text("Entering court $courtNumber?")
                Spacer(modifier = Modifier.height(8.dp))
                TeamCardForDialog(team = team)
            }
        },
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
        text = {
            Column {
                Text("Finished on court ${team.court}?")
                Spacer(modifier = Modifier.height(8.dp))
                TeamCardForDialog(team = team)
            }
       },
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

@Composable
fun TeamCardForDialog(
    team: Team,
    modifier: Modifier = Modifier
) {
    Surface(
        color = team.color,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .shadow(elevation = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "${team.name} (${team.availableSpotsText})",
            color = team.textColor,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ChooseTeamToRemoveDialog(
    courtNumber: Int,
    teamsOnCourt: List<Team>,
    onTeamSelected: (Team) -> Unit,
    onDismiss: () -> Unit
) {
    val lowerOrderTeam = teamsOnCourt.minByOrNull { it.order } // Find the team with the lowest order

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Court $courtNumber is Full") },
        text = {
            Column {
                Text("Choose a team to remove:")
                LazyColumn {
                    items(teamsOnCourt) { team ->
                        TextButton(onClick = { onTeamSelected(team) }) {
                            TeamCardForDialog(
                                team = team,
                                modifier = Modifier
                                    .then(
                                        if (team == lowerOrderTeam) {
                                            Modifier.border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            // No confirm button, selection happens within the dialog
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SkippedTeamDialog(
    nextTeam: Team,
    skippedTeam: Team,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Out of Order!") },
        text = {
            Column {
                Text("${skippedTeam.name} should play before ${nextTeam.name}.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Go back and select ${skippedTeam.name}.")
                Spacer(modifier = Modifier.height(16.dp))
                TeamCardForDialog(team = skippedTeam)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Okay")
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
    val sortedPlayers = players.sortedWith(
        compareBy<Player> { it.teamId } // Sort by teamId first (nulls last)
            .thenBy { it.name }         // Then sort by name within each team
    )
    LazyColumn(modifier = modifier
        .padding(4.dp)
    ) {
        items(sortedPlayers) { player ->
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
fun PinInputDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

// Function to handle PIN confirmation
    fun handlePinConfirmation() {
        if (enteredPin.length >= 4) {
            onConfirm(enteredPin.toInt())
            focusManager.clearFocus()
        } else {
            errorMessage = "Invalid PIN"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { enteredPin = it },
                    label = { Text("Enter PIN") },
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { handlePinConfirmation() }
                    ),
                    modifier = Modifier
                        .onKeyEvent {
                            if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                                handlePinConfirmation()
                                true // Consume the event
                            } else {
                                false // Don't consume the event
                            }
                        }
                        .focusRequester(focusRequester)
                )
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        enabled = enteredPin.length >= 4,
                        onClick = { handlePinConfirmation() }
                    ) { Text("Confirm") }
                }
            }
        }
        // Request focus when the dialog is shown
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}

@Composable
fun PinErrorDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "The PIN you entered is incorrect.",
                    color = Color.Red
                )
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
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
            Button(
                onClick = { onConfirm(name, pin.toIntOrNull() ?: 0) },
                enabled = name.isNotBlank() && pin.length >= 4
            ) {
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
    var showGetPinDialog by remember { mutableStateOf(false) }
    var joiningTeam by remember { mutableStateOf(false) }
    var leavingTeam by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val uiState by gymViewModel.uiState.collectAsState()
    val team = uiState.teams.values.find { it.id == player.teamId }
    val teamColor = team?.color ?: nullTeamColor
    val textColor = team?.textColor ?: nullTeamTextColor

    ElevatedButton(
        onClick = {
            if (player.teamId == null) { joiningTeam = true }
            else { leavingTeam = true }
            showGetPinDialog = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = teamColor
        ),
        modifier = modifier
    ) {
        Text(
            text = if (player.teamId == null) {"+"} else {"-"},
            color = textColor
        )
    }

    if (showGetPinDialog) {
        PinInputDialog(
            onConfirm = { enteredPin ->
                if (player.isCorrectPin(enteredPin)) {
                    // Proceed with join/leave team action
                    if (joiningTeam) {
                        showAssignDialog = true
                        joiningTeam = false // Reset joiningTeam
                    } else if (leavingTeam) {
                        showRemoveDialog = true
                        leavingTeam = false // Reset leavingTeam
                    }
                } else {
                    // Show error message
                    showErrorDialog = true
                }
                showGetPinDialog = false
            },
            onDismiss = { showGetPinDialog = false }
        )
    }

    if (showErrorDialog) {
        PinErrorDialog(onDismiss = { showErrorDialog = false })
    }

    if (showAssignDialog) {
        AssignPlayerToTeamDialog(
            player = player,
            availableTeams = gymViewModel.getAvailableTeams(),
            onDismiss = { showAssignDialog = false },
            onTeamSelected = { t ->
                onAssignTeam(t)
                showAssignDialog = false
            }
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
    onTeamSelected: (Team) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${player.name}, choose your team!") },
        text = {
            LazyColumn {
                items(availableTeams.sortedBy { it.order }) { team ->

                    TextButton(onClick = { onTeamSelected(team) }) {
                        TeamCardForDialog(team = team)
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
        TeamList(teams, gymViewModel = viewModel())
    }
}