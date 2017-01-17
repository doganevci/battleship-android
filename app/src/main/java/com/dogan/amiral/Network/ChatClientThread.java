package com.dogan.amiral.Network;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.dogan.amiral.models.AllLists;
import com.dogan.amiral.models.GenericSendReceiveModel;
import com.dogan.amiral.models.messageModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by doganevci on 17/01/2017.
 */

public class ChatClientThread extends Thread {

    String name;
    String dstAddress;
    int dstPort;
    SenderThread ClientSenderThread=null;
    Context cntx;



    GenericSendReceiveModel msgToSend = null;
    GenericSendReceiveModel receivedMessage = null;

    boolean goOut = false;

    public ChatClientThread(String name, String address, int port,Context cntx) {
        this.name = name;
        dstAddress = address;
        dstPort = port;
        this.cntx=cntx;
    }

    @Override
    public void run() {
        Socket socket = null;
        ObjectOutputStream dataOutputStream = null;
        ObjectInputStream dataInputStream = null;

        try {
            socket = new Socket(dstAddress, dstPort);
            socket.setReceiveBufferSize(50000);

           //TODO Broadcast ConnectionOn
            sendBroadCast(1);


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



            while (!goOut) {


                if ((receivedMessage=(GenericSendReceiveModel)(dataInputStream.readObject())) != null) {

                    Log.i("MESSAGE::","RECEIVING");


                    if(receivedMessage.getType()==1)
                    {
                        AllLists.THE_MESSAGE_LIST.add(receivedMessage.getMessage());

                        //TODO Broadcast to Refresh messages list
                        sendBroadCast(2);
                    }
                    receivedMessage=null;
                }


            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            final String eString = e.toString();

        } catch (IOException e) {
            e.printStackTrace();
            final String eString = e.toString();

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

    public void sendMsg(GenericSendReceiveModel msg){
        msgToSend = msg;
    }

    public void disconnect(){
        goOut = true;
    }

    public SenderThread getSenderThread()
    {
        return  ClientSenderThread;
    }


    private void sendBroadCast(int type) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("type", type);
        LocalBroadcastManager.getInstance(cntx).sendBroadcast(intent);
    }
}
