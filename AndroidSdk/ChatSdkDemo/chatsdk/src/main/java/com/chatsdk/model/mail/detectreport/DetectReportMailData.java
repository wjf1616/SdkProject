package com.chatsdk.model.mail.detectreport;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;

public class DetectReportMailData extends MailData
{
	private DetectReportMailContents	detail;

	public DetectReportMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(DetectReportMailContents detail)
	{
		this.detail = detail;
	}

	public void parseContents()
	{
		super.parseContents();
		if (!getContents().equals(""))
		{
			if(this.getItemIdFlag() == 1) {
				String temp[] = getContents().split("\\|");
				if(temp.length > 1){
					nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_114102);
					contentText = LanguageManager.getLangByKey(temp[0],temp[1]);
					detail = new DetectReportMailContents();
					detail.setContents(getContents());
					hasMailOpend = true;
				}
				if (getContents().equals("114005")) {
					nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_114102);
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_114005);
					detail = new DetectReportMailContents();
					detail.setContents(getContents());
					hasMailOpend = true;
				}
			}else
			{
				try
				{
					detail = JSON.parseObject(getContents(), DetectReportMailContents.class);
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
					UserInfoParams user = detail.getUser();
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
					if (detail.getDefence() != null)
					{
						defenceNum = detail.getDefence().getTotal();
					}
					String defenceInfo = MathUtil.getRoundFormatNumber(defenceNum);

					int resourceNum = 0;
					if (detail.getResource() != null)
					{
//						resourceNum = (int) (detail.getResource().getFood() + detail.getResource().getFood_not_collected()
//								+ detail.getResource().getWood() + detail.getResource().getWood_not_collected()
//								+ detail.getResource().getIron() + detail.getResource().getIron_not_collected()
//								+ detail.getResource().getStone() + detail.getResource().getStone_not_collected());

						resourceNum = (int)(detail.getResource().getFood_plunder() + detail.getResource().getIron_plunder()
								+ detail.getResource().getWood_plunder() + detail.getResource().getStone_plunder());
					}
					String resourInfo = MathUtil.getRoundFormatNumber(resourceNum);

					int helperNum = -1;

					if (detail.getRein_about_detail() != null)
					{
						helperNum = 0;
						if (detail.getRein_about() == null)
						{
							List<List<ReinAboutDetailParams>> rein_about_detailList = detail.getRein_about_detail();
							for (int i = 0; i < rein_about_detailList.size(); i++)
							{
								List<ReinAboutDetailParams> reinAboutDetailParamsList = rein_about_detailList.get(i);
								if (reinAboutDetailParamsList != null && reinAboutDetailParamsList.size() > 0)
								{
									for (int j = 0; j < reinAboutDetailParamsList.size(); j++)
									{
										ReinAboutDetailParams params = reinAboutDetailParamsList.get(j);
										if (params != null)
										{
											helperNum += params.getCount();
										}
									}
								}
							}
						}
					}
					else
					{
						if (detail.getRein_about() != null)
						{
							helperNum = detail.getRein_about().getTotal();
						}
					}


					String helperInfo = MathUtil.getRoundFormatNumber(helperNum);
					if (helperNum < 0)
						helperInfo = "NA";
					if (defenceNum < 0)
						defenceInfo = "NA";

					if(detail.getIsBattlefieldServer()==1)
					{
						isArenagameMail=true;
					}

					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_132111, defenceInfo, resourInfo, helperInfo);

					if (contentText.length() > 50)
					{
						contentText = contentText.substring(0, 50);
						contentText = contentText + "...";
					}

					if (StringUtils.isNotEmpty(nameStr))
						contentText = nameStr + "\n" + contentText;
				}
				catch (Exception e)
				{
					LogUtil.trackMessage("[DetectReportMailContents parseContents error]: contents:" + getContents());
				}
			}

		}

	}
}
