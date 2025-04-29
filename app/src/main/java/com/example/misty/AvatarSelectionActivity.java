package com.example.misty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
//TCP imports
import android.os.AsyncTask;
import android.os.Handler;
import com.example.misty.Socketconnection.TCPClient;
import com.example.misty.Socketconnection.TCPClientOwner;

import java.util.ArrayList;
import java.util.List;

public class AvatarSelectionActivity extends AppCompatActivity implements  TCPClientOwner {
    private String selectedDifficulty;
    private String selectedAvatar; // Variable to store the chosen avatar
    private boolean isRedAvatarSelected;

    //private Button nextPageButton; // declare at class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_selection);

        if (TCPClient.singleton != null) {
            TCPClient.singleton.setSessionOwner(this);
        }

        selectedDifficulty = getIntent().getStringExtra("difficulty");

        TextView avatarText = findViewById(R.id.avatarText);
        avatarText.setText("Choose Your Avatar for " + selectedDifficulty);

        Button redAvatarButton = findViewById(R.id.redAvatarButton);

        Button blueAvatarButton = findViewById(R.id.blueAvatarButton);

        Button nextPageButton = findViewById(R.id.nextPageButton);

        // Set click listeners for the avatar buttons
        redAvatarButton.setOnClickListener(v -> {
            selectedAvatar = "Red";
            sendAvatarChoice(); // Call method to send choice to TCPClient
        });

        blueAvatarButton.setOnClickListener(v -> {
            selectedAvatar = "Blue";
            sendAvatarChoice();// Call method to send choice to TCPClient
        });

        nextPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(AvatarSelectionActivity.this, GamePage.class);
            intent.putExtra("avatar", selectedAvatar);
            intent.putExtra("difficulty", selectedDifficulty);
            startActivity(intent);
        });
        // disable the next page button by doing false
        nextPageButton.setClickable(true);
        nextPageButton.setEnabled(true);
        // Set up a delay to allow sending before going to the next page.
        new Handler().postDelayed(() -> {
            nextPageButton.setClickable(true);
        }, 5000);


        //redAvatarButton.setOnClickListener(v -> startGame("Red"));
        //blueAvatarButton.setOnClickListener(v -> startGame("Blue"));
    }
    private void sendAvatarChoice() {
        if (selectedAvatar != null) {
            //prepare list for TextUtils.Join
            List<String> buttonChoiceList = new ArrayList<>();
            buttonChoiceList.add(selectedAvatar);
            // Join the button choice into a string
            String buttonIdsString = TextUtils.join(",", buttonChoiceList) + ";";

            // Send the avatar choice via TCP
            new Thread(() -> {
                if (TCPClient.singleton != null) {
                    System.out.println("message being sent: avatar;" + buttonIdsString);
                    TCPClient.singleton.sendMessage("avatar;" + buttonIdsString);
                    System.out.println("avatar;" + buttonIdsString);
                }
            }).start();
        }
    }

    @Override
    public void messageReceived(String message) {
        System.out.println(message);
        // Global.processMessage(message, buttonContainer);  // Assuming buttonContainer is defined elsewhere
    }

    @Override
    public void disableButtons() {
        // Implement button disabling logic here if needed
    }

    public class sendMessage extends AsyncTask<String, Void, Void> {
        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(String... message) {
            String message1 = message[0];
            if (TCPClient.singleton != null) {
                TCPClient.singleton.sendMessage(message1);
            }
            return null;
        }
    }
}

