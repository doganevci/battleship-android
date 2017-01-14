package com.dogan.amiral;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static android.R.id.message;

public class MainActivity extends AppCompatActivity {

    EditText ipTxt;
    String message = "";

    TextView txtYourIp;
    Button btnBeServer;
    Button btnMessageSend;
    Button btnConnectToFriend;
    EditText txtMessageSend;

    String PORT="65123";


    TextView txtServerLog;


    boolean isConnected=false;
    boolean isServer=false;
    public static Socket theConnectedSocket;
    public static ServerSocket serverSocket;

    public static  SocketSendMessage SocketSendMessage;


    ChatClientThread chatClientThread = null;
    List<ChatClient> userList= new ArrayList<>();

    String msgLog = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtMessageSend = (EditText) findViewById(R.id.txtMessageSend);
        ipTxt = (EditText) findViewById(R.id.iptxt);
        txtYourIp = (TextView) findViewById(R.id.txtYourIp);
        btnBeServer=(Button) findViewById(R.id.btnBeServer);
        btnMessageSend=(Button) findViewById(R.id.btnMessageSend);
        btnConnectToFriend = (Button) findViewById(R.id.button);
        txtServerLog = (TextView) findViewById(R.id.txtServerLog);


        connectionModeOn(false);

        txtYourIp.setText("Your Ip Adress: "+getIPAddress(true));


        btnConnectToFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("Connecting::","connect request to a friend");

                Intent i = new Intent(getApplicationContext(), amiralMainActivity.class);
                i.putExtra("txtIp", ipTxt.getText().toString());

                // startActivity(i);

                isServer=true;




                chatClientThread = new ChatClientThread("CHAT:",ipTxt.getText().toString(),Integer.parseInt(PORT));
                chatClientThread.start();

            }
        });





        btnBeServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Waiting::","waiting request from a friend");



                ChatServerThread chatServerThread = new ChatServerThread();
                chatServerThread.start();

            }
        });



        btnMessageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (txtMessageSend.getText().toString().equals("")) {
                    return;
                }

                if(chatClientThread==null){
                    return;
                }

                chatClientThread.sendMsg(txtMessageSend.getText().toString() + "\n");

            }
        });

    }


    public void connectionModeOn(boolean connected)
    {

        isConnected=connected;

        if(connected)
        {
            btnMessageSend.setVisibility(View.VISIBLE);
            txtMessageSend.setVisibility(View.VISIBLE);
            btnBeServer.setVisibility(View.GONE);
            ipTxt.setVisibility(View.GONE);
            btnConnectToFriend.setVisibility(View.GONE);

        }
        else
        {
            btnMessageSend.setVisibility(View.GONE);
            txtMessageSend.setVisibility(View.GONE);

            btnBeServer.setVisibility(View.VISIBLE);
            ipTxt.setVisibility(View.VISIBLE);
            btnConnectToFriend.setVisibility(View.VISIBLE);

        }

    }







    private class SocketSendMessage extends Thread {

        private Socket hostThreadSocket;
        String sendMessage;

        SocketSendMessage(Socket socket, String message) {
            hostThreadSocket = socket;
            sendMessage = message;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "" + sendMessage;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                message += "replayed: " + msgReply + "\n";

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        txtServerLog.setText(message);
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    txtServerLog.setText(message);
                }
            });
        }

    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }




    private class ChatClientThread extends Thread {

        String name;
        String dstAddress;
        int dstPort;

        String msgToSend = "";
        boolean goOut = false;

        ChatClientThread(String name, String address, int port) {
            this.name = name;
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        connectionModeOn(true);
                    }

                });



                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream.writeUTF(name);
                dataOutputStream.flush();

                while (!goOut) {
                    if (dataInputStream.available() > 0) {
                        msgLog += dataInputStream.readUTF();

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                txtServerLog.setText(msgLog);
                            }
                        });
                    }

                    if(!msgToSend.equals("")){
                        dataOutputStream.writeUTF(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = "";
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"IOEXCEPTION:"+ eString, Toast.LENGTH_LONG).show();
                    }

                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                    }

                });
            }

        }

        private void sendMsg(String msg){
            msgToSend = msg;
        }

        private void disconnect(){
            goOut = true;
        }
    }

    //---------------------------------




    private class ChatServerThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(Integer.parseInt(PORT));
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                       // infoPort.setText("I'm waiting here: "
                              //  + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    socket = serverSocket.accept();
                    ChatClient client = new ChatClient();
                    userList.add(client);

                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            connectionModeOn(true);
                        }

                    });
                    ConnectThread connectThread = new ConnectThread(client, socket);
                    connectThread.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    private class ConnectThread extends Thread {

        Socket socket;
        ChatClient connectClient;
        String msgToSend = "";

        ConnectThread(ChatClient client, Socket socket){
            connectClient = client;
            this.socket= socket;
            client.socket = socket;
            client.chatThread = this;
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                String n = dataInputStream.readUTF();

                connectClient.name = n;

                msgLog += connectClient.name + " connected@" +
                        connectClient.socket.getInetAddress() +
                        ":" + connectClient.socket.getPort() + "\n";
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        txtServerLog.setText(msgLog);
                    }
                });

                dataOutputStream.writeUTF("Welcome " + n + "\n");
                dataOutputStream.flush();

                broadcastMsg(n + " join our chat.\n");

                while (true) {
                    if (dataInputStream.available() > 0) {
                        String newMsg = dataInputStream.readUTF();


                        msgLog += n + ": " + newMsg;
                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                txtServerLog.setText(msgLog);
                            }
                        });

                        broadcastMsg(n + ": " + newMsg);
                    }

                    if(!msgToSend.equals("")){
                        dataOutputStream.writeUTF(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = "";
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                userList.remove(connectClient);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                connectClient.name + " removed.", Toast.LENGTH_LONG).show();

                        msgLog += "-- " + connectClient.name + " leaved\n";
                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                txtServerLog.setText(msgLog);
                            }
                        });

                        broadcastMsg("-- " + connectClient.name + " leaved\n");
                    }
                });
            }

        }

        private void sendMsg(String msg){
            msgToSend = msg;
        }

    }

    private void broadcastMsg(String msg){
        for(int i=0; i<userList.size(); i++){
            userList.get(i).chatThread.sendMsg(msg);
            msgLog += "- send to " + userList.get(i).name + "\n";
        }

        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                txtServerLog.setText(msgLog);
            }
        });
    }


    class ChatClient {
        String name;
        Socket socket;
        ConnectThread chatThread;

    }





}
