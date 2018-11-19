package com.chatsdk.model.mail.newbattle;

/**
 * Created by user on 2018/4/9.
 */
public class DetailParams {
    private String armId;
    private int    free;
    private int    hurt;
    private int    dead;

    public String getArmId() {
        return armId;
    }

    public void setArmId(String armId) {
        this.armId = armId;
    }

    public int getDead() {
        return dead;
    }

    public void setDead(int dead) {
        this.dead = dead;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getHurt() {
        return hurt;
    }

    public void setHurt(int hurt) {
        this.hurt = hurt;
    }
}
