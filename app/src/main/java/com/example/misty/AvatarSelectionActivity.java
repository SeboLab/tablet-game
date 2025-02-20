package com.example.misty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AvatarSelectionActivity extends AppCompatActivity {
    private String selectedDifficulty;
    private boolean isRedAvatarSelected;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_selection);

        selectedDifficulty = getIntent().getStringExtra("difficulty");

        TextView avatarText = findViewById(R.id.avatarText);
        avatarText.setText("Choose Your Avatar for " + selectedDifficulty);

        Button redAvatarButton = findViewById(R.id.redAvatarButton);


        Button blueAvatarButton = findViewById(R.id.blueAvatarButton);


        redAvatarButton.setOnClickListener(v -> startGame("Red"));
        blueAvatarButton.setOnClickListener(v -> startGame("Blue"));
    }

    private void startGame(String avatarColor) {
        isRedAvatarSelected = avatarColor.equals("Red");
        Intent intent = new Intent(AvatarSelectionActivity.this, GamePage.class);
        intent.putExtra("difficulty", selectedDifficulty);
        intent.putExtra("avatar", avatarColor);
        startActivity(intent);
    }
}
