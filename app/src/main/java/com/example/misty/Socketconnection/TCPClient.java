package com.example.misty.Socketconnection;

import android.os.AsyncTask;
import android.util.Log;

import com.example.misty.HomeActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

    private String serverMessage;
    //message to send to  the server

    public static final String SERVERIP = "192.168.1.105";

    //public static final String SERVERIP = "10.0.0.10";

    // your computer IP address
    public static final int SERVERPORT = 8080;
    private OnMessageReceived mMessageListener = null;
    //while this is true, the server will continue running
    private boolean mRun = false;
    private String ipAddressVar;
    private int ipPortVar;
    private HomeActivity owner;
    private TCPClientOwner sessionOwner;

    //used to send messages
    PrintWriter out;
    // used to read messages from the server
    BufferedReader in;

    public static TCPClient singleton = null;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener, HomeActivity owner) {
        mMessageListener = listener;
        ipAddressVar = null;
        this.owner = owner;
        this.sessionOwner = null;
    }

    public void setSessionOwner(TCPClientOwner sessionOwner){

        this.sessionOwner = sessionOwner;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(final String message){
        if (out != null && !out.checkError()) {
            if (sessionOwner != null) {
                // notify the owner that the message is being sent
                new DisableButtonsTask().execute(sessionOwner);
                        }
            out.println(message);
            out.flush();
                    }
                }

    public void stopClient(){

        mRun = false;
    }

    public void run() {

        mRun = true; //start the loop for receiving messages
        Log.d("TCPClient", "run: Starting receiving thread"); // check that the thread started

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr;
            int ipPortVar;

            if (ipAddressVar == null) {
                serverAddr = InetAddress.getByName(SERVERIP);
                ipPortVar = SERVERPORT;
            }
            else {
                serverAddr = InetAddress.getByName(ipAddressVar);
                //use ipPortVar that was input by user
                ipPortVar = this.ipPortVar;
            }

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, ipPortVar);
            Log.d("TCPClient", "Socket created successfully."); // ADD THIS LOG HERE
            //owner.connected();
            Log.e("TCP Client", "C: Connected");

            new NotifyConnectedTask().execute(owner); // Notify connected on UI thread

            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                out.print("Session started!\r\n");
                out.flush();

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");


                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    Log.d("TCPClient", "Attempting to read message..."); // ADD THIS LOG BEFORE ATTEMPTING TO READ
                    Log.e("TCP Client", "Running");
                    serverMessage = in.readLine();
                    Log.e("TCP Client", "Receiving" + in);


                    if (serverMessage != null && mMessageListener != null) {
                        Log.d("TCPClient", "Received message: " + serverMessage); // ADD THIS LOG AFTER RECEIVING MESSAGE
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                        new NotifyMessageReceivedTask().execute(sessionOwner, serverMessage);
                    }
                    serverMessage = null;

                }



                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");


            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    public String getIpAddress() {

        return ipAddressVar;
    }

    public void setIpAddress(String ipAddressVar) {

        this.ipAddressVar = ipAddressVar;
    }

    public int getIpPortVar() {

        return ipPortVar;
    }

    public void setIpPortVar(int ipPortVar){

        this.ipPortVar = ipPortVar;
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
    //create an async task to run the code in the main thread
    private static class NotifyConnectedTask extends AsyncTask<HomeActivity, Void, Void> {
        @Override
        protected Void doInBackground(HomeActivity... owners) {
            // Run this on the UI thread
            owners[0].connected();
            return null;
        }
    }
    //create an async task to run the code in the main thread
    private static class NotifyMessageReceivedTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            // Run this on the UI thread
            TCPClientOwner sessionOwner = (TCPClientOwner) params[0];
            String message = (String) params[1];
            sessionOwner.messageReceived(message);
            return null;
        }
    }
    //create an async task to run the code in the main thread
    private static class DisableButtonsTask extends AsyncTask<TCPClientOwner, Void, Void> {
        @Override
        protected Void doInBackground(TCPClientOwner... sessionOwners) {
            // Run this on the UI thread
            sessionOwners[0].disableButtons();
            return null;
        }
    }
}