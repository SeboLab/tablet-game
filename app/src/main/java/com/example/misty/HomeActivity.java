package com.example.misty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private String selectedDifficulty = "Easy"; // Default selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Spinner difficultySpinner = findViewById(R.id.difficultySpinner);
        String[] difficultyLevels = getResources().getStringArray(R.array.difficulty_levels);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficultyLevels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDifficulty = difficultyLevels[position]; // Store selected difficulty
                Toast.makeText(HomeActivity.this, "Selected: " + selectedDifficulty, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            // Go to Avatar Selection Page before starting the game
            Intent intent = new Intent(HomeActivity.this, AvatarSelectionActivity.class);
            intent.putExtra("difficulty", selectedDifficulty);
            startActivity(intent);
        });
        Button practiceButton = findViewById(R.id.practiceButton);
        practiceButton.setOnClickListener(v->{
            Intent intent = new Intent(HomeActivity.this, PracticeActivity.class);
            intent.putExtra("difficulty", selectedDifficulty);
            startActivity(intent);
        });
    }
}
