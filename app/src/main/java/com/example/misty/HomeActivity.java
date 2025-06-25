package com.example.misty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.misty.Socketconnection.TCPClient;
import com.example.misty.Socketconnection.TCPServer;

// importing libraries for setting up TCP/IP connection between the python and tablet
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class HomeActivity extends Activity implements View.OnClickListener, TCPClient.OnMessageReceived {
    private String selectedDifficulty = "Easy"; // Default selection
    private EditText iPandPort;
    private Button connectButton;
    private TextView connectionStatus;

    private boolean isConnected = false;

    private TCPClient mTcpClient = TCPClient.getInstance();
    private ConnectTask mConnectTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //set the ip and port of the server
        mTcpClient.setIpAddress("192.168.0.190");
        mTcpClient.setIpPortVar(8080);

        // Create the Connect Task
        mConnectTask = new ConnectTask();
        Log.d("HomeActivity","ConnectTask created");

        mTcpClient.addMessageListener(this);

        // Execute the connect task
        mConnectTask.execute();
        Log.d("HomeActivity","ConnectTask executed");

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

        // setting up the ip port address and connect button
        iPandPort = findViewById(R.id.IPandPort);
        iPandPort.setText("192.168.0.144:8080");
        connectButton = findViewById(R.id.ConnectButton);
        //connectionStatus = findViewById(R.id.ConnectionStatus);

        // listener on when the connect button is clicked
        connectButton.setOnClickListener(this);
    }


    public void connected() {
        // to display the connected status
        //connectionStatus.setText("Connected.");
        runOnUiThread(() -> {
            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
        });


    }

    public void connectTablet(View view) {

        // function to start the connect tablet process
        String ipInput = iPandPort.getText().toString();
        String ipaddress = ipInput.split(":")[0];
        String ipport = ipInput.split(":")[1];
        ConnectTask connectTask = new ConnectTask();
        connectTask.execute(ipaddress, ipport);

        //connectionStatus.setText("Trying to connect to server");
        Toast.makeText(this, "Trying to connect to server...", Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onClick(View v) {

        //onclick function to start the connect tablet function defined above
        if (v.getId() == R.id.ConnectButton) {
            connectTablet(v);
        }
    }

    private class ConnectTask extends AsyncTask<String, String, Boolean> {
        private boolean isConnectionSuccessful = false;

        @Override
        protected void onPreExecute() {
            Log.d("ConnectTask", "onPreExecute called");
        }
        @Override
        protected Boolean doInBackground(String... message) {
            Log.d("ConnectTask", "doInBackground called");
            mTcpClient.run();
            if (mTcpClient.isConnected()) {
                mTcpClient.sendMessage("Session started!");
                Log.d("ConnectTask", "session started message sent");
                isConnectionSuccessful= true;
            } else {
                isConnectionSuccessful= false;
            }
            return isConnectionSuccessful;
        }
        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            super.onPostExecute(isSuccessful);
            if (isSuccessful) {
                // If connection is successful, update the button on the UI thread
                runOnUiThread(() -> {
                    connectButton.setText("Connected");
                });
            } else {
                runOnUiThread(() -> {
                    connectButton.setText("Connection failed");
                });
            }if (isSuccessful) {
                // If connection is successful, update the button on the UI thread
                runOnUiThread(() -> {
                    connectButton.setText("Connected");
                });
            } else {
                runOnUiThread(() -> {
                    connectButton.setText("Connection failed");
                });
            }
            Log.d("ConnectTask", "onPostExecute called");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mTcpClient.startListening();
                }
            }).start();
        }
    }
    @Override
    public void messageReceived(String message){
        runOnUiThread(() -> {
            //update UI
            Log.e("HomeActivity", "Message received: " + message);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //remove listener in ondestroy
        mTcpClient.removeMessageListener(this);
        Log.d("HomeActivity", "Listener removed in onDestroy");
    }
}