package com.dogan.amiral.game;

import com.dogan.amiral.game.enums.shipType;

import java.util.ArrayList;

public class ship {

    public shipType type;
    public ArrayList<Integer> points;


    private int size;
    private int lifeLevel;
    private String name;

    public ship(shipType TYPE,ArrayList<Integer> pointsOfShip)
    {
        type=TYPE;
        name=TYPE.toString();
        points=pointsOfShip;

        switch (TYPE) {
            case CARRIER:
                size=5;
                lifeLevel=5;
                break;
            case BATTLESHIP:
                size=4;
                lifeLevel=4;
                break;

            case CRUISERS:
                size=3;
                lifeLevel=3;
                break;

            case SUBMARINES:
                size=3;
                lifeLevel=3;
                break;

            case DESTROYERS:
                size=2;
                lifeLevel=2;
                break;
        }


    }


}
