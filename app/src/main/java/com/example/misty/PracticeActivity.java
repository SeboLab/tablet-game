package com.example.misty;

import android.os.Bundle;
import android.os.Handler;
import android.widget.GridLayout;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.content.Intent;

public class PracticeActivity extends AppCompatActivity {
    private static final int ROWS = 3;
    private static final int COLUMNS = 3;

    private Button[][] leftButtons = new Button[ROWS][COLUMNS];
    private char[][] leftNumbers = new char[ROWS][COLUMNS];
    private boolean[][] leftRevealed = new boolean[ROWS][COLUMNS];

    private Button[][] rightButtons = new Button[ROWS][COLUMNS];
    private char[][] rightNumbers = new char[ROWS][COLUMNS];
    private boolean[][] rightRevealed = new boolean[ROWS][COLUMNS];

    private Board leftGame = new Board(ROWS, COLUMNS);
    private Board rightGame = new Board(ROWS, COLUMNS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        GridLayout leftGridLayout = findViewById(R.id.leftBoard);
        GridLayout rightGridLayout = findViewById(R.id.rightBoard);

        leftGridLayout.setRowCount(ROWS);
        leftGridLayout.setColumnCount(COLUMNS);
        rightGridLayout.setRowCount(ROWS);
        rightGridLayout.setColumnCount(COLUMNS);

        new Thread(() -> {
            MistyConnection misty = new MistyConnection("192.168.1.143", 80);
            misty.speak("Please choose a square", true, "2");
        }).start();

        generateShuffledNumbers(leftGame, leftNumbers, leftRevealed);
        generateShuffledNumbers(rightGame, rightNumbers, rightRevealed);

        initializeBoard(leftGridLayout, leftButtons, leftNumbers, leftRevealed, true);
        initializeBoard(rightGridLayout, rightButtons, rightNumbers, rightRevealed, false);

        // Back to Home Button
        Button backHomeButton = findViewById(R.id.backHomeButton);
        backHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(PracticeActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void generateShuffledNumbers(Board board, char[][] numbers, boolean[][] revealed) {
        List<Character> values = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                values.add(board.passSquare(i, j));
            }
        }
        int index = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                numbers[row][col] = values.get(index++);
                revealed[row][col] = false;
            }
        }
    }

    private void initializeBoard(GridLayout gridLayout, Button[][] buttons, char[][] numbers, boolean[][] revealed, boolean isLeftBoard) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                final int r = row, c = col;
                buttons[row][col] = new Button(this);
                buttons[row][col].setText(""); // Initially blank
                buttons[row][col].setTextSize(24);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.width = 200;
                params.height = 200;
                buttons[row][col].setLayoutParams(params);

                if (isLeftBoard) {
                    // Allow human to play on left board
                    buttons[row][col].setOnClickListener(v -> buttonClick(r, c));
                } else {
                    // Disable interaction on Misty’s board
                    buttons[row][col].setEnabled(false);
                }

                gridLayout.addView(buttons[row][col]);
            }
        }
    }

    private void buttonClick(int row, int column) {
        char v = flipButton(row, column, true, true); // Human plays on left board
        new Thread(() -> {
            MistyConnection misty = new MistyConnection("192.168.1.143", 80);
            misty.speak("You have picked Row " + row + " and Column " + column, false, "2");

            if (v == 'G') {
                misty.speak("You have hit gold! Congrats, you have won.", false, "2");
            } else if (v == 'B') {
                misty.speak("I'm sorry, you hit a bomb. You have lost.", false, "2");
            } else {
                misty.speak("You are " + v + " square away from the bomb!", false, "2");
            }
        }).start();

        // Misty plays immediately after human's move
        new Handler().postDelayed(this::mistyTurn, 6000);
    }

    private char flipButton(int row, int col, boolean player, boolean isLeftBoard) {
        Button[][] buttons = isLeftBoard ? leftButtons : rightButtons;
        char[][] numbers = isLeftBoard ? leftNumbers : rightNumbers;
        boolean[][] revealed = isLeftBoard ? leftRevealed : rightRevealed;

        if (!revealed[row][col]) {
            buttons[row][col].setText(String.valueOf(numbers[row][col])); // Show number
            revealed[row][col] = true;

            if (numbers[row][col] == 'G') {
                Toast.makeText(this, (player ? "You" : "Misty") + " hit gold!", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, (player ? "You" : "Misty") + " won the game! Resetting now...", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(this::resetGame, 3000);
            } else if (numbers[row][col] == 'B') {
                Toast.makeText(this, (player ? "You" : "Misty") + " hit a Bomb!", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(this::resetGame, 3000);
            }
            return numbers[row][col];
        }
        return 'a';
    }
    private void mistyTurn(){
//        check if it has been 5 minutes

//        if not run the turn
        flipRandomSquare();
    }
    private void flipRandomSquare() {
        System.out.println("Misty is playing...");

        Random random = new Random();
        int randRow, randCol;

        do {
            randRow = random.nextInt(ROWS);
            randCol = random.nextInt(COLUMNS);
        } while (rightRevealed[randRow][randCol]); // Ensure Misty doesn't flip an already revealed tile

        flipButton(randRow, randCol, false, false); // Misty plays on right board

        int finalRandRow = randRow;
        int finalRandCol = randCol;
        new Thread(() -> {
            MistyConnection misty = new MistyConnection("192.168.1.143", 80);
            misty.speak("Misty has chosen Row " + finalRandRow + " and Column " + finalRandCol, false, "2");
        }).start();
    }

    private void resetGame() {
        Intent intent = new Intent(PracticeActivity.this, PracticeActivity.class);
        startActivity(intent);
        finish();
    }
}
