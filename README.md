# Tic Tac Toe Board

[![JitPack](https://jitpack.io/v/niraj-prajapati/tic-tac-toe-board.svg)](https://jitpack.io/#niraj-prajapati/tic-tac-toe-board)

A customizable Tic Tac Toe game board Android library with built-in animations, AI opponent, and
various customization options.

## Features

- 🎮 Built-in AI opponent with multiple difficulty levels
- ✨ Smooth animations for X and O moves
- 🎨 Highly customizable appearance
- 🔊 Sound effects and haptic feedback
- 🎯 Visual feedback for game state
- 📱 Supports Android API level 26+

## Installation

Add the JitPack repository to your project's `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.niraj-prajapati:tic-tac-toe-board:LATEST_VERSION'
}
```

## Quick Start

Add the TicTacToeBoard to your layout:

```xml

<com.niraj.tictactoeboard.TicTacToeBoard android:id="@+id/tictactoeBoard" android:layout_width="0dp"
    android:layout_height="0dp" app:ttb_boardLineColor="#000000" app:ttb_boardLineWidth="4dp"
    app:ttb_lineWidth="8dp" app:ttb_boardPadding="8dp" app:ttb_xColor="#2196F3"
    app:ttb_oColor="#F44336" app:ttb_cellPadding="0.2" app:ttb_autoPlay="true"
    app:ttb_aiDifficulty="medium" app:ttb_enableSound="true" app:ttb_enableVibration="true"
    app:ttb_enableHapticFeedback="true" app:ttb_highlightWinningCells="true"
    app:ttb_showPlayerTurn="true" />
```

Set up the game state listener in your activity:

```kotlin
tictactoeBoard.setOnGameStateChangeListener { currentPlayer, isGameOver, winner ->
    when {
        isGameOver && winner != Player.NONE -> {
            // Game won by winner
            updateStatus("Player $winner wins!")
        }
        isGameOver -> {
            // Game ended in a draw
            updateStatus("Game ended in a draw!")
        }
        else -> {
            // Game in progress
            updateStatus("Player $currentPlayer's turn")
        }
    }
}
```

## Customization

### XML Attributes

#### Board Appearance

| Attribute            | Type      | Default | Description                   |
|----------------------|-----------|---------|-------------------------------|
| `ttb_boardLineColor` | color     | BLACK   | Color of the board grid lines |
| `ttb_boardLineWidth` | dimension | 4dp     | Width of the board grid lines |
| `ttb_lineWidth`      | dimension | 8dp     | Width of X, O, and win lines  |
| `ttb_boardPadding`   | dimension | 0dp     | Padding around the board      |

#### X and O Styling

| Attribute    | Type  | Default | Description       |
|--------------|-------|---------|-------------------|
| `ttb_xColor` | color | BLUE    | Color of X symbol |
| `ttb_oColor` | color | RED     | Color of O symbol |

#### Cell Customization

| Attribute         | Type  | Default | Description                           |
|-------------------|-------|---------|---------------------------------------|
| `ttb_cellPadding` | float | 0.2     | Padding inside each cell (0.0 to 0.5) |

#### Game Behavior

| Attribute                  | Type    | Default | Description                            |
|----------------------------|---------|---------|----------------------------------------|
| `ttb_autoPlay`             | boolean | false   | Enable/disable AI opponent             |
| `ttb_aiDifficulty`         | enum    | medium  | AI difficulty level (easy/medium/hard) |
| `ttb_enableSound`          | boolean | false   | Enable/disable sound effects           |
| `ttb_enableVibration`      | boolean | false   | Enable/disable vibration feedback      |
| `ttb_enableHapticFeedback` | boolean | false   | Enable/disable haptic feedback         |

#### Visual Feedback

| Attribute                   | Type    | Default | Description                          |
|-----------------------------|---------|---------|--------------------------------------|
| `ttb_highlightWinningCells` | boolean | false   | Highlight cells when game is won     |
| `ttb_showPlayerTurn`        | boolean | false   | Show current player's turn indicator |

### Programmatic Customization

All attributes can be set programmatically:

```kotlin
tictactoeBoard.apply {
    // Board Appearance
    setBoardLineColor(Color.BLACK)
    setBoardLineWidth(4f)
    setLineWidth(8f)
    setBoardPadding(8f)

    // X and O Styling
    setXColor(Color.BLUE)
    setOColor(Color.RED)

    // Cell Customization
    setCellPadding(0.2f)

    // Game Behavior
    setAutoPlay(true)
    setAiDifficulty(AIDifficulty.MEDIUM)
    setEnableSound(true)
    setEnableVibration(true)
    setEnableHapticFeedback(true)

    // Visual Feedback
    setHighlightWinningCells(true)
    setShowPlayerTurn(true)
}
```

## API Reference

### Game Control Methods

```kotlin
// Reset the game board
fun reset()

// Set listener for game state changes
fun setOnGameStateChangeListener(listener: (Player, Boolean, Player) -> Unit)
```

### Game State Listener

The game state listener receives:

- `currentPlayer`: Current player (X, O, or NONE)
- `isGameOver`: Whether the game is over
- `winner`: The winner (X, O, or NONE for draw)

### AI Difficulty Levels

```kotlin
enum class AIDifficulty {
    EASY,    // Makes random moves
    MEDIUM,  // Mix of random and smart moves
    HARD     // Uses minimax algorithm
}
```

## Requirements

- Android API level 26 (Android 8.0) or higher
- Kotlin 1.8.0 or higher

## Permissions

The library requires the following permissions in your `AndroidManifest.xml`:

```xml

<uses-permission android:name="android.permission.VIBRATE" />
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. 