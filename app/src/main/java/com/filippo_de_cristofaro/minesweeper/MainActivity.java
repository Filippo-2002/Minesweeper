package com.filippo_de_cristofaro.minesweeper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView mineCountTextView, timerTextView;
    private ImageButton modeToggleButton;
    private GridLayout mineGrid;

    private boolean isWin;
    private final int ROWS = 12;
    private final int COLUMNS = 10;
    private final int TOTAL_MINES = 4;
    private final Cell[][] cells = new Cell[ROWS][COLUMNS];
    private final Button[][] buttons = new Button[ROWS][COLUMNS];

    private boolean isFlagMode = false;
    private int flagsPlaced = 0;
    private int elapsedSeconds = 0;
    private boolean isGameOver = false;

    private final Handler timerHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mineCountTextView = findViewById(R.id.mineCountTextView);
        timerTextView = findViewById(R.id.timerTextView);
        modeToggleButton = findViewById(R.id.modeToggleButton);
        mineGrid = findViewById(R.id.mineGrid);

        //game-start
        initializeGame();

        modeToggleButton.setOnClickListener(view -> toggleMode());
    }

    private void initializeGame() {

        isGameOver = false;
        isFlagMode = false;
        flagsPlaced = 0;
        elapsedSeconds = 0;
        mineCountTextView.setText(getString(R.string.mine_count_text, TOTAL_MINES - flagsPlaced));
        timerTextView.setText(getString(R.string.time_text, elapsedSeconds));
        modeToggleButton.setImageResource(R.drawable.digging_icon);

        //start the timer
        startTimer();

        //initialize the game
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                cells[row][col] = new Cell();

                Button button = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row, 1f),
                        GridLayout.spec(col, 1f)
                );
                params.width = 0;
                params.height = 0;
                params.setMargins(2, 2, 2, 2);
                button.setLayoutParams(params);
                button.setBackgroundColor(Color.LTGRAY);
                button.setTag(new int[]{row, col});
                button.setOnClickListener(cellClickListener);

                buttons[row][col] = button;
                mineGrid.addView(button);
            }
        }

        placeMines();
        calculateAdjacentMines();
    }

        //timer
    private void startTimer() {
        timerHandler.removeCallbacksAndMessages(null);
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                timerTextView.setText(getString(R.string.time_text, elapsedSeconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }


    //place mines
    private void placeMines() {
        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < TOTAL_MINES) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLUMNS);
            if (!cells[row][col].hasMine()) {
                cells[row][col].setMine(true);
                minesPlaced++;
            }
        }
    }

    //mines logic
    private void calculateAdjacentMines() {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (cells[row][col].hasMine()) {
                    continue;
                }
                int count = 0;
                for (int i = 0; i < dx.length; i++) {
                    int newRow = row + dx[i];
                    int newCol = col + dy[i];
                    if (isValidCell(newRow, newCol) && cells[newRow][newCol].hasMine()) {
                        count++;
                    }
                }
                cells[row][col].setAdjacentMines(count);
            }
        }
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLUMNS;
    }

    private void toggleMode() {
        isFlagMode = !isFlagMode;
        if (isFlagMode) {
            modeToggleButton.setImageResource(R.drawable.flag_icon);
            Toast.makeText(this, "Flag Mode", Toast.LENGTH_SHORT).show();
        } else {
            modeToggleButton.setImageResource(R.drawable.digging_icon);
            Toast.makeText(this, "Digging Mode", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameOver) {
            startTimer();
        }
    }

private final View.OnClickListener cellClickListener = view -> {
    if (isGameOver) {
        navigateToResult();
        return;
    }

    int[] position = (int[]) view.getTag();
    int row = position[0];
    int col = position[1];
    Cell cell = cells[row][col];
    Button button = buttons[row][col];

    if (cell.isRevealed()) {
        return;
    }

    if (isFlagMode) {
        handleFlagAction(cell, button);
    } else {
        handleDigAction(row, col);
    }
};

private void handleFlagAction(Cell cell, Button button) {
    if (!cell.isRevealed()) {
        if (!cell.isFlagged()) {
            if (flagsPlaced < TOTAL_MINES) {
                cell.setFlagged(true);
                button.setText("F");
                flagsPlaced++;
                mineCountTextView.setText(getString(R.string.mine_count_text, TOTAL_MINES - flagsPlaced));
            } else {
                Toast.makeText(MainActivity.this, "No more flags available", Toast.LENGTH_SHORT).show();
            }
        } else {
            cell.setFlagged(false);
            button.setText("");
            flagsPlaced--;
            mineCountTextView.setText(getString(R.string.mine_count_text, TOTAL_MINES - flagsPlaced));
        }
    }
}

private void handleDigAction(int row, int col) {
    Cell cell = cells[row][col];
    if (!cell.isRevealed() && !cell.isFlagged()) {
        if (cell.hasMine()) {
            revealCell(row, col);
            gameOver(false);
        } else {
            revealCell(row, col);
            if (checkWinCondition()) {
                gameOver(true);
            }
        }
    }
}

private void revealCell(int row, int col) {
    if (!isValidCell(row, col)) return;

    Cell cell = cells[row][col];
    Button button = buttons[row][col];

    if (cell.isRevealed() || cell.isFlagged()) return;

    cell.setRevealed(true);
    button.setEnabled(false);
    button.setBackgroundColor(Color.WHITE);

    if (cell.hasMine()) {
        button.setText("M");
        button.setTextColor(Color.RED);
    } else {
        int adjacent = cell.getAdjacentMines();
        if (adjacent > 0) {
            button.setText(String.valueOf(adjacent));
            switch (adjacent) {
                case 1:
                    button.setTextColor(Color.BLUE);
                    break;
                case 2:
                    button.setTextColor(Color.GREEN);
                    break;
                case 3:
                    button.setTextColor(Color.RED);
                    break;
                default:
                    button.setTextColor(Color.BLACK);
                    break;
            }
        } else {
            button.setText("");
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        revealCell(row + i, col + j);
                    }
                }
            }
        }
    }
}

private void gameOver(boolean isWin) {
    isGameOver = true;
    this.isWin = isWin; //store result
    timerHandler.removeCallbacksAndMessages(null);

    //reveal all mines
    revealAllMines();
    //make it so any cell can be clicked to show results
    for (int row = 0; row < ROWS; row++) {
        for (int col = 0; col < COLUMNS; col++) {
            Button button = buttons[row][col];
            button.setEnabled(true);
        }
    }
}

private void revealAllMines() {
    for (int row = 0; row < ROWS; row++) {
        for (int col = 0; col < COLUMNS; col++) {
            Cell cell = cells[row][col];
            Button button = buttons[row][col];
            if (cell.hasMine()) {
                button.setText("M");
                button.setTextColor(Color.RED);
                button.setEnabled(false);
            }
        }
    }
}

private boolean checkWinCondition() {
    for (int row = 0; row < ROWS; row++) {
        for (int col = 0; col < COLUMNS; col++) {
            Cell cell = cells[row][col];
            if (!cell.hasMine() && !cell.isRevealed()) {
                return false;
            }
        }
    }
    return true;
}

private void navigateToResult() {
    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
    intent.putExtra("RESULT", isWin ? "won" : "lost");
    intent.putExtra("TIME", elapsedSeconds);
    startActivity(intent);
    finish();
}
}
