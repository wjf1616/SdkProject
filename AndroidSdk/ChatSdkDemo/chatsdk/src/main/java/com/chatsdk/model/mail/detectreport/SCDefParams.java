package com.chatsdk.model.mail.detectreport;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by user on 2018/4/3.
 */

public class SCDefParams {
    @JSONField(name = "1")
    private Object army1;
    @JSONField(name = "2")
    private Object army2;
    @JSONField(name = "3")
    private Object army3;

    private String uuid;
    private int formationNumber;
    private int index;
    private boolean isSelf;

    public int getFormationNumber() {
        return formationNumber;
    }

    public void setFormationNumber(int formationNumber) {
        this.formationNumber = formationNumber;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Object getArmy1() {
        return army1;
    }

    public void setArmy1(Object army1) {
        this.army1 = army1;
    }

    public Object getArmy2() {
        return army2;
    }

    public void setArmy2(Object army2) {
        this.army2 = army2;
    }

    public Object getArmy3() {
        return army3;
    }

    public void setArmy3(Object army3) {
        this.army3 = army3;
    }
}
