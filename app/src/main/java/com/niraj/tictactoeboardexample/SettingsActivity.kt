package com.niraj.tictactoeboardexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.niraj.tictactoeboardexample.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsManager: SettingsManager

    companion object {
        private const val VIBRATE_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.apply {
            ViewCompat.setOnApplyWindowInsetsListener(settingsContainer) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        settingsManager = SettingsManager(this)
        setupClickListeners()
        loadSettings()
    }

    private fun setupClickListeners() = binding.apply {
        backButton.setOnClickListener {
            finish()
        }

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.soundEnabled = isChecked
        }

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.vibrationEnabled = isChecked
            if (isChecked && !hasVibratePermission()) {
                requestVibratePermission()
            }
        }

        hapticSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.hapticEnabled = isChecked
        }
    }

    private fun loadSettings() = binding.apply {
        soundSwitch.isChecked = settingsManager.soundEnabled
        vibrationSwitch.isChecked = settingsManager.vibrationEnabled
        hapticSwitch.isChecked = settingsManager.hapticEnabled
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            VIBRATE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    binding.vibrationSwitch.isChecked = false
                    settingsManager.vibrationEnabled = false
                }
            }
        }
    }
} 