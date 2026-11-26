package com.example.game.domain

/**
 * Игрок (X или O)
 */
enum class Player {
    X,
    O;

    val opponent: Player
        get() = if (this === X) O else X

    val displayName: String
        get() = this.name
}
