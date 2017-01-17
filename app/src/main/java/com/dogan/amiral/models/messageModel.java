package com.dogan.amiral.models;

import java.io.Serializable;

/**
 * Created by doganevci on 15/01/2017.
 */

public class messageModel implements Serializable{

    static final long serialVersionUID = -50077493051991107L;

    String username;
    String message;
    boolean isThisMe;

    public messageModel()
    {
        isThisMe=true;
    }


    public boolean isThisMe() {
        return isThisMe;
    }

    public void setThisMe(boolean thisMe) {
        isThisMe = thisMe;
    }



    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
