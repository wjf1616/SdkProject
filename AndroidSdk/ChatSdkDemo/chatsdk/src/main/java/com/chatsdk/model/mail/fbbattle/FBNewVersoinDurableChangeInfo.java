package com.chatsdk.model.mail.fbbattle;

/**
 * Created by mac on 2018/9/6.
 */

public class FBNewVersoinDurableChangeInfo {
    private int type;  //48 地块 49 世界建筑
    private String configId;  //对应的配置 id
    private int level;   //地块 没有这个字断
    private int oldValue;
    private int atkValue;
    private int currValue;
    private int limit; //上限

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getOldValue() {
        return oldValue;
    }

    public void setOldValue(int oldValue) {
        this.oldValue = oldValue;
    }

    public int getAtkValue() {
        return atkValue;
    }

    public void setAtkValue(int atkValue) {
        this.atkValue = atkValue;
    }

    public int getCurrValue() {
        return currValue;
    }

    public void setCurrValue(int currValue) {
        this.currValue = currValue;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
