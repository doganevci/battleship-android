package com.dogan.amiral.models;


import java.io.Serializable;

public class MovementModel implements Serializable {

    boolean Approval;
    boolean isMyFireHitTheShip;

    int coordinate;
    int type;

    static final long serialVersionUID = -50077493451991107L;


    public boolean isApproval() {
        return Approval;
    }

    public void setApproval(boolean approval) {
        Approval = approval;
    }

    public boolean isMyFireHitTheShip() {
        return isMyFireHitTheShip;
    }

    public void setMyFireHitTheShip(boolean myFireHitTheShip) {
        isMyFireHitTheShip = myFireHitTheShip;
    }

    public int getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(int coordinate) {
        this.coordinate = coordinate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }


}
