package com.example.getinthegame.ui

import java.util.UUID

data class Player(
    val name: String,
    val pin: Int,
    var teamId: UUID? = null,
    val id: UUID = UUID.randomUUID()
)