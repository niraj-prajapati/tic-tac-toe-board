package com.niraj.tictactoeboard

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import kotlin.math.min

/**
 * A custom view that implements a Tic Tac Toe game board with various customization options.
 *
 * Features:
 * - Customizable board appearance (colors, line widths, padding)
 * - Built-in animations for X and O
 * - AI opponent with multiple difficulty levels
 * - Sound effects and haptic feedback
 * - Visual feedback for game state
 *
 * @property onGameStateChangeListener Callback for game state changes
 */
class TicTacToeBoard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    // Default values
    companion object {
        private const val DEFAULT_BOARD_LINE_COLOR = Color.BLACK
        private const val DEFAULT_BOARD_LINE_WIDTH = 4f
        private const val DEFAULT_LINE_WIDTH = 8f
        private const val DEFAULT_X_COLOR = Color.BLUE
        private const val DEFAULT_O_COLOR = Color.RED
        private const val DEFAULT_CELL_PADDING = 0.2f
        private const val DEFAULT_BOARD_PADDING = 0f
        private const val DEFAULT_ANIMATION_DURATION = 300L
    }

    // Paints
    private val boardPaint = Paint().apply {
        color = DEFAULT_BOARD_LINE_COLOR
        strokeWidth = DEFAULT_BOARD_LINE_WIDTH
        isAntiAlias = true
    }

    private val xPaint = Paint().apply {
        color = DEFAULT_X_COLOR
        strokeWidth = DEFAULT_LINE_WIDTH
        isAntiAlias = true
    }

    private val oPaint = Paint().apply {
        color = DEFAULT_O_COLOR
        strokeWidth = DEFAULT_LINE_WIDTH
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Board properties
    private var cellSize = 0f
    private var boardWidth = 0f
    private var boardHeight = 0f
    private var boardPadding = DEFAULT_BOARD_PADDING
    private var cellPadding = DEFAULT_CELL_PADDING
    private var boardLineWidth = DEFAULT_BOARD_LINE_WIDTH
    private var lineWidth = DEFAULT_LINE_WIDTH

    // Game state
    private val game = TicTacToeGame()
    private var onGameStateChangeListener: ((Player, Boolean, Player) -> Unit)? = null
    private var winningCells: List<Pair<Int, Int>> = emptyList()
    private var isProcessingMove = false

    // Animation properties
    private val animations = mutableMapOf<Pair<Int, Int>, ValueAnimator>()
    private val animationProgress = mutableMapOf<Pair<Int, Int>, Float>()

    // Game behavior
    private var enableSound = false
    private var enableVibration = false
    private var enableHapticFeedback = false
    private var autoPlay = false
    private var aiDifficulty = AIDifficulty.MEDIUM

    // Visual feedback
    private var highlightWinningCells = false
    private var showPlayerTurn = false

    // Sound effects
    private var moveSound: MediaPlayer? = null
    private var winSound: MediaPlayer? = null
    private var drawSound: MediaPlayer? = null

    init {
        // Load custom attributes
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TicTacToeBoard)
        try {
            // Board customization
            boardPaint.color = typedArray.getColor(
                R.styleable.TicTacToeBoard_ttb_boardLineColor,
                DEFAULT_BOARD_LINE_COLOR
            )
            boardLineWidth = typedArray.getDimension(
                R.styleable.TicTacToeBoard_ttb_boardLineWidth,
                DEFAULT_BOARD_LINE_WIDTH
            )
            lineWidth = typedArray.getDimension(
                R.styleable.TicTacToeBoard_ttb_lineWidth,
                DEFAULT_LINE_WIDTH
            )
            boardPadding = typedArray.getDimension(
                R.styleable.TicTacToeBoard_ttb_boardPadding,
                DEFAULT_BOARD_PADDING
            )

            // Apply line widths
            boardPaint.strokeWidth = boardLineWidth
            xPaint.strokeWidth = lineWidth
            oPaint.strokeWidth = lineWidth

            // X customization
            xPaint.color =
                typedArray.getColor(R.styleable.TicTacToeBoard_ttb_xColor, DEFAULT_X_COLOR)

            // O customization
            oPaint.color =
                typedArray.getColor(R.styleable.TicTacToeBoard_ttb_oColor, DEFAULT_O_COLOR)

            // Cell customization
            cellPadding = typedArray.getFloat(
                R.styleable.TicTacToeBoard_ttb_cellPadding,
                DEFAULT_CELL_PADDING
            )

            // Game behavior
            enableSound = typedArray.getBoolean(R.styleable.TicTacToeBoard_ttb_enableSound, false)
            enableVibration =
                typedArray.getBoolean(R.styleable.TicTacToeBoard_ttb_enableVibration, false)
            enableHapticFeedback =
                typedArray.getBoolean(R.styleable.TicTacToeBoard_ttb_enableHapticFeedback, false)
            autoPlay = typedArray.getBoolean(R.styleable.TicTacToeBoard_ttb_autoPlay, false)
            val aiDifficulty = typedArray.getInteger(R.styleable.TicTacToeBoard_ttb_aiDifficulty, 1)
            this.aiDifficulty = AIDifficulty.entries.toTypedArray()[aiDifficulty]

            // Visual feedback
            highlightWinningCells =
                typedArray.getBoolean(R.styleable.TicTacToeBoard_ttb_highlightWinningCells, false)
            showPlayerTurn =
                typedArray.getBoolean(R.styleable.TicTacToeBoard_ttb_showPlayerTurn, false)

            // Initialize sound effects if enabled
            if (enableSound) {
                initializeSounds()
            }
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * Sets a listener for game state changes.
     * The listener receives:
     * - Current player (X, O, or NONE)
     * - Whether the game is over
     * - The winner (X, O, or NONE for draw)
     */
    fun setOnGameStateChangeListener(listener: (Player, Boolean, Player) -> Unit) {
        onGameStateChangeListener = listener
    }

    /**
     * Resets the game board to its initial state.
     * Clears all moves, animations, and resets the game state.
     */
    fun reset() {
        // Cancel all running animations
        animations.values.forEach { it.cancel() }
        animations.clear()
        animationProgress.clear()

        // Reset game state
        game.reset()
        winningCells = emptyList()
        isProcessingMove = false

        invalidate()
        onGameStateChangeListener?.invoke(
            game.getCurrentPlayer(),
            game.isGameOver(),
            game.getWinner()
        )
    }

    private fun initializeSounds() {
        try {
            moveSound = MediaPlayer.create(context, R.raw.move)
            winSound = MediaPlayer.create(context, R.raw.win)
            drawSound = MediaPlayer.create(context, R.raw.draw)
        } catch (e: Exception) {
            // Handle sound resource loading error
            enableSound = false
        }
    }

    private fun playMoveSound() {
        if (enableSound) {
            try {
                moveSound?.apply {
                    if (isPlaying) {
                        seekTo(0)
                    }
                    start()
                }
            } catch (e: Exception) {
                // Handle sound playback error
            }
        }
    }

    private fun playWinSound() {
        if (enableSound) {
            try {
                winSound?.apply {
                    if (isPlaying) {
                        seekTo(0)
                    }
                    start()
                }
            } catch (e: Exception) {
                // Handle sound playback error
            }
        }
    }

    private fun playDrawSound() {
        if (enableSound) {
            try {
                drawSound?.apply {
                    if (isPlaying) {
                        seekTo(0)
                    }
                    start()
                }
            } catch (e: Exception) {
                // Handle sound playback error
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = (w - 2 * boardPadding) / 3
        boardWidth = w.toFloat()
        boardHeight = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawCells(canvas)
        drawMoves(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        // Draw vertical lines
        for (i in 1..2) {
            canvas.drawLine(
                boardPadding + cellSize * i,
                boardPadding,
                boardPadding + cellSize * i,
                boardHeight - boardPadding,
                boardPaint
            )
        }

        // Draw horizontal lines
        for (i in 1..2) {
            canvas.drawLine(
                boardPadding,
                boardPadding + cellSize * i,
                boardWidth - boardPadding,
                boardPadding + cellSize * i,
                boardPaint
            )
        }
    }

    private fun drawCells(canvas: Canvas) {
        for (row in 0..2) {
            for (col in 0..2) {
                val cellRect = RectF(
                    boardPadding + col * cellSize,
                    boardPadding + row * cellSize,
                    boardPadding + (col + 1) * cellSize,
                    boardPadding + (row + 1) * cellSize
                )

                // Highlight winning cells
                if (highlightWinningCells && winningCells.contains(Pair(row, col))) {
                    canvas.drawRect(cellRect, Paint().apply {
                        color = Color.GREEN
                        alpha = 50
                        style = Paint.Style.FILL
                    })
                }
            }
        }

        // Draw player turn indicator
        if (showPlayerTurn && !game.isGameOver()) {
            val currentPlayer = game.getCurrentPlayer()
            val indicatorPaint = Paint().apply {
                color = when (currentPlayer) {
                    Player.X -> xPaint.color
                    Player.O -> oPaint.color
                    Player.NONE -> Color.GRAY
                }
                alpha = 30
                style = Paint.Style.FILL
            }
            canvas.drawRect(
                RectF(
                    boardPadding,
                    boardPadding,
                    boardWidth - boardPadding,
                    boardHeight - boardPadding
                ),
                indicatorPaint
            )
        }
    }

    private fun drawMoves(canvas: Canvas) {
        val board = game.getBoard()
        for (row in 0..2) {
            for (col in 0..2) {
                when (board[row][col]) {
                    Player.X -> drawX(canvas, row, col)
                    Player.O -> drawO(canvas, row, col)
                    Player.NONE -> {}
                }
            }
        }
    }

    private fun drawX(canvas: Canvas, row: Int, col: Int) {
        val padding = cellSize * cellPadding
        val progress = animationProgress[Pair(row, col)] ?: 1f

        // Draw first diagonal line with animation
        canvas.drawLine(
            boardPadding + col * cellSize + padding,
            boardPadding + row * cellSize + padding,
            boardPadding + col * cellSize + padding + (cellSize - padding * 2) * progress,
            boardPadding + row * cellSize + padding + (cellSize - padding * 2) * progress,
            xPaint
        )

        // Draw second diagonal line with animation
        canvas.drawLine(
            boardPadding + (col + 1) * cellSize - padding,
            boardPadding + row * cellSize + padding,
            boardPadding + (col + 1) * cellSize - padding - (cellSize - padding * 2) * progress,
            boardPadding + row * cellSize + padding + (cellSize - padding * 2) * progress,
            xPaint
        )
    }

    private fun drawO(canvas: Canvas, row: Int, col: Int) {
        val padding = cellSize * cellPadding
        val progress = animationProgress[Pair(row, col)] ?: 1f
        val centerX = boardPadding + col * cellSize + cellSize / 2
        val centerY = boardPadding + row * cellSize + cellSize / 2
        val radius = (cellSize - padding * 2) / 2

        // Draw circle with animation
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            0f,
            360f * progress,
            false,
            oPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (isProcessingMove || game.isGameOver() || game.getCurrentPlayer() != Player.X) {
                    return true
                }

                val row = ((event.y - boardPadding) / cellSize).toInt()
                val col = ((event.x - boardPadding) / cellSize).toInt()
                if (row in 0..2 && col in 0..2) {
                    performClick()
                    handleMove(row, col)
                }
                invalidate()
            }
        }
        return true
    }

    private fun handleMove(row: Int, col: Int) {
        // Check if the move is valid
        if (!game.makeMove(row, col)) {
            return
        }

        isProcessingMove = true

        // Start animation
        startAnimation(row, col)

        // Handle feedback
        if (enableHapticFeedback) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        if (enableVibration) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                if (vibrator.hasVibrator()) {
                    val timings = longArrayOf(0, 100, 100, 100, 100, 100)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                }
            } catch (e: SecurityException) {
                // Vibration permission not granted
            }
        }
        playMoveSound()

        // Check for win
        if (game.isGameOver()) {
            if (game.getWinner() != Player.NONE) {
                winningCells = findWinningCells()
                playWinSound()
            } else {
                playDrawSound()
            }
            isProcessingMove = false
        }

        // Notify listener
        onGameStateChangeListener?.invoke(
            game.getCurrentPlayer(),
            game.isGameOver(),
            game.getWinner()
        )

        // Handle AI move
        if (autoPlay && !game.isGameOver() && game.getCurrentPlayer() == Player.O) {
            postDelayed({
                makeAIMove()
                isProcessingMove = false
            }, DEFAULT_ANIMATION_DURATION)
        } else {
            isProcessingMove = false
        }
    }

    private fun startAnimation(row: Int, col: Int) {
        // Cancel any existing animation for this cell
        animations[Pair(row, col)]?.cancel()

        // Create new animation
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = DEFAULT_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                animationProgress[Pair(row, col)] = animation.animatedValue as Float
                invalidate()
            }
            start()
        }

        animations[Pair(row, col)] = animator
    }

    private fun findWinningCells(): List<Pair<Int, Int>> {
        val board = game.getBoard()
        val winner = game.getWinner()

        // Check rows
        for (row in 0..2) {
            if (board[row][0] == winner && board[row][1] == winner && board[row][2] == winner) {
                return listOf(Pair(row, 0), Pair(row, 1), Pair(row, 2))
            }
        }

        // Check columns
        for (col in 0..2) {
            if (board[0][col] == winner && board[1][col] == winner && board[2][col] == winner) {
                return listOf(Pair(0, col), Pair(1, col), Pair(2, col))
            }
        }

        // Check diagonals
        if (board[0][0] == winner && board[1][1] == winner && board[2][2] == winner) {
            return listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
        }
        if (board[0][2] == winner && board[1][1] == winner && board[2][0] == winner) {
            return listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        }

        return emptyList()
    }

    private fun makeAIMove() {
        if (game.isGameOver() || game.getCurrentPlayer() != Player.O) {
            return
        }

        val move = when (aiDifficulty) {
            AIDifficulty.EASY -> makeRandomMove()
            AIDifficulty.MEDIUM -> if (Math.random() < 0.5) makeRandomMove() else makeSmartMove()
            AIDifficulty.HARD -> makeSmartMove()
        }
        move?.let { (row, col) -> handleMove(row, col) }
    }

    private fun makeRandomMove(): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        val board = game.getBoard()
        for (row in 0..2) {
            for (col in 0..2) {
                if (board[row][col] == Player.NONE) {
                    emptyCells.add(Pair(row, col))
                }
            }
        }
        return emptyCells.randomOrNull()
    }

    private fun makeSmartMove(): Pair<Int, Int>? {
        val board = game.getBoard()
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        // Try each empty cell
        for (row in 0..2) {
            for (col in 0..2) {
                if (board[row][col] == Player.NONE) {
                    // Make the move
                    board[row][col] = Player.O

                    // Calculate score for this move
                    val score = minimax(board, 0, false)

                    // Undo the move
                    board[row][col] = Player.NONE

                    // Update best move if this score is better
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(row, col)
                    }
                }
            }
        }

        return bestMove
    }

    private fun minimax(board: Array<Array<Player>>, depth: Int, isMaximizing: Boolean): Int {
        // Check for terminal states
        val winner = checkWinner(board)
        if (winner != null) {
            return when (winner) {
                Player.O -> 10 - depth // AI wins
                Player.X -> depth - 10 // Player wins
                Player.NONE -> 0 // Draw
            }
        }

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col] == Player.NONE) {
                        board[row][col] = Player.O
                        val score = minimax(board, depth + 1, false)
                        board[row][col] = Player.NONE
                        bestScore = maxOf(score, bestScore)
                    }
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col] == Player.NONE) {
                        board[row][col] = Player.X
                        val score = minimax(board, depth + 1, true)
                        board[row][col] = Player.NONE
                        bestScore = minOf(score, bestScore)
                    }
                }
            }
            return bestScore
        }
    }

    private fun checkWinner(board: Array<Array<Player>>): Player? {
        // Check rows
        for (row in 0..2) {
            if (board[row][0] != Player.NONE &&
                board[row][0] == board[row][1] &&
                board[row][1] == board[row][2]
            ) {
                return board[row][0]
            }
        }

        // Check columns
        for (col in 0..2) {
            if (board[0][col] != Player.NONE &&
                board[0][col] == board[1][col] &&
                board[1][col] == board[2][col]
            ) {
                return board[0][col]
            }
        }

        // Check diagonals
        if (board[0][0] != Player.NONE &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]
        ) {
            return board[0][0]
        }
        if (board[0][2] != Player.NONE &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]
        ) {
            return board[0][2]
        }

        // Check for draw
        var isDraw = true
        for (row in 0..2) {
            for (col in 0..2) {
                if (board[row][col] == Player.NONE) {
                    isDraw = false
                    break
                }
            }
        }
        if (isDraw) {
            return Player.NONE
        }

        return null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up animations
        animations.values.forEach { it.cancel() }
        animations.clear()
        animationProgress.clear()

        // Clean up sounds
        try {
            moveSound?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            winSound?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            drawSound?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // Handle cleanup error
        } finally {
            moveSound = null
            winSound = null
            drawSound = null
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
        return true
    }

    // Programmatic setters for attributes
    fun setBoardLineColor(@ColorInt color: Int) {
        boardPaint.color = color
        invalidate()
    }

    fun setBoardLineWidth(width: Float) {
        boardPaint.strokeWidth = width
        invalidate()
    }

    fun setLineWidth(width: Float) {
        xPaint.strokeWidth = width
        oPaint.strokeWidth = width
        invalidate()
    }

    fun setBoardPadding(padding: Float) {
        boardPadding = padding
        invalidate()
    }

    fun setXColor(@ColorInt color: Int) {
        xPaint.color = color
        invalidate()
    }

    fun setOColor(@ColorInt color: Int) {
        oPaint.color = color
        invalidate()
    }

    fun setCellPadding(padding: Float) {
        cellPadding = padding.coerceIn(0f, 0.5f)
        invalidate()
    }

    fun setAutoPlay(enabled: Boolean) {
        autoPlay = enabled
    }

    fun setAiDifficulty(difficulty: AIDifficulty) {
        aiDifficulty = difficulty
    }

    fun setEnableSound(enabled: Boolean) {
        enableSound = enabled
        if (enabled && moveSound == null) {
            initializeSounds()
        }
    }

    fun setEnableVibration(enabled: Boolean) {
        enableVibration = enabled
    }

    fun setEnableHapticFeedback(enabled: Boolean) {
        enableHapticFeedback = enabled
    }

    fun setHighlightWinningCells(enabled: Boolean) {
        highlightWinningCells = enabled
        invalidate()
    }

    fun setShowPlayerTurn(enabled: Boolean) {
        showPlayerTurn = enabled
        invalidate()
    }
} 