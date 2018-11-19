package com.chatsdk.model.mail.battleend;

/**
 * Created by zhangkankan on 2017/7/5.
 */
import java.util.List;

public class BattleEndMailContents
{


    private String								ownerName;
    private String								pointId;
    private int									serverId;
    private int									killEnemy;
    private int									mykillEnemy;
    private String					            existTime;
    private String					            destroyed;
    private List<TopInfoParams>				    topInfo;

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName (String ownerName)
    {
        this.ownerName = ownerName;
    }

    public String getPointId()
    {
        return pointId;
    }

    public void setPointId(String pointId)
    {
        this.pointId = pointId;
    }

    public int getServerId()
    {
        return serverId;
    }

    public void setServerId(int serverId)
    {
        this.serverId = serverId;
    }

    public int getKillEnemy()
    {
        return killEnemy;
    }

    public void setKillEnemy(int killEnemy)
    {
        this.killEnemy = killEnemy;
    }

    public int getMykillEnemy()
    {
        return mykillEnemy;
    }

    public void setMykillEnemy(int mykillEnemy)
    {
        this.mykillEnemy = mykillEnemy;
    }

    public String getExistTime()
    {
        return existTime;
    }

    public void setExistTime (String existTime)
    {
        this.existTime = existTime;
    }

    public String getDestroyed()
    {
        return destroyed;
    }

    public void setDestroyed (String destroyed)
    {
        this.destroyed = destroyed;
    }


    public List<TopInfoParams> getTopInfo()
    {
        return topInfo;
    }

    public void setTopInfo(List<TopInfoParams> topInfo)
    {
        this.topInfo = topInfo;
    }

}
