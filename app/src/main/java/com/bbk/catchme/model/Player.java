package com.bbk.catchme.model;

import com.badlogic.androidgames.framework.math.Vector2;

public class Player {
    Vector2 positionCenter;

    enum Roles {
        Chased,
        Chasing
    }

    Roles role;

    public Player(Vector2 positionCenter, Roles role) {
        this.positionCenter = positionCenter;
        this.role = role;
    }

    public Vector2 getPositionCenter() {
        return positionCenter;
    }

    public void setPositionCenter(Vector2 positionCenter) {
        this.positionCenter = positionCenter;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }
}
