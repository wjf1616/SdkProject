package com.chatsdk.model.mail.newbattle;

import java.util.List;

/**
 * Created by user on 2018/4/9.
 */
public class ExtraArmyInfoParams {
    private List<DetailParams> detail;
    private int          totalHurt;
    private int          totalDead;

    public List<DetailParams> getDetail() {
        return detail;
    }

    public void setDetail(List<DetailParams> detail) {
        this.detail = detail;
    }

    public int getTotalDead() {
        return totalDead;
    }

    public void setTotalDead(int totalDead) {
        this.totalDead = totalDead;
    }

    public int getTotalHurt() {
        return totalHurt;
    }

    public void setTotalHurt(int totalHurt) {
        this.totalHurt = totalHurt;
    }
}
