package com.dogan.amiral.Network;

import android.content.Context;

import com.dogan.amiral.MainActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by doganevci on 17/01/2017.
 */

public class ReceiverServerThread extends Thread {

    public static ServerSocket serverSocket;
    public String PORT="65123";
    public ConnectThread connectThread=null;
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

                connectThread = new ConnectThread( socket,cntx);
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


    public ConnectThread getConnectedThread()
    {
        return  connectThread;
    }

}
