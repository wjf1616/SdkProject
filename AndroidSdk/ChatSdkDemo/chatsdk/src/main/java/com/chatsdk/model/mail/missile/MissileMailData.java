package com.chatsdk.model.mail.missile;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.MailIconName;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzh on 16/9/7.
 */

public class MissileMailData extends MailData
{
    private MissileMailContents         detail;
    private List<MissileMailContents>   missile;
    private int							unread;
    private int							totalNum;


    public MissileMailContents getDetail()
    {
        return detail;
    }

    public void setDetail(MissileMailContents detail)
    {
        this.detail = detail;
    }



    public List<MissileMailContents> getMissile()
    {
        return missile;
    }

    public void setMissile(List<MissileMailContents> missile)
    {
        this.missile = missile;
    }

    public int getUnread()
    {
        return unread;
    }

    public void setUnread(int unread)
    {
        this.unread = unread;
    }

    public int getTotalNum()
    {
        return totalNum;
    }

    public void setTotalNum(int totalNum)
    {
        this.totalNum = totalNum;
    }

    public void parseContents()
    {
        super.parseContents();
        if (!getContents().equals(""))
        {
            try
            {
                detail = JSON.parseObject(getContents(), MissileMailContents.class);

                if (detail == null)
                    return;

                missile = new ArrayList<MissileMailContents>();
                detail.setUid(getUid());
                detail.setType(getType());
                long time = ((long) getCreateTime()) * 1000;
                detail.setCreateTime("" + time);

                mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_MISSILE);
//                nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
//                contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105579);

                missile.add(detail);
            }
            catch (Exception e)
            {
                LogUtil.trackMessage("[MissileMailData parseContents error]: contents:" + getContents());
            }
        }
    }

}
