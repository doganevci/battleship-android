package com.dogan.amiral;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dogan.amiral.Network.ChatClientThread;
import com.dogan.amiral.Network.ChatServerThread;
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



    boolean isConnected=false;
    boolean isServer=false;

    public String PORT="65123";
    ChatClientThread chatClientThread = null;
    ChatServerThread chatServerThread=null;



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


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));


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




                chatClientThread = new ChatClientThread("Clint Dogan:",ipTxt.getText().toString(),Integer.parseInt(PORT),MainActivity.this);
                chatClientThread.start();

            }
        });





        btnBeServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Waiting::","waiting request from a friend");



                 chatServerThread = new ChatServerThread(MainActivity.this);
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
        newMessage.setThisMe(false);

        GenericSendReceiveModel genNew=new GenericSendReceiveModel();
        genNew.setType(1);
        genNew.setMessage(newMessage);



        if (txtMessageSend.getText().toString().equals("")) {
            return;
        }

        if(chatClientThread==null){


            chatServerThread.getConnectedThread().getSenderThread().sendMsg(genNew);

        }
        else
        {
            chatClientThread.getSenderThread().sendMsg(genNew);
        }


        refreshMessageList();

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



    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            // Get extra data included in the Intent
            int type = intent.getIntExtra("type",0);

            if(type==1)
            {
                connectionModeOn(true);
            }
            else if(type == 2)
            {
                refreshMessageList();
            }




        }
    };


    public void refreshMessageList()
    {
        String allText="";
        for(messageModel m: AllLists.THE_MESSAGE_LIST)
        {

            if(m.isThisMe())
            {
                allText+="\nMe::"+m.getMessage();
            }
            else
            {
                allText+="\nEnemy::"+m.getMessage();
            }

        }


        txtServerLog.setText(allText);
    }

}