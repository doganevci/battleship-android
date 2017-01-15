package com.dogan.amiral.models;

import java.io.Serializable;

/**
 * Created by doganevci on 15/01/2017.
 */

public class GenericSendReceiveModel implements Serializable{

    static final long serialVersionUID = -50077493051991507L;

    int type;
    messageModel message;

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
