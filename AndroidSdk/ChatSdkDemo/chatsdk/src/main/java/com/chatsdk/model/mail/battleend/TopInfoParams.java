package com.chatsdk.model.mail.battleend;

/**
 * Created by zhangkankan on 2017/7/5.
 */

public class TopInfoParams {

    private String	name;
    private int		kill;
    private int		rank;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getKill()
    {
        return kill;
    }

    public void setKill(int kill)
    {
        this.kill = kill;
    }

    public int getRank()
    {
        return rank;
    }

    public void setRank(int rank)
    {
        this.rank = rank;
    }
}
