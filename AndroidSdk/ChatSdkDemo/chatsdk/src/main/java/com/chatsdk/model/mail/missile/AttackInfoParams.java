package com.chatsdk.model.mail.missile;

/**
 * Created by lzh on 16/10/26.
 */
public class AttackInfoParams {

    private String	    marchUUid1;
    private String		srcServerId;
    private String		pointId1;
    private String		marchTime1;
    private String		side;
    private String		marchUUid2;
    private String		marchTime2;
    private String		serverId;
    private String		cross;

    public String getPointId2() {
        return pointId2;
    }

    public void setPointId2(String pointId2) {
        this.pointId2 = pointId2;
    }

    private String	    pointId2;

    public String getMarchUUid1() {
        return marchUUid1;
    }

    public void setMarchUUid1(String marchUUid1) {
        this.marchUUid1 = marchUUid1;
    }

    public String getSrcServerId() {
        return srcServerId;
    }

    public void setSrcServerId(String srcServerId) {
        this.srcServerId = srcServerId;
    }

    public String getPointId1() {
        return pointId1;
    }

    public void setPointId1(String pointId1) {
        this.pointId1 = pointId1;
    }

    public String getMarchTime1() {
        return marchTime1;
    }

    public void setMarchTime1(String marchTime1) {
        this.marchTime1 = marchTime1;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getMarchUUid2() {
        return marchUUid2;
    }

    public void setMarchUUid2(String marchUUid2) {
        this.marchUUid2 = marchUUid2;
    }

    public String getMarchTime2() {
        return marchTime2;
    }

    public void setMarchTime2(String marchTime2) {
        this.marchTime2 = marchTime2;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getCross() {
        return cross;
    }

    public void setCross(String cross) {
        this.cross = cross;
    }
}
