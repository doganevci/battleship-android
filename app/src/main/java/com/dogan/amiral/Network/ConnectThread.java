package com.dogan.amiral.Network;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.dogan.amiral.MainActivity;
import com.dogan.amiral.models.AllLists;
import com.dogan.amiral.models.GenericSendReceiveModel;
import com.dogan.amiral.models.messageModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by doganevci on 17/01/2017.
 */
public class ConnectThread extends Thread {

    Socket socket;
    Context cntx;

    GenericSendReceiveModel msgToSend = null;
    GenericSendReceiveModel receivedMessage = null;
    SenderThread  ConnectedSenderThread=null;

    public ConnectThread( Socket socket,Context cntx){
        this.socket= socket;
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
            sendBroadCast(1);

            GenericSendReceiveModel n = (GenericSendReceiveModel) dataInputStream.readObject();


            AllLists.THE_MESSAGE_LIST.add(n.getMessage());
            // TODO messajlar yenilenecek
            sendBroadCast(2);


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

                try {


                    if ((receivedMessage=(GenericSendReceiveModel)(dataInputStream.readObject())) != null) {


                        Log.i("MESSAGE::","RECEIVING");



                        if(receivedMessage.getType()==1)
                        {
                            AllLists.THE_MESSAGE_LIST.add(receivedMessage.getMessage());
                           //TODO listeyi yenile mesajların
                            sendBroadCast(2);
                        }



                        receivedMessage=null;
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
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



        }

    }

    private void sendMsg(GenericSendReceiveModel msg){
        msgToSend = msg;
    }


    public SenderThread getSenderThread()
    {
        return  ConnectedSenderThread;
    }


    private void sendBroadCast(int type) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("type", type);
        LocalBroadcastManager.getInstance(cntx).sendBroadcast(intent);
    }

}