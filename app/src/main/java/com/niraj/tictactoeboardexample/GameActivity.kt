package com.niraj.tictactoeboardexample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.niraj.tictactoeboard.AIDifficulty
import com.niraj.tictactoeboard.Player
import com.niraj.tictactoeboardexample.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private var scoreX = 0
    private var scoreO = 0
    private var draws = 0
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.apply {
            ViewCompat.setOnApplyWindowInsetsListener(gameContainer) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        settingsManager = SettingsManager(this)
        setupGame()
        updateScoreDisplay()
    }

    private fun setupGame() = binding.apply {
        // Configure the board based on settings from SettingsManager
        tictactoeBoard.apply {
            setEnableSound(settingsManager.soundEnabled)
            setEnableVibration(settingsManager.vibrationEnabled)
            setEnableHapticFeedback(settingsManager.hapticEnabled)
            setAiDifficulty(getDifficultyFromString(settingsManager.difficulty))
            setAutoPlay(true)
            setHighlightWinningCells(true)
            setShowPlayerTurn(true)
        }

        tictactoeBoard.setOnGameStateChangeListener { currentPlayer, isGameOver, winner ->
            updateGameStatus(currentPlayer, isGameOver, winner)
        }

        resetButton.setOnClickListener {
            tictactoeBoard.reset()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getDifficultyFromString(difficulty: String): AIDifficulty {
        return when (difficulty.lowercase()) {
            "easy" -> AIDifficulty.EASY
            "medium" -> AIDifficulty.MEDIUM
            "hard" -> AIDifficulty.HARD
            else -> AIDifficulty.EASY
        }
    }

    private fun updateGameStatus(currentPlayer: Player, isGameOver: Boolean, winner: Player) =
        binding.apply {
            statusText.text = when {
                isGameOver && winner == Player.NONE -> {
                    draws++
                    updateScoreDisplay()
                    "Game Draw!"
                }

                isGameOver -> {
                    updateScore(winner)
                    "Player ${winner.name} Wins!"
                }

                else -> "Player ${currentPlayer.name}'s turn"
            }
        }

    private fun updateScore(winner: Player) {
        when (winner) {
            Player.X -> scoreX++
            Player.O -> scoreO++
            Player.NONE -> {} // Draw, no score update
        }
        updateScoreDisplay()
    }

    private fun updateScoreDisplay() = binding.apply {
        playerXScore.text = "X: $scoreX"
        playerOScore.text = "O: $scoreO"
        playerXWins.text = "Wins: $scoreX"
        playerOWins.text = "Wins: $scoreO"
        drawsText.text = "Draws: $draws"
    }
} 