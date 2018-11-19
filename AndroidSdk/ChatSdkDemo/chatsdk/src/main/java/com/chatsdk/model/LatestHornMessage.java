package com.chatsdk.model;

/**
 * Created by lzh on 17/2/24.
 */
public class LatestHornMessage {
    private String name;
    private String asn;
    private String msg;
    private String channelType;
    private String uid;

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getAsn()
    {
        return asn;
    }
    public void setAsn(String asn)
    {
        this.asn = asn;
    }
    public String getMsg()
    {
        return msg;
    }
    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    public String getChannelType()
    {
        return channelType;
    }
    public void setChannelType(String channelType)
    {
        this.channelType = channelType;
    }
    public String getUid()
    {
        return uid;
    }
    public void setUid(String uid)
    {
        this.uid = uid;
    }
}
