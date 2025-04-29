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

public class HomeActivity extends Activity implements View.OnClickListener {
    private String selectedDifficulty = "Easy"; // Default selection
    private EditText iPandPort;
    private Button connectButton;
    private TextView connectionStatus;

    private TCPClient mTcpClient;
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

        // setting up the ip port address and connect button
        iPandPort = findViewById(R.id.IPandPort);
        iPandPort.setText("192.168.1.105:8080");
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
        connectTask.owner = this;
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

    public class ConnectTask extends AsyncTask<String,String,TCPClient> {

        private String ipaddress;
        public HomeActivity owner;
        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            TCPClient mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                    onProgressUpdate(message);
                    Log.e("HomeActivity", "Message received from server: " + message);

                }
            }, owner);

            if (this.validIP(message[0])){
                mTcpClient.setIpAddress(message[0]);
                mTcpClient.setIpPortVar(Integer.parseInt(message[1]));
                //if valid, write ip in text file
                BufferedWriter writer = null;
                try
                {
                    writer = new BufferedWriter(new FileWriter("/Users/tflanagan/Desktop/hri.txt"));
                    writer.write(message[0]);
                }
                catch (IOException e)
                {
                }
                finally
                {
                    try
                    {
                        if ( writer != null)
                            writer.close( );
                    }
                    catch ( IOException e)
                    {
                    }
                }

            } else {  //if not valid IP, try to read the one from the text file
                String ipaddress = null;
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader("/sdcard/Movies/ip.txt"));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    String savedIP = br.readLine();
                    br.close();
                    if (this.validIP(savedIP))
                        mTcpClient.setIpAddress(savedIP);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // mTcpClient.setIpAddress(message[0]);
            TCPClient.singleton = mTcpClient;
            mTcpClient.run();

            return null;
        }

        public boolean validIP(String ip) {
            // function to check if the ip address entered is valid or not
            if (ip == null || ip.isEmpty()) return false;
            ip = ip.trim();
            if ((ip.length() < 6) & (ip.length() > 15)) return false;

            try {
                Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
                Matcher matcher = pattern.matcher(ip);
                return matcher.matches();
            } catch (PatternSyntaxException ex) {
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //in the arrayList we add the messaged received from server
            // arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            //mAdapter.notifyDataSetChanged();
        }
    }
}
