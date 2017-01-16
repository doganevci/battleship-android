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

import com.dogan.amiral.models.AllLists;
import com.dogan.amiral.models.GenericSendReceiveModel;
import com.dogan.amiral.models.messageModel;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import static com.dogan.amiral.models.AllLists.THE_MESSAGE_LIST;

public class MainActivity extends AppCompatActivity {

    EditText ipTxt;
    EditText txtMessageSend;

    TextView txtYourIp;
    TextView txtServerLog;

    Button btnBeServer;
    Button btnMessageSend;
    Button btnConnectToFriend;


    String PORT="65123";


    boolean isConnected=false;
    boolean isServer=false;
    public static ServerSocket serverSocket;

    ChatClientThread chatClientThread = null;
    ConnectThread connectThread=null;

    SenderThread  ClientSenderThread=null;
    SenderThread  ConnectedSenderThread=null;

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




                chatClientThread = new ChatClientThread("Clint Dogan:",ipTxt.getText().toString(),Integer.parseInt(PORT));
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



                sendMessage(txtMessageSend.getText().toString());


            }
        });

    }


    public void sendMessage(String messageText)
    {
        //THE_MESSAGE_LIST.


        Log.i("SENDMESSAGEBTN::","clicked");

        messageModel newMessage=new messageModel();
        newMessage.setMessage(messageText);
        newMessage.setUsername("USER");

        GenericSendReceiveModel genNew=new GenericSendReceiveModel();
        genNew.setType(1);
        genNew.setMessage(newMessage);



        if (txtMessageSend.getText().toString().equals("")) {
            return;
        }

        if(chatClientThread==null){


            ConnectedSenderThread.sendMsg(genNew);
            // broadcastMsg(messageText);
        }
        else
        {
            ClientSenderThread.sendMsg(genNew);
        }

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



    //____Client
    private class ChatClientThread extends Thread {

        String name;
        String dstAddress;
        int dstPort;

        GenericSendReceiveModel msgToSend = null;
        GenericSendReceiveModel receivedMessage = null;

        boolean goOut = false;

        ChatClientThread(String name, String address, int port) {
            this.name = name;
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;
            ObjectOutputStream dataOutputStream = null;
            ObjectInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                socket.setReceiveBufferSize(50000);

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        connectionModeOn(true);
                    }

                });



                messageModel newMessage=new messageModel();
                newMessage.setMessage("Hi,");
                newMessage.setUsername(name);

                GenericSendReceiveModel genNew=new GenericSendReceiveModel();
                genNew.setType(1);
                genNew.setMessage(newMessage);

                dataOutputStream = new ObjectOutputStream(
                        socket.getOutputStream());
                dataInputStream = new ObjectInputStream(socket.getInputStream());
                dataOutputStream.writeObject(genNew);
                dataOutputStream.flush();


               ClientSenderThread = new SenderThread(socket,dataOutputStream);
                ClientSenderThread.start();



                //dataInputStream = new ObjectInputStream(socket.getInputStream());

                while (!goOut) {
                    Log.i("CLIENT::","WROKING");





                        if ((receivedMessage=(GenericSendReceiveModel)(dataInputStream.readObject())) != null) {
                            //  if (dataInputStream.available()>0){

                            Log.i("MESSAGE::","RECEIVING");

                           // receivedMessage=(GenericSendReceiveModel)(dataInputStream.readObject());



                            if(receivedMessage.getType()==1)
                            {
                                AllLists.THE_MESSAGE_LIST.add(receivedMessage.getMessage());
                                msgLog+="mesaj::"+receivedMessage.getMessage().getMessage();
                            }


                            MainActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    txtServerLog.setText(msgLog);
                                }
                            });

                            receivedMessage=null;
                        }


                    Log.i("ARADA::","KALDIM");

                    if(msgToSend!=null){

                        Log.i("MESSAGE::","SENDING");

                        dataOutputStream.writeObject(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = null;

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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {


                        Log.i("SOCKET::","closing");
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {

                    Log.i("SOCKET2::","closing");

                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {


                    Log.i("SOCKET3::","closing");
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }


            }

        }

        private void sendMsg(GenericSendReceiveModel msg){
            msgToSend = msg;
        }

        private void disconnect(){
            goOut = true;
        }
    }

    //----SERVER
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
                    connectThread = new ConnectThread(client, socket);
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

    //____Guest_
    private class ConnectThread extends Thread {

        Socket socket;
        ChatClient connectClient;


        GenericSendReceiveModel msgToSend = null;
        GenericSendReceiveModel receivedMessage = null;

        ConnectThread(ChatClient client, Socket socket){
            connectClient = client;
            this.socket= socket;
            client.socket = socket;
            client.chatThread = this;
        }

        @Override
        public void run() {
            ObjectInputStream dataInputStream = null;
            ObjectOutputStream dataOutputStream = null;

            try {
                socket.setReceiveBufferSize(50000);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            try {
                dataInputStream = new ObjectInputStream(socket.getInputStream());
                dataOutputStream = new ObjectOutputStream(socket.getOutputStream());


                GenericSendReceiveModel n = (GenericSendReceiveModel) dataInputStream.readObject();

                connectClient.name = n.getMessage().getUsername();

                msgLog += connectClient.name + " connected@" +
                        connectClient.socket.getInetAddress() +
                        ":" + connectClient.socket.getPort() + "\n";
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        txtServerLog.setText(msgLog);
                    }
                });


                messageModel newMessage=new messageModel();
                newMessage.setMessage(""+n.getMessage().getUsername()+ "join chat.");
                newMessage.setUsername("Server:");

                GenericSendReceiveModel genNew=new GenericSendReceiveModel();
                genNew.setType(1);
                genNew.setMessage(newMessage);



                dataOutputStream.writeObject(genNew);
                dataOutputStream.flush();


                //Dikkat dataoutputstream'i birden fazla yaratırsak  streamcorruptedexception veriri
                ConnectedSenderThread = new SenderThread(socket,dataOutputStream);
                ConnectedSenderThread.start();

                while (true) {

                    Log.i("SERVER::","WROKING");
                    try {





                        if ((receivedMessage=(GenericSendReceiveModel)(dataInputStream.readObject())) != null) {
                      //  if (dataInputStream.available()>0){

                            Log.i("MESSAGE::","RECEIVING");

                           // receivedMessage=(GenericSendReceiveModel)(dataInputStream.readObject());

                            if(receivedMessage.getType()==1)
                            {
                                AllLists.THE_MESSAGE_LIST.add(receivedMessage.getMessage());
                                msgLog+="mesaj::"+receivedMessage.getMessage().getMessage();
                            }


                            MainActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    txtServerLog.setText(msgLog);
                                }
                            });

                            receivedMessage=null;
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    Log.i("ARADA::","KALDIM");

                    if(msgToSend!=null){

                        Log.i("MESSAGE::","SENDING");

                        dataOutputStream.writeObject(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = null;

                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {

                Log.i("SOCKET::","closing");

                if (dataInputStream != null) {

                    Log.i("SOCKET2::","closing");

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

                        // broadcastMsg("-- " + connectClient.name + " leaved\n");
                    }
                });
            }

        }

        private void sendMsg(GenericSendReceiveModel msg){
            msgToSend = msg;
        }

    }






    private class SenderThread extends Thread {

        Socket socket;
        GenericSendReceiveModel msgToSend = null;
        ObjectOutputStream dataOutputStream = null;

        SenderThread(Socket socket,ObjectOutputStream data){
            this.socket= socket;
            this.dataOutputStream=data;
        }

        @Override
        public void run() {


            try {

              //  dataOutputStream = new ObjectOutputStream(socket.getOutputStream());

                while (true) {

                    if(msgToSend!=null){
                        Log.i("MESSAGESENDERNEWW::","SENDING");
                        dataOutputStream.writeObject(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = null;

                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }


            }

        }

        private void sendMsg(GenericSendReceiveModel msg){
            msgToSend = msg;
        }

    }






    private void broadcastMsg(String msg){


        messageModel newMessage=new messageModel();
        newMessage.setMessage(msg);
        newMessage.setUsername("Server:");

        GenericSendReceiveModel genNew=new GenericSendReceiveModel();
        genNew.setType(1);
        genNew.setMessage(newMessage);


        for(int i=0; i<userList.size(); i++){
            userList.get(i).chatThread.sendMsg(genNew);
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