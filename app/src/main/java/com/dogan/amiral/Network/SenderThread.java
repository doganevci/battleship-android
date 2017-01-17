package com.dogan.amiral.Network;

import android.util.Log;

import com.dogan.amiral.models.AllLists;
import com.dogan.amiral.models.GenericSendReceiveModel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by doganevci on 17/01/2017.
 */

public class SenderThread extends Thread {

    Socket socket;
    GenericSendReceiveModel msgToSend = null;
    ObjectOutputStream dataOutputStream = null;

    public SenderThread(Socket socket,ObjectOutputStream data){
        this.socket= socket;
        this.dataOutputStream=data;
    }

    @Override
    public void run() {


        try {

            while (true) {

                if(msgToSend!=null){
                    Log.i("MESSAGESENDER::","SENDING");



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

    public  void sendMsg(GenericSendReceiveModel msg){
        msgToSend = msg;
    }

    public  void sendMovementPlay(GenericSendReceiveModel msg){
        msgToSend = msg;
    }

}