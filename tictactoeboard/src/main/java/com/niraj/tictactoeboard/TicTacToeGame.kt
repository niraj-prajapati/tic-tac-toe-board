package com.niraj.tictactoeboard

enum class Player {
    X, O, NONE
}

class TicTacToeGame {
    private val board = Array(3) { Array(3) { Player.NONE } }
    private var currentPlayer = Player.X
    private var gameOver = false
    private var winner: Player = Player.NONE

    fun makeMove(row: Int, col: Int): Boolean {
        if (gameOver || row !in 0..2 || col !in 0..2 || board[row][col] != Player.NONE) {
            return false
        }

        board[row][col] = currentPlayer
        checkGameState()
        switchPlayer()
        return true
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
    }

    private fun checkGameState() {
        // Check rows
        for (row in 0..2) {
            if (board[row][0] != Player.NONE &&
                board[row][0] == board[row][1] &&
                board[row][1] == board[row][2]
            ) {
                gameOver = true
                winner = board[row][0]
                return
            }
        }

        // Check columns
        for (col in 0..2) {
            if (board[0][col] != Player.NONE &&
                board[0][col] == board[1][col] &&
                board[1][col] == board[2][col]
            ) {
                gameOver = true
                winner = board[0][col]
                return
            }
        }

        // Check diagonals
        if (board[0][0] != Player.NONE &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]
        ) {
            gameOver = true
            winner = board[0][0]
            return
        }

        if (board[0][2] != Player.NONE &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]
        ) {
            gameOver = true
            winner = board[0][2]
            return
        }

        // Check for draw
        if (board.all { row -> row.all { it != Player.NONE } }) {
            gameOver = true
            winner = Player.NONE
        }
    }

    fun getCurrentPlayer(): Player = currentPlayer
    fun isGameOver(): Boolean = gameOver
    fun getWinner(): Player = winner
    fun getBoard(): Array<Array<Player>> = board
    fun reset() {
        for (row in 0..2) {
            for (col in 0..2) {
                board[row][col] = Player.NONE
            }
        }
        currentPlayer = Player.X
        gameOver = false
        winner = Player.NONE
    }
} 