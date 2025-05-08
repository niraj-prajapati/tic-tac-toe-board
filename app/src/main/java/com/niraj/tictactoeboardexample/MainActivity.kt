package com.niraj.tictactoeboardexample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.niraj.tictactoeboard.Player
import com.niraj.tictactoeboard.TicTacToeBoard

class MainActivity : AppCompatActivity() {
    private lateinit var ticTacToeBoard: TicTacToeBoard
    private lateinit var statusText: TextView
    private lateinit var resetButton: Button
    private lateinit var playerXScore: TextView
    private lateinit var playerOScore: TextView

    private var scoreX = 0
    private var scoreO = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ticTacToeBoard = findViewById(R.id.tictactoeBoard)
        statusText = findViewById(R.id.statusText)
        resetButton = findViewById(R.id.resetButton)
        playerXScore = findViewById(R.id.playerXScore)
        playerOScore = findViewById(R.id.playerOScore)

        setupGame()
    }

    private fun setupGame() {
        ticTacToeBoard.setOnGameStateChangeListener { currentPlayer, isGameOver, winner ->
            updateGameStatus(currentPlayer, isGameOver, winner)
        }

        resetButton.setOnClickListener {
            ticTacToeBoard.reset()
        }
    }

    private fun updateGameStatus(currentPlayer: Player, isGameOver: Boolean, winner: Player) {
        statusText.text = when {
            isGameOver && winner == Player.NONE -> "Game Draw!"
            isGameOver -> {
                updateScore(winner)
                "Player ${winner.name} Wins!"
            }

            else -> "Player ${currentPlayer.name}'s turn"
        }
    }

    private fun updateScore(winner: Player) {
        when (winner) {
            Player.X -> {
                scoreX++
                playerXScore.text = "Player X: $scoreX"
            }

            Player.O -> {
                scoreO++
                playerOScore.text = "Player O: $scoreO"
            }

            Player.NONE -> {} // Draw, no score update
        }
    }
}