package com.chatsdk.model.mail.mobilization;

import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/11/24.
 */

public class MobilizationMailData extends MailData {
    private MobilizationMailContents detail;
    private List<MobilizationMailContents> mobilizations;
    private int							unread;
    private int							totalNum;

    public MobilizationMailContents getDetail() {
        return detail;
    }

    public void setDetail(MobilizationMailContents detail) {
        this.detail = detail;
    }

    public List<MobilizationMailContents> getMobilizations() {
        return mobilizations;
    }

    public void setMobilizations(List<MobilizationMailContents> mobilizations) {
        this.mobilizations = mobilizations;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public void parseContents()
    {
        super.parseContents();
        if (!getContents().equals(""))
        {
            try
            {
                if (getStatus() == 0)
                    setUnread(1);
                else
                    setUnread(0);
                setTotalNum(1);
                mobilizations = new ArrayList<MobilizationMailContents>();
                MobilizationMailContents detail = new MobilizationMailContents();
                if (detail == null)
                    return;
                detail.setMailID(getUid());
                long time = ((long) getCreateTime()) * 1000;
                detail.setCreateTime("" + time);
                detail.setType(getType());
                detail.setContents(getContents());
                detail.setRewardId(getRewardId());
                detail.setTitle(getTitle());

                mobilizations.add(detail);
                hasMailOpend = true;
                if (detail == null || needParseByForce)
                    return;
                if (contentText.length() > 50)
                {
                    contentText = contentText.substring(0, 50);
                    contentText = contentText + "...";
                }
            }
            catch (Exception e)
            {
                LogUtil.trackMessage("[GiftMailData parseContents error]: contents:" + getContents());
            }
        }
    }
}
