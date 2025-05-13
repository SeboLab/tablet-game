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

public class PracticeActivity extends AppCompatActivity implements TCPClient.OnMessageReceived {
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

    private LinearLayout leftRowNumbers, leftColumnNumbers, rightRowNumbers, rightColumnNumbers;


    private TCPClient mTcpClient = TCPClient.getInstance();

    private boolean mistyTurnOver = true;

    private int specifiedRow = -1;
    private int specifiedCol = -1;

    private TextView leftGoldCountText;
    private TextView rightGoldCountText;
    private int leftGoldCount = 0;
    private int rightGoldCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        Log.d("GamePage", "onCreate called - mTcpClient instance: " + mTcpClient.toString());
        mTcpClient.addMessageListener(this);

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

        leftRowNumbers = findViewById(R.id.leftRowNumberss);
        leftColumnNumbers = findViewById(R.id.leftColumnNumberss);
        rightRowNumbers = findViewById(R.id.rightRowNumberss);
        rightColumnNumbers = findViewById(R.id.rightColumnNumberss);

        setupGridLabels();

        Button backHomeButton = findViewById(R.id.backHomeButton);
        backHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(PracticeActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTcpClient.removeMessageListener(this);
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

    private void setupGridLabels() {
        leftRowNumbers.removeAllViews();
        leftColumnNumbers.removeAllViews();
        rightRowNumbers.removeAllViews();
        rightColumnNumbers.removeAllViews();

        int columnPadding = 80;
        int rowPadding = 50;

        for (int i = 0; i < COLUMNS; i++) {
            TextView colLeft = new TextView(this);
            colLeft.setText(String.valueOf(i + 1));
            colLeft.setTextColor(getResources().getColor(android.R.color.white));
            colLeft.setTextSize(22);
            colLeft.setPadding(columnPadding, 10, columnPadding, 10);
            colLeft.setGravity(Gravity.CENTER);
            leftColumnNumbers.addView(colLeft);

            TextView colRight = new TextView(this);
            colRight.setText(String.valueOf(i + 1));
            colRight.setTextColor(getResources().getColor(android.R.color.white));
            colRight.setTextSize(22);
            colRight.setPadding(columnPadding, 10, columnPadding, 10);
            colRight.setGravity(Gravity.CENTER);
            rightColumnNumbers.addView(colRight);
        }

        for (int i = 0; i < ROWS; i++) {
            TextView rowLeft = new TextView(this);
            rowLeft.setText(String.valueOf((char) ('A' + i)));
            rowLeft.setTextColor(getResources().getColor(android.R.color.white));
            rowLeft.setTextSize(25);
            rowLeft.setPadding(50, rowPadding, 10, rowPadding);
            leftRowNumbers.addView(rowLeft);

            TextView rowRight = new TextView(this);
            rowRight.setText(String.valueOf((char) ('A' + i)));
            rowRight.setTextColor(getResources().getColor(android.R.color.white));
            rowRight.setTextSize(25);
            rowRight.setPadding(50, rowPadding, 10, rowPadding);
            rightRowNumbers.addView(rowRight);
        }
    }

    private void buttonClick(int row, int column) {
        if (!mistyTurnOver) return;
        char v = flipButton(row, column, true);
        mistyTurnOver = false; // Misty's turn now
        //we check that v was not equal to a, since a is returned if the button has already been clicked.
        if (v != 'a') {
            sendCOutcomeOverTCP(v); // Send the value over TCP
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
        System.out.println("Misty is playing by specified move...");
        char mv = flipButton(row, col, false); // Misty plays on right board

        //we check that v was not equal to a, since a is returned if the button has already been clicked.
        if (mv != 'a') {
            sendMOutcomeOverTCP(mv); // Send the value over TCP
        }

    }

    private void sendPracticeMessage(String msg) {
        if (TCPClient.singleton != null) {
            new Thread(() -> {
                Log.d("PracticeActivity", "Sending: " + msg);
                TCPClient.singleton.sendMessage(msg);
            }).start();
        }
    }

    private void resetGame() {
        Intent intent = new Intent(PracticeActivity.this, PracticeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void messageReceived(String message) {
        runOnUiThread(() -> {
            Log.d("PracticeActivity", "message received: " + message);
            String[] parts = message.split(";");
            Log.d("PracticeActivity", "messageReceived: parts length: " + parts.length);

            if (parts.length == 2) {
                String type = parts[0].trim();
                if (type.equals("Mistychoice")) {
                    String coordinates = parts[1].trim();
                    Log.d("PracticeActivity", "messageReceived: coordinates: " + coordinates);

                    if (!coordinates.isEmpty() && coordinates.length() >= 2) {
                        try {
                            char rowChar = coordinates.charAt(0);
                            String colStr = coordinates.substring(1);

                            specifiedRow = rowChar - 'A';
                            specifiedCol = Integer.parseInt(colStr) - 1;

                            Log.d("PracticeActivity", "Parsed to row: " + specifiedRow + ", col: " + specifiedCol);
                            mistyTurn();
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            Log.e("PracticeActivity", "Error parsing coordinate: " + coordinates, e);
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
        runOnUiThread(() -> Toast.makeText(PracticeActivity.this, "", Toast.LENGTH_SHORT).show());
    }

    private void sendMOutcomeOverTCP(char mv) {
        // Prepare the message string
        String message = "Moutcome;" + mv + ";";

        // Send the message using the existing sendMessageToPython method
        System.out.println("message being sent: " + message);
        sendMessageToPython(message);

        System.out.println(message);
    }

    private void sendCOutcomeOverTCP(char v) {
        if (mTcpClient != null) {
            // Prepare the message string
            String messageToSend = "Coutcome;" + v + ";";

            sendMessageToPython(messageToSend);
            Log.i("PracticePage", "message being sent: " + messageToSend);

            String MmessageToSend = "Mistyturn;" + "started";
            sendMessageToPython(MmessageToSend);
            Log.i("PracticePage", "message being sent: " + MmessageToSend);
        }
    }

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