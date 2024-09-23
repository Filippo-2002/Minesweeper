package com.filippo_de_cristofaro.minesweeper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
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

    private final int ROWS = 12;
    private final int COLUMNS = 10;
    private final int TOTAL_MINES = 4;
    private Cell[][] cells = new Cell[ROWS][COLUMNS];
    private Button[][] buttons = new Button[ROWS][COLUMNS];

    private boolean isFlagMode = false;
    private int flagsPlaced = 0;
    private int elapsedSeconds = 0;
    private boolean isGameOver = false;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedSeconds++;
            timerTextView.setText("Time: " + elapsedSeconds + "s");
            timerHandler.postDelayed(this, 1000);
        }
    };

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

        modeToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMode();
            }
        });
    }

    private void initializeGame() {

        isGameOver = false;
        flagsPlaced = 0;
        elapsedSeconds = 0;
        mineCountTextView.setText("Mines: " + (TOTAL_MINES - flagsPlaced));
        timerTextView.setText("Time: 0s");

        //start the timer
        timerHandler.postDelayed(timerRunnable, 1000);
        //initialize the game
        mineGrid.removeAllViews();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                cells[row][col] = new Cell();

                Button button = new Button(this);
                button.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                button.setBackgroundColor(Color.LTGRAY);
                button.setTag(new int[]{row, col});
                button.setOnClickListener(cellClickListener);

                buttons[row][col] = button;
                mineGrid.addView(button);
            }
        }
        //to be defined
        placeMines();
        //to be defined
        calculateAdjacentMines();
    }

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

    private void calculateAdjacentMines() {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (cells[row][col].hasMine()) {
                    cells[row][col].setAdjacentMines(-1); // -1 indicates a mine
                    continue;
                }
                int count = 0;
                for (int i = 0; i < 8; i++) {
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
        if (isFlagMode) {
            isFlagMode = false;
            modeToggleButton.setImageResource(R.drawable.digging_icon);
            Toast.makeText(this, "Now in Digging Mode", Toast.LENGTH_SHORT).show();
        } else {
            isFlagMode = true;
            modeToggleButton.setImageResource(R.drawable.flag_icon);
            Toast.makeText(this, "Now in Flag", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameOver) {
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private View.OnClickListener cellClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isGameOver) {
                navigateToResult();
                return;
            }

            int[] position = (int[]) view.getTag();
            int row = position[0];
            int col = position[1];
            Cell cell = cells[row][col];
            Button button = buttons[row][col];

            if (isFlagMode) {
                //flag mode
                if (!cell.isRevealed()) {
                    if (!cell.isFlagged()) {
                        if (flagsPlaced < TOTAL_MINES) {
                            cell.setFlagged(true);
                            button.setText("F");
                            flagsPlaced++;
                            mineCountTextView.setText("Mines: " + (TOTAL_MINES - flagsPlaced));
                        } else {
                            Toast.makeText(MainActivity.this, "No more flags available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cell.setFlagged(false);
                        button.setText("");
                        flagsPlaced--;
                        mineCountTextView.setText("Mines: " + (TOTAL_MINES - flagsPlaced));
                    }
                }
            } else {
                //digging mode
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    revealCell(row, col);
                    checkWinCondition();
                }
            }
        }
    };

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
            gameOver(false);
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
                //if no mines
                button.setText("");
                int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
                int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
                for (int i = 0; i < 8; i++) {
                    int newRow = row + dx[i];
                    int newCol = col + dy[i];
                    if (isValidCell(newRow, newCol)) {
                        revealCell(newRow, newCol);
                    }
                }
            }
        }
    }

    private void gameOver(boolean isWin) {
        isGameOver = true;
        timerHandler.removeCallbacks(timerRunnable);

        //revral mines
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (cells[row][col].hasMine()) {
                    buttons[row][col].setText("M");
                    buttons[row][col].setTextColor(Color.RED);
                }
            }
        }
        navigateToResult(isWin);
    }

    private void checkWinCondition() {
        boolean allNonMineCellsRevealed = true;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Cell cell = cells[row][col];
                if (!cell.hasMine() && !cell.isRevealed()) {
                    allNonMineCellsRevealed = false;
                    break;
                }
            }
        }

        if (allNonMineCellsRevealed) {
            gameOver(true);
        }
    }
    //Result class to be defined
    private void navigateToResult(boolean isWin) {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("RESULT", isWin ? "won" : "lost");
        intent.putExtra("TIME", elapsedSeconds);
        startActivity(intent);
    }

    private void navigateToResult() {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        startActivity(intent);
    }

}
