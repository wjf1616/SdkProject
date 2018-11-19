package com.chatsdk.model.mail.missile;

import com.chatsdk.model.mail.battle.ArmyParams;

import java.util.List;
/**
 * Created by im302016121504 on 17/5/19.
 */
public class DeadListParams {
    private String	    uid;

    private List<ArmyParams>        deadArr;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<ArmyParams> getDeadArr() {
        return deadArr;
    }

    public void setDeadArr(List<ArmyParams> deadArr) {
        this.deadArr = deadArr;
    }
}
