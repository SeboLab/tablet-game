package com.example.misty.Socketconnection;

import android.os.AsyncTask;
import android.util.Log;

import com.example.misty.HomeActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TCPClient {

    private String serverMessage;
    // Constants for default server IP and port
    public static final String SERVERIP = "10.150.93.11";
    public static final int SERVERPORT = 8080;

    private String ipAddress;
    private int ipPortVar;
    private boolean running = false;
    private PrintWriter bufferOut;
    private BufferedReader bufferIn;
    private Socket socket;
    private List<OnMessageReceived> messageListeners = new ArrayList<>(); // List of listeners

    private boolean isConnected = false;
    public static TCPClient singleton = null; // Singleton instance
    private TCPClient() {
        // Private constructor to enforce singleton pattern
        // Initialize with default values
        this.ipAddress = SERVERIP;
        this.ipPortVar = SERVERPORT;
    }
    public static TCPClient getInstance() {
        if (singleton == null)
            singleton = new TCPClient();
        return singleton;
    }
    //method to add listeners
    public void addMessageListener(OnMessageReceived listener) {

        messageListeners.add(listener);
    }
    //method to remove listeners
    public void removeMessageListener(OnMessageReceived listener) {
        messageListeners.remove(listener);
    }
    public interface OnMessageReceived {
        void messageReceived(String message);
    }
    private void sendMessageToListeners(String message) {
        for (OnMessageReceived listener : messageListeners) {
            listener.messageReceived(message);
        }
    }
    public void setIpAddress(String ipAddress) {
        //check if its empty, if so, use the default one
        if (ipAddress.isEmpty())
            this.ipAddress= SERVERIP;
        else
            this.ipAddress = ipAddress;
    }

    public void setIpPortVar(int ipPortVar) {
        //check if its 0, if so, use default one
        if(ipPortVar==0)
            this.ipPortVar= SERVERPORT;
        else
            this.ipPortVar = ipPortVar;
    }

    public void sendMessage(String message) {
        Log.d("TCPClient", "sendMessage called"); // Log that this method is being reached.
        if (socket == null) {
            Log.e("TCPClient", "Socket is null!");
        } else {
            Log.d("TCPClient", "Socket is not null!");
        }
        if (bufferOut == null) {
            Log.e("TCPClient", "BufferOut is null!");
        } else {
            Log.d("TCPClient", "BufferOut is not null!");
        }
        if (bufferOut != null && !bufferOut.checkError()) {
            bufferOut.println(message);
            bufferOut.flush();
            Log.e("TCPClient", "Sent Message: " + message);
        } else {
            Log.e("TCPClient", "Error: Could not send message - Output stream is null or in error state.");
        }
    }

    public void run() {
        Log.d("TCPClient", "C: Attempting to connect with IP: " + ipAddress + " and Port:" + ipPortVar);

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(ipAddress);

            socket = new Socket(serverAddr, ipPortVar);
            Log.d("TCPClient", "C: Connected.");
            isConnected = true;

            // Create the buffers
            bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            Log.d("TCPClient", "bufferOut created");
            bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Log.d("TCPClient", "bufferIn created");
            Log.e("TCPClient", "C: Session Started!");

        } catch (Exception e) {
            Log.e("TCPClient", "C: Error", e);
            Log.e("TCPClient", "C: Could not Connect.");
        }
    }
    public void startListening() {
        String serverMessage;
        try {
            //keep listening for message while there is not an error.
            while (true) {
                Log.e("TCPClient", "C: Waiting for message...");
                serverMessage = bufferIn.readLine();
                if (serverMessage!=null){
                    //send the message to the listeners
                    if (!serverMessage.isEmpty()) {
                        sendMessageToListeners(serverMessage);
                        Log.e("TCPClient", "C: Received Message - " + serverMessage);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("TCPClient", "C: Error Listening", e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    //the socket must be closed. It is closed after each session
                    socket.close();
                } catch (IOException e) {
                    Log.e("TCPClient", "C: Error closing socket", e);
                }
                Log.e("TCPClient", "C: Connection Closed.");
            }
        }
    }
    public boolean isConnected() {
        return isConnected;
    }
}