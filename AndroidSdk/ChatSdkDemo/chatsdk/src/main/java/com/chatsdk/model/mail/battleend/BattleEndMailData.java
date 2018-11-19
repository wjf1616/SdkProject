package com.chatsdk.model.mail.battleend;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailIconName;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.battle.Content;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangkankan on 2017/7/5.
 */

public class BattleEndMailData extends MailData
{
    private BattleEndMailContents	detail;

    public BattleEndMailContents getDetail()
    {
        return detail;
    }

    public void setDetail(BattleEndMailContents detail)
    {
        this.detail = detail;
    }

    public void parseContents()
    {
        super.parseContents();
        super.parseContents();
        if (!getContents().equals(""))
        {
            try
            {
                detail = JSON.parseObject(getContents(), BattleEndMailContents.class);
                hasMailOpend = true;

                if (detail == null || needParseByForce)
                    return;

                nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_FUBAO_SUCCESS);

                if (StringUtils.isNotEmpty(detail.getDestroyed()))
                {
                    contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_FUBAO_DIS_END_TWO);
                }
                else
                {
                    contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_FUBAO_DIS_END_ONE);
                }



                if (contentText.length() > 50)
                {
                    contentText = contentText.substring(0, 50);
                    contentText = contentText + "...";
                }
            }
            catch (Exception e)
            {
                LogUtil.trackMessage("[DetectReportMailContents parseContents error]: contents:" + getContents());
            }

        }
    }
}
