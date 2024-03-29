package com.dogan.amiral;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dogan.amiral.MessagerModule.DiscussArrayAdapter;
import com.dogan.amiral.MessagerModule.OneComment;
import com.dogan.amiral.Network.ReceiverClientThread;
import com.dogan.amiral.Network.ReceiverServerThread;
import com.dogan.amiral.models.AllLists;
import com.dogan.amiral.models.GenericSendReceiveModel;
import com.dogan.amiral.models.messageModel;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static com.dogan.amiral.GameActivity.PORT;
import static com.dogan.amiral.GameActivity.chatClientThread;
import static com.dogan.amiral.GameActivity.chatServerThread;

public class MainFragment extends Fragment {

    EditText ipTxt;
    EditText txtMessageSend;

    TextView txtYourIp;
    TextView txtServerLog;

    Button btnBeServer;
    Button btnMessageSend;
    Button btnConnectToFriend;


    LinearLayout chatView;
    private ListView lv;
    private DiscussArrayAdapter adapter;



    boolean isConnected=false;





    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);



        txtMessageSend = (EditText)rootView. findViewById(R.id.txtMessageSend);
        ipTxt = (EditText) rootView.findViewById(R.id.iptxt);
        txtYourIp = (TextView) rootView.findViewById(R.id.txtYourIp);
        btnBeServer=(Button) rootView.findViewById(R.id.btnBeServer);
        btnMessageSend=(Button) rootView.findViewById(R.id.btnMessageSend);
        btnConnectToFriend = (Button) rootView.findViewById(R.id.button);
        txtServerLog = (TextView)rootView. findViewById(R.id.txtServerLog);

        chatView=(LinearLayout)rootView.findViewById(R.id.chatView);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

        connectionModeOn(false);

        txtYourIp.setText("Your Ip Adress: "+getIPAddress(true));


        btnConnectToFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("Connecting::","connect request to a friend");

                chatClientThread = new ReceiverClientThread("Clint Dogan:",ipTxt.getText().toString(),Integer.parseInt(PORT),getActivity());
                chatClientThread.start();

                btnConnectToFriend.setText("Connecting, please wait!");


                btnBeServer.setVisibility(View.GONE);

            }
        });





        btnBeServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Waiting::","waiting request from a friend");
                chatServerThread = new ReceiverServerThread(getActivity());
                chatServerThread.start();


                btnBeServer.setText("Connecting, please wait!");
                btnConnectToFriend.setVisibility(View.GONE);
                ipTxt.setVisibility(View.GONE);
            }
        });



        btnMessageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(txtMessageSend.getText().toString());
            }
        });







        lv = (ListView)rootView.findViewById(R.id.listView1);
        adapter = new DiscussArrayAdapter(getActivity(), R.layout.listitem_discuss);
        lv.setAdapter(adapter);


        return rootView;
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

        genNew.getMessage().setThisMe(false);

        AllLists.THE_MESSAGE_LIST.add(genNew.getMessage());


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
            txtYourIp.setVisibility(View.GONE);
            chatView.setVisibility(View.VISIBLE);
        }
        else
        {
            btnMessageSend.setVisibility(View.GONE);
            txtMessageSend.setVisibility(View.GONE);

            btnBeServer.setVisibility(View.VISIBLE);
            ipTxt.setVisibility(View.VISIBLE);
            btnConnectToFriend.setVisibility(View.VISIBLE);
            txtYourIp.setVisibility(View.VISIBLE);
            chatView.setVisibility(View.GONE);
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
        adapter.clear();


        String allText="";
      /*  for(messageModel m: AllLists.THE_MESSAGE_LIST)
        {

            adapter.add(new OneComment(m.isThisMe(), m.getMessage()));

        }
        */
        messageModel m= AllLists.THE_MESSAGE_LIST.get(AllLists.THE_MESSAGE_LIST.size()-1);
        adapter.add(new OneComment(m.isThisMe(), m.getMessage()));


    }





}