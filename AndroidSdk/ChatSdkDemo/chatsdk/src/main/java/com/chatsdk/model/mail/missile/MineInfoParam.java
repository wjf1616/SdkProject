package com.chatsdk.model.mail.missile;

/**
 * Created by im302016121504 on 17/5/16.
 */
public class MineInfoParam {

    private String	    startTime;
    private String		pointId;
    private String		invalidTime;
    private String		expt;
    private String		itemId;


    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(String invalidTime) {
        this.invalidTime = invalidTime;
    }

    public String getExpt() {
        return expt;
    }

    public void setExpt(String expt) {
        this.expt = expt;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

}
