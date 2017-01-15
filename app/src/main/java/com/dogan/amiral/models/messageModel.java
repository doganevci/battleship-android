package com.dogan.amiral.models;

import java.io.Serializable;

/**
 * Created by doganevci on 15/01/2017.
 */

public class messageModel implements Serializable{

    static final long serialVersionUID = -50077493051991107L;

    String username;
    String message;

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
