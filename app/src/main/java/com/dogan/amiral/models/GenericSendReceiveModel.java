package com.dogan.amiral.models;

import java.io.Serializable;


public class GenericSendReceiveModel implements Serializable{

    static final long serialVersionUID = -50077493051991507L;

    int type;
    messageModel message;
    MovementModel gameMovement;



    public MovementModel getGameMovement() {
        return gameMovement;
    }

    public void setGameMovement(MovementModel gameMovement) {
        this.gameMovement = gameMovement;
    }


    public messageModel getMessage() {
        return message;
    }

    public void setMessage(messageModel message) {
        this.message = message;
    }

    public int getType() {

        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
