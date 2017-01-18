package com.dogan.amiral.game;

import android.util.Log;

import com.dogan.amiral.game.enums.shipType;

import java.util.ArrayList;
import java.util.Random;

import static com.dogan.amiral.game.enums.shipType.BATTLESHIP;
import static com.dogan.amiral.game.enums.shipType.CARRIER;
import static com.dogan.amiral.game.enums.shipType.CRUISERS;
import static com.dogan.amiral.game.enums.shipType.DESTROYERS;
import static com.dogan.amiral.game.enums.shipType.NONE;
import static com.dogan.amiral.game.enums.shipType.SUBMARINES;



public class gameProcess {


    public static ArrayList<shipType> THE_MY_BOARD= new ArrayList<>();
    public static ArrayList<Integer> THE_MY_BOARD_HITS= new ArrayList<>();

    public static ArrayList<shipType> THE_ENEMY_BOARD= new ArrayList<>();
    public static ArrayList<Integer> THE_ENEMY_BOARD_HITS= new ArrayList<>();


    public static ArrayList<Integer> shipToChooseRemain;
    public static ArrayList<Integer> bombToChooseRemain;

    public static  int LAST_AIM_POSITION=-1;

    public static  boolean IS_MY_TURN=false;

    public void ResetGame()
    {
        THE_MY_BOARD= new ArrayList<>();
        THE_MY_BOARD_HITS= new ArrayList<>();
        THE_ENEMY_BOARD= new ArrayList<>();
        THE_ENEMY_BOARD_HITS= new ArrayList<>();
    }



    public static void PrepareGame()
    {
        for (int i=0;i<225;i++)
        {
            THE_MY_BOARD.add(shipType.NONE);
            THE_ENEMY_BOARD.add(shipType.NONE);

            THE_MY_BOARD_HITS.add(0);
            THE_ENEMY_BOARD_HITS.add(0);
        }


        shipToChooseRemain=new ArrayList<>();

        shipToChooseRemain.add(1);
        shipToChooseRemain.add(1);
        shipToChooseRemain.add(1);
        shipToChooseRemain.add(2);
        shipToChooseRemain.add(2);



        bombToChooseRemain=new ArrayList<>();

        bombToChooseRemain.add(-1);
        bombToChooseRemain.add(2);
        bombToChooseRemain.add(1);
        bombToChooseRemain.add(1);
    }


    public static void ADD_SHIP(int type)
    {
        shipType typeShip=NONE;


        switch (type) {
            case 0:
                typeShip=CARRIER;
                break;
            case 1:
                typeShip=BATTLESHIP;
                break;

            case 2:
                typeShip=CRUISERS;
                break;

            case 3:
                typeShip=SUBMARINES;
                break;

            case 4:
                typeShip=DESTROYERS;
                break;
            case 5:
                typeShip=NONE;
                break;
        }



        ArrayList<Integer> coordinates=null;


        int tryCnt=50;
        while (coordinates==null)
        {
            coordinates=  getRondomCoordinateForShip(typeShip);

            tryCnt--;


            if(tryCnt<0)
            {

                break;
            }
        }


        for (int coor:coordinates) {


            THE_MY_BOARD.set(coor,typeShip);
        }


    }



    //_____ private methods

    private static ArrayList<Integer> getRondomCoordinateForShip(shipType type)
    {
        ArrayList<Integer> coordinates=new ArrayList<>();

        Random rand = new Random();

        boolean isVertical=false;


        int increment=1;
        int size=0;

        int  verticalOrHorizontal = rand.nextInt(2) + 1;

        if(verticalOrHorizontal==1)
        {
            isVertical=false;
            increment=1;
        }
        else
        {
            isVertical=true;
            increment=15;
        }




        switch (type) {
            case CARRIER:
                size = 5;
                break;
            case BATTLESHIP:
                size = 4;
                break;

            case CRUISERS:
                size = 3;
                break;

            case SUBMARINES:
                size = 3;
                break;

            case DESTROYERS:
                size = 2;
                break;
            case NONE:
                size = 0;
                break;
        }


        int  startPoint = rand.nextInt(250) + 1;


        int point=startPoint;
        for(int i=0;i<size;i++ )
        {

            Log.i("POINT","::"+point);

            coordinates.add(point);


            point+=increment;
        }



        if(checkSpaceForShip(coordinates))
        {
            return coordinates;
        }
        else
        {
            return  null;
        }


    }


    private  static boolean  checkSpaceForShip(ArrayList<Integer> coordinates)
    {


        for (int coor:coordinates) {


            try
            {
                if(THE_MY_BOARD.get(coor)!=NONE)
                {
                    return  false;
                }
            }catch (Exception e) {return  false;}


            try
            {
                if(THE_MY_BOARD.get(coor+1)!=NONE)
                {
                    return  false;
                }
            }catch (Exception e) {}

            try
            {
                if(THE_MY_BOARD.get(coor-1)!=NONE)
                {
                    return  false;
                }
            }catch (Exception e) {}


            try
            {
                if(THE_MY_BOARD.get(coor+15)!=NONE)
                {
                    return  false;
                }
            }catch (Exception e) {}


            try
            {
                if(THE_MY_BOARD.get(coor-15)!=NONE)
                {
                    return  false;
                }
            }catch (Exception e) {}




        }

        return true;
    }

}
