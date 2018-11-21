package com.chatsdk.model.mail.fbscoutreport;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.detectreport.UserInfoParams;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;

public class FBDetectReportMailData extends MailData
{
	private FBDetectReportMailContents	detail;

	public FBDetectReportMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(FBDetectReportMailContents detail)
	{
		this.detail = detail;
	}

	public void parseContents()
	{
		super.parseContents();
		if (!getContents().equals(""))
		{
			{
				try
				{
					detail = JSON.parseObject(getContents(), FBDetectReportMailContents.class);
					hasMailOpend = true;

					if (detail == null || needParseByForce)
						return;

					switch (detail.getPointType())
					{
						case MailManager.Throne:
						{ // 王座
							if(detail.getWarServerType() == 3)
							{
								nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_SUPERCENTER);
							}
							else {
								nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_PALACE);
							}
							break;
						}
						case MailManager.Trebuchet:
						{// 投石机
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_CATAPULT);
							break;
						}
						case MailManager.Tile_allianceArea:
						{
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_CASTLE_SUCESS);
							break;
						}
						case MailManager.tile_banner:
						{
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_BANNER_SUCESS);
							break;
						}
						default:
						{
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_SUCESS);
							break;
						}
					}

					String nameStr = "";
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_SUCESS);
					UserInfoParams user = detail.getUserInfo();
					int addServerId = -1;
					if (user != null)
					{
						if (user.getNoDef() == 1)
						{

							String asn = user.getAbbr();
							String name = user.getName();
							addServerId = user.getDefUserServerId();

							if (StringUtils.isEmpty(name))
							{
								if (detail.getPointType() == MailManager.Tile_allianceArea)
								{
									if (user.getCount() > 0)
										nameStr += LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_CASTLE, "" + user.getCount());
									else
										nameStr += LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_CASTLE, "1");
								}
								else if (detail.getPointType() == MailManager.tile_banner)
								{
									nameStr += LanguageManager.getLangByKey(LanguageKeys.MAIL_BANNER);
								}
							}
							nameStr = UserManager.getInstance().getCurrentUser().getNameWithServerId(asn,name,addServerId);
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_INFO, nameStr);
						}
						else
						{
							nameStr = user.getName();
							if(detail.getPointType() == 31)
							{
								nameStr = LanguageManager.getLangByKey(nameStr);
							}
							String asn = user.getAbbr();
							addServerId = user.getDefUserServerId();

							nameStr = UserManager.getInstance().getCurrentUser().getNameWithServerId(asn,nameStr,addServerId);
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_DETECT_INFO, nameStr);
						}
					}

					int defenceNum = -1;
					if (detail.getUserArmy() != null)
					{
						defenceNum = detail.getUserArmy().getTotal();
					}
					String defenceInfo = MathUtil.getRoundFormatNumber(defenceNum);

					int resourceNum = 0;
					if (detail.getCityBaseInfo() != null)
					{

						resourceNum = (int)(detail.getCityBaseInfo().getFood_plunder() + detail.getCityBaseInfo().getIron_plunder()
								+ detail.getCityBaseInfo().getWood_plunder() + detail.getCityBaseInfo().getStone_plunder());
					}
					String resourInfo = MathUtil.getRoundFormatNumber(resourceNum);

					int helperNum = -1;

					if (detail.getMemberInfo() != null)
					{
						helperNum = detail.getMemberInfo().getTotal();
					}

					String helperInfo = MathUtil.getRoundFormatNumber(helperNum);
					if (helperNum < 0)
						helperInfo = "NA";
					if (defenceNum < 0)
						defenceInfo = "NA";

					contentText = contentText+ "\n" +LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_132111, defenceInfo, resourInfo, helperInfo);

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
}
