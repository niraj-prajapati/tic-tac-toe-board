package com.niraj.tictactoeboardexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.niraj.tictactoeboardexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsManager: SettingsManager

    companion object {
        private const val VIBRATE_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.apply {
            ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        settingsManager = SettingsManager(this)
        setupClickListeners()
        checkPermissions()
        updateDifficultySelection()
    }

    private fun setupClickListeners() = binding.apply {
        difficultyChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipEasy -> settingsManager.difficulty = "Easy"
                R.id.chipMedium -> settingsManager.difficulty = "Medium"
                R.id.chipHard -> settingsManager.difficulty = "Hard"
            }
        }

        startGameButton.setOnClickListener {
            startGame()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

        aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun updateDifficultySelection() = binding.apply {
        val currentDifficulty = settingsManager.difficulty
        when (currentDifficulty) {
            "Easy" -> difficultyChipGroup.check(R.id.chipEasy)
            "Medium" -> difficultyChipGroup.check(R.id.chipMedium)
            "Hard" -> difficultyChipGroup.check(R.id.chipHard)
        }
    }

    private fun checkPermissions() {
        if (settingsManager.vibrationEnabled && !hasVibratePermission()) {
            requestVibratePermission()
        }
    }

    private fun hasVibratePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestVibratePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.VIBRATE),
            VIBRATE_PERMISSION_REQUEST
        )
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("About Tic Tac Toe")
            .setMessage(
                "A beautiful and modern Tic Tac Toe game built with Android.\n\n" +
                        "Features:\n" +
                        "• Beautiful animations\n" +
                        "• Sound effects\n" +
                        "• Haptic feedback\n" +
                        "• Multiple difficulty levels\n" +
                        "• Score tracking\n\n" +
                        "Multiplayer support coming soon!"
            )
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            VIBRATE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    settingsManager.vibrationEnabled = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh difficulty selection when returning from settings
        updateDifficultySelection()
    }
}