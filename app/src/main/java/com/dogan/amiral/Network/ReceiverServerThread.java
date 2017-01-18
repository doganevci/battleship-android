package com.dogan.amiral.Network;

import android.content.Context;

import com.dogan.amiral.GENERALPROPERTIES;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by doganevci on 17/01/2017.
 */

public class ReceiverServerThread extends Thread {

    public static ServerSocket serverSocket;
    public String PORT= GENERALPROPERTIES.PORT;
    public ReceiverConnectThread connectThread=null;
    Context cntx;

    public ReceiverServerThread(Context cntx){
        this.cntx= cntx;
    }


    @Override
    public void run() {
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(Integer.parseInt(PORT));


            while (true) {
                socket = serverSocket.accept();

                //TODO Connection modu açıldı

                connectThread = new ReceiverConnectThread( socket,cntx);
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


    public ReceiverConnectThread getConnectedThread()
    {
        return  connectThread;
    }

}
