package com.example.getinthegame.ui

import java.util.UUID

data class Player(
    val name: String,
    private val pin: Int,
    var teamId: UUID? = null,
    val id: UUID = UUID.randomUUID()
) {
    fun isCorrectPin(pin: Int) = this.pin == pin

}