package com.example.misty;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.misty.Socketconnection.TCPClient;
import com.example.misty.Socketconnection.TCPClientOwner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GamePage extends AppCompatActivity implements TCPClient.OnMessageReceived {
    private static int ROWS;
    private static int COLUMNS;

    private Button[][] leftButtons;
    private char[][] leftNumbers;
    private boolean[][] leftRevealed;

    private Button[][] rightButtons;
    private char[][] rightNumbers;
    private boolean[][] rightRevealed;

    private Board leftGame;
    private Board rightGame;
    private boolean interrupt = false;

    //variables for the left and right row and column numbers
    private LinearLayout leftRowNumbers, leftColumnNumbers, rightRowNumbers, rightColumnNumbers;
    private TextView leftGoldCountText;
    private TextView rightGoldCountText;
    private int leftGoldCount = 0;
    private int rightGoldCount = 0;

    //variable for TCP connection, outcome
    // private String choiceOutcome;

    private TCPClient mTcpClient = TCPClient.getInstance();

    private boolean mistyTurnOver = true;

    private int specifiedRow = -1;
    private int specifiedCol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.d("GamePage", "onCreate called - mTcpClient instance: " + mTcpClient.toString());
        mTcpClient.addMessageListener(this);
        setContentView(R.layout.game_board);

        TextView textViewMode = findViewById(R.id.textViewMode);
        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = "Easy"; // Default value if null
        textViewMode.setText(difficulty);
        leftGoldCountText = findViewById(R.id.leftGoldCountText);
        rightGoldCountText = findViewById(R.id.rightGoldCountText);

        String avatar = getIntent().getStringExtra("avatar");
        if (avatar == null) avatar = "Red";

        ImageView leftAvatarImage = findViewById(R.id.leftAvatarImage);
        ImageView rightAvatarImage = findViewById(R.id.rightAvatarImage);

        if ("Red".equals(avatar)) {
            leftAvatarImage.setImageResource(R.drawable.fire);
            rightAvatarImage.setImageResource(R.drawable.water);

        } else {
            leftAvatarImage.setImageResource(R.drawable.water);
            rightAvatarImage.setImageResource(R.drawable.fire);
        }

        //calling from the xml file with the linear layout
        leftRowNumbers = findViewById(R.id.leftRowNumbers);
        leftColumnNumbers = findViewById(R.id.leftColumnNumbers);
        rightRowNumbers = findViewById(R.id.rightRowNumbers);
        rightColumnNumbers = findViewById(R.id.rightColumnNumbers);


        // Set grid size
        switch (difficulty) {
            case "Medium":
                ROWS = 5;
                COLUMNS = 5;
                break;
            case "Hard":
                ROWS = 7;
                COLUMNS = 7;
                break;
            default:
                ROWS = 3;
                COLUMNS = 3;
                break;
        }
        //the method in which we can set the row and column numbers
        setupGrid();

        leftButtons = new Button[ROWS][COLUMNS];
        leftNumbers = new char[ROWS][COLUMNS];
        leftRevealed = new boolean[ROWS][COLUMNS];

        rightButtons = new Button[ROWS][COLUMNS];
        rightNumbers = new char[ROWS][COLUMNS];
        rightRevealed = new boolean[ROWS][COLUMNS];

        leftGame = new Board(ROWS, COLUMNS);
        rightGame = new Board(ROWS, COLUMNS);

        GridLayout leftGridLayout = findViewById(R.id.leftBoard);
        GridLayout rightGridLayout = findViewById(R.id.rightBoard);

        leftGridLayout.setRowCount(ROWS);
        leftGridLayout.setColumnCount(COLUMNS);
        rightGridLayout.setRowCount(ROWS);
        rightGridLayout.setColumnCount(COLUMNS);


        generateShuffledNumbers(leftGame, leftNumbers, leftRevealed);
        generateShuffledNumbers(rightGame, rightNumbers, rightRevealed);

        initializeBoard(leftGridLayout, leftButtons, leftNumbers, leftRevealed, true);
        initializeBoard(rightGridLayout, rightButtons, rightNumbers, rightRevealed, false);

        Button backHomeButton = findViewById(R.id.backHomeButton);
        backHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(GamePage.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTcpClient.removeMessageListener(this);
    }

    @Override
    public void messageReceived(String message) {
        runOnUiThread(() -> {
            Log.d("GamePage", "message received: " + message);
            String[] parts = message.split(";");
           // Log.d("GamePage", "messageReceived: parts length: " + parts.length);
            if (parts.length == 2) {
               // Log.d("GamePage", "messageReceived: inside if parts.length ==2: ");
                String type = parts[0].trim();
                Log.d("GamePage", "messageReceived: type: " + type);
                // Extract row and column from the coordinates string
                if (type.equals("Mistychoice")) {
                   // Log.d("GamePage", "messageReceived: inside if type equals MistyChoice: ");
                    String coordinates = parts[1].trim();
                    Log.d("GamePage", "messageReceived: coordinates: " + coordinates);
                    // Extract row and column from the coordinates string
                    if (!coordinates.isEmpty()) {
                        try {
                            // Find the first digit (row)
                            int firstDigitIndex = 0;
                            while (firstDigitIndex < coordinates.length() && !Character.isDigit(coordinates.charAt(firstDigitIndex))) {
                                firstDigitIndex++;
                            }
                            // Find the last digit (column)
                            int lastDigitIndex = coordinates.length() - 1;
                            while (lastDigitIndex >= 0 && !Character.isDigit(coordinates.charAt(lastDigitIndex))) {
                                lastDigitIndex--;
                            }
                            // Extract row and col as integers
                            specifiedRow = Integer.parseInt(coordinates.substring(firstDigitIndex,firstDigitIndex+1)); // Parse the first character as row
                            specifiedCol = Integer.parseInt(coordinates.substring(lastDigitIndex, lastDigitIndex + 1)); // Parse the second character as column
                            Log.e("GamePage", "flipSquare: Checking row: " + specifiedRow + " and column: " + specifiedCol);
                            mistyTurn();
                        } catch (NumberFormatException e) {
                            Log.e("GamePage", "Error parsing row or col", e);
                        }
                    }
                }
            }
        });
    }

    private class SendMessageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... messages) {
            String message = messages[0];
            // Call the method to send the message through the TCPClient
            mTcpClient.sendMessage(message);
            return null;
        }
    }

    public void sendMessageToPython(String message) {
        new SendMessageTask().execute(message);
    }

    private void performTimedAction() {
        runOnUiThread(() -> Toast.makeText(GamePage.this, "", Toast.LENGTH_SHORT).show());
    }

    public void mistyTurn() {
        // Log.e("GamePage", "mistyTurn: Called");
        if (specifiedRow != -1 && specifiedCol != -1) {
            // Use the specified row and column
            flipSpecifiedSquare(specifiedRow, specifiedCol);
            // Reset the specified row and column
            specifiedRow = -1;
            specifiedCol = -1;
            mistyTurnOver = true;

        } else {
            mistyTurnOver = false;
        }
    }

    private void flipSpecifiedSquare(int row, int col) {
       // System.out.println("Misty is playing by specified move...");
        char mv = flipButton(row, col, false); // Misty plays on right board

        //we check that v was not equal to a, since a is returned if the button has already been clicked.
        if (mv != 'a') {
            sendMOutcomeOverTCP(mv); // Send the value over TCP
        }

    }

    private void flipRandomSquare() {
        // Log.e("GamePage", "flipRandomSquare: Called");
       // System.out.println("Misty is playing...");

        Random random = new Random();
        int randRow, randCol;

        do {
            Solver solver = new Solver(ROWS, COLUMNS, rightNumbers, rightRevealed);
            int[] move = solver.getMove(getIntent().getStringExtra("difficulty"));

            randRow = move[0];
            randCol = move[1];
            //    Log.e("GamePage", "flipRandomSquare: Checking row: " + randRow + " and column: " + randCol);
        } while (rightRevealed[randRow][randCol]);

        char mv = flipButton(randRow, randCol, false); // Misty plays on right board
        //flipButton(randRow, randCol, false); // Misty plays on right board

        //  int finalRandRow = randRow;
        //  int finalRandCol = randCol;
        //we check that v was not equal to a, since a is returned if the button has already been clicked.
        if (mv != 'a') {
            sendMOutcomeOverTCP(mv); // Send the value over TCP
        }

    }

    private void sendMOutcomeOverTCP(char mv) {
        // Prepare the message string
        String message = "Moutcome;" + mv + ";";

        // Send the message using the existing sendMessageToPython method
        System.out.println("message being sent: " + message);
        sendMessageToPython(message);

        System.out.println(message);
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
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        int maxBoardWidth = (int) (screenWidth * 0.9); // 90% of screen width
        int maxBoardHeight = (int) (screenHeight * 0.6); // 60% of screen height

// Adjust button size based on rows/columns, ensuring it fits within limits
        int buttonSize = Math.min(maxBoardWidth / (COLUMNS + 1), maxBoardHeight / (ROWS + 2));

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                final int r = row, c = col;
                buttons[row][col] = new Button(this);
                //buttons[row][col].setScaleType(ImageView.ScaleType.CENTER_CROP);
                //buttons[row][col].setAdjustViewBounds(true);
                buttons[row][col].setText("");
                buttons[row][col].setTextSize(15);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(1, 1, 1, 1);
                buttons[row][col].setLayoutParams(params);

                buttons[row][col].setBackgroundResource(R.drawable.dirt);

                if (isLeftBoard) {
                    buttons[row][col].setOnClickListener(v -> buttonClick(r, c));
                } else {
                    buttons[row][col].setEnabled(false);
                }

                gridLayout.addView(buttons[row][col]);
            }
        }
    }

    //sends signals to the misty regarding row + column picked
    //the format needs to be in (row, column) or (column, row) where both has to be valid
    //row is displayed in letters and column are numbers
    private void buttonClick(int row, int column) {
        if (!mistyTurnOver) return;
        char v = flipButton(row, column, true);
        mistyTurnOver = false; // Misty's turn now
        //we check that v was not equal to a, since a is returned if the button has already been clicked.
        if (v != 'a') {
            sendCOutcomeOverTCP(v); // Send the value over TCP
        }

    }

    private void sendCOutcomeOverTCP(char v) {
        if (mTcpClient != null) {
            // Prepare the message string
            String messageToSend = "Coutcome;" + v + ";";

            sendMessageToPython(messageToSend);
            Log.i("GamePage", "message being sent: " + messageToSend);

            String MmessageToSend = "Mistyturn;" + "started";
            sendMessageToPython(MmessageToSend);
            Log.i("GamePage", "message being sent: " + MmessageToSend);

            new Handler().postDelayed(() -> {
                if (specifiedRow == -1 && specifiedCol == -1) {
                    flipRandomSquare();
                }
            }, 1000);
        }
    }

    //the method that sets up the grid with the numbers/letters
    private void setupGrid() {
        // Clear existing row and column labels to avoid duplication
        leftRowNumbers.removeAllViews();
        leftColumnNumbers.removeAllViews();
        rightRowNumbers.removeAllViews();
        rightColumnNumbers.removeAllViews();

        // Adjust padding dynamically based on difficulty
        int columnPadding, rowPadding;
        if (ROWS == 7) { // Hard mode (7x7)
            columnPadding = 40; // Reduce column padding
            rowPadding = 30;    // Reduce row padding
        } else if (ROWS == 5) { // Medium mode (5x5)
            columnPadding = 60;
            rowPadding = 40;
        } else { // Easy mode (3x3)
            columnPadding = 80;
            rowPadding = 50;
        }

        // Add column numbers
        for (int i = 0; i < COLUMNS; i++) {
            TextView colLabelLeft = new TextView(this);
            colLabelLeft.setText(String.valueOf(i));
            colLabelLeft.setGravity(Gravity.CENTER);
            colLabelLeft.setTextColor(Color.WHITE);
            colLabelLeft.setTextSize(22); // Reduce text size slightly
            colLabelLeft.setPadding(columnPadding, 10, columnPadding, 10);
            leftColumnNumbers.addView(colLabelLeft);

            TextView colLabelRight = new TextView(this);
            colLabelRight.setText(String.valueOf(i));
            colLabelRight.setTextSize(22f);
            colLabelRight.setTextColor(Color.WHITE);
            colLabelRight.setGravity(Gravity.CENTER);
            colLabelRight.setPadding(columnPadding, 10, columnPadding, 10);
            rightColumnNumbers.addView(colLabelRight);
        }

        // Add row numbers
        for (int i = 0; i < ROWS; i++) {
            TextView rowLabelLeft = new TextView(this);
            rowLabelLeft.setText(String.valueOf(i));
            rowLabelLeft.setTextSize(25f);
            rowLabelLeft.setTextColor(Color.WHITE);

            // Adjust padding dynamically
            rowPadding = (ROWS == 3) ? 50 : (ROWS == 5) ? 40 : 20;
            rowLabelLeft.setPadding(50, rowPadding, 10, rowPadding);
            leftRowNumbers.addView(rowLabelLeft);

            TextView rowLabelRight = new TextView(this);
            rowLabelRight.setText(String.valueOf(i));
            rowLabelRight.setTextSize(25f);
            rowLabelRight.setTextColor(Color.WHITE);
            rowLabelRight.setPadding(50, rowPadding, 10, rowPadding);
            rightRowNumbers.addView(rowLabelRight);
        }
    }


    private char flipButton(int row, int col, boolean isLeftBoard) {
        Button[][] buttons = isLeftBoard ? leftButtons : rightButtons;
        char[][] numbers = isLeftBoard ? leftNumbers : rightNumbers;
        boolean[][] revealed = isLeftBoard ? leftRevealed : rightRevealed;

        if (!revealed[row][col]) {
            //buttons[row][col].setText(String.valueOf(numbers[row][col]));
            revealed[row][col] = true;


            if (numbers[row][col] == 'G') {
                if (isLeftBoard) {
                    leftGoldCount++;
                    if (leftGoldCountText != null) {
                        leftGoldCountText.setText(" " + leftGoldCount);
                    }

                } else {
                    rightGoldCount++;
                    if (rightGoldCountText != null) {
                        rightGoldCountText.setText(" " + rightGoldCount);
                    }

                }
                buttons[row][col].setBackgroundResource(R.drawable.gold); // this will show gold image
                new Handler().postDelayed(() -> resetBoard(isLeftBoard), 5000);
                mistyTurnOver = true;
            } else if (numbers[row][col] == 'B') {
                buttons[row][col].setBackgroundResource(R.drawable.bomb); // this will show bomb image
                new Handler().postDelayed(() -> resetBoard(isLeftBoard), 5000);
                mistyTurnOver = true;
            } else {
                buttons[row][col].setText(String.valueOf(numbers[row][col])); // this will show the number of squares away from the bomb
                buttons[row][col].setTextColor(Color.BLACK);
                buttons[row][col].setBackgroundColor(Color.LTGRAY);
                mistyTurnOver = true;
            }
            return numbers[row][col];
        }
        return 'a';
    }

    //private void resetGame() {
    //Intent intent = new Intent(GamePage.this, GamePage.class);
    //intent.putExtra("difficulty", getIntent().getStringExtra("difficulty"));
    //startActivity(intent);
    // finish();
    //}

    //utilized the boolean flag isLeftBoard that was in the flipButton
    //if it's the left board then go into that condition
    //if not then it'd be the right baord that'd get updated
    //above was the reset game but it resets the whole game
    //so i changed it to reset board so it changes the individual board
    private void resetBoard(boolean isLeftBoard) {
        if (isLeftBoard) {
            leftGame = new Board(ROWS, COLUMNS);
            generateShuffledNumbers(leftGame, leftNumbers, leftRevealed);

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    leftButtons[row][col].setText("");
                    leftButtons[row][col].setBackgroundResource(R.drawable.dirt);
                    leftRevealed[row][col] = false;
                }
            }


        } else {
            rightGame = new Board(ROWS, COLUMNS);
            generateShuffledNumbers(rightGame, rightNumbers, rightRevealed);

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    rightButtons[row][col].setText("");
                    rightButtons[row][col].setBackgroundResource(R.drawable.dirt);
                    rightRevealed[row][col] = false;
                }
            }
        }

    }

}