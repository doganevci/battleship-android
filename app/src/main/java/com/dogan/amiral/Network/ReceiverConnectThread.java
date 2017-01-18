package com.dogan.amiral.Network;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dogan.amiral.game.enums.shipType;
import com.dogan.amiral.models.AllLists;
import com.dogan.amiral.models.GenericSendReceiveModel;
import com.dogan.amiral.models.MovementModel;
import com.dogan.amiral.models.messageModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import static com.dogan.amiral.GameActivity.chatClientThread;
import static com.dogan.amiral.GameActivity.chatServerThread;
import static com.dogan.amiral.game.gameProcess.THE_ENEMY_BOARD_HITS;
import static com.dogan.amiral.game.gameProcess.THE_MY_BOARD;
import static com.dogan.amiral.game.gameProcess.THE_MY_BOARD_HITS;

/**
 * Created by doganevci on 17/01/2017.
 */
public class ReceiverConnectThread extends Thread {

    Socket socket;
    Context cntx;

    GenericSendReceiveModel msgToSend = null;
    GenericSendReceiveModel receivedMessage = null;
    SenderThread  ConnectedSenderThread=null;

    public ReceiverConnectThread(Socket socket, Context cntx){
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
                        if(receivedMessage.getType()==2) // type2 rakipten hamle geldi
                        {

                            if(receivedMessage.getGameMovement().isApproval())
                            {

                                if(receivedMessage.getGameMovement().isMyFireHitTheShip())
                                {

                                    THE_ENEMY_BOARD_HITS.set(receivedMessage.getGameMovement().getCoordinate(),1);
                                }
                                else
                                {
                                    THE_ENEMY_BOARD_HITS.set(receivedMessage.getGameMovement().getCoordinate(),2);
                                }

                                sendBroadCastGame(1,receivedMessage.getGameMovement().isMyFireHitTheShip());

                            }
                            else
                            {
                                if(THE_MY_BOARD.get(receivedMessage.getGameMovement().getCoordinate())!= shipType.NONE)
                                {
                                    THE_MY_BOARD_HITS.set(receivedMessage.getGameMovement().getCoordinate(),1);
                                    sendFireApprovel(receivedMessage.getGameMovement(),true);

                                    sendBroadCastGame(2,true);
                                }
                                else
                                {
                                    sendFireApprovel(receivedMessage.getGameMovement(),false);

                                    sendBroadCastGame(2,false);
                                }



                            }




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


    public void sendFireApprovel(MovementModel mm,boolean isHit)
    {



        mm.setApproval(true);

        mm.setMyFireHitTheShip(isHit);




        GenericSendReceiveModel genNew=new GenericSendReceiveModel();
        genNew.setType(2);
        genNew.setGameMovement(mm);



        if(chatClientThread==null){


            chatServerThread.getConnectedThread().getSenderThread().sendMsg(genNew);

        }
        else
        {
            chatClientThread.getSenderThread().sendMsg(genNew);
        }

        AllLists.THE_MESSAGE_LIST.add(genNew.getMessage());
    }


    private void sendBroadCast(int type) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("type", type);
        LocalBroadcastManager.getInstance(cntx).sendBroadcast(intent);
    }

    private void sendBroadCastGame(int type,boolean isFired) {
        Log.d("sender", "Broadcasting game");
        Intent intent = new Intent("mGameNotifReceiver");
        intent.putExtra("type", type);
        intent.putExtra("isfire", isFired);
        LocalBroadcastManager.getInstance(cntx).sendBroadcast(intent);
    }

}