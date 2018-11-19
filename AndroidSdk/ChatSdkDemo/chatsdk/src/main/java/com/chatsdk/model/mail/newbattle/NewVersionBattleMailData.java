package com.chatsdk.model.mail.newbattle;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailIconName;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.battle.ArmyParams;
import com.chatsdk.model.mail.battle.Content;
import com.chatsdk.model.mail.battle.TowerKillParams;
import com.chatsdk.model.mail.battle.UserParams;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.chatsdk.model.MailManager.MAIL_SC_FINGHT_CITY;
import static com.chatsdk.model.MailManager.MAIL_SC_FINGHT_RUINS;

public class NewVersionBattleMailData extends MailData
{
	private NewVersionBattleMailContents	detail;
	private Content attualContent;
	private List<NewVersionBattleMailContents>	knight;
	private int							unread;
	private int							totalNum;

	private static final int			WIN		= 0;
	private static final int			DRAW	= 1;
	private static final int			LOOSE	= 2;

	public NewVersionBattleMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(NewVersionBattleMailContents detail)
	{
		this.detail = detail;
	}

	public Content getAttualContent()
	{
		return attualContent;
	}

	public void setAttualContent(Content attualContent)
	{
		this.attualContent = attualContent;
	}

	public List<NewVersionBattleMailContents> getKnight()
	{
		return knight;
	}

	public void setKnight(List<NewVersionBattleMailContents> knight)
	{
		this.knight = knight;
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
		isKnightMail = false;
		isKnightActivityFinishMail = false;
		if (!getContents().equals(""))
		{
			try
			{
				detail = JSON.parseObject(getContents(), NewVersionBattleMailContents.class);

				if (detail == null)
					return;
				this.isFreeAll = detail.getIsFreeAll();
				this.issamplereport = detail.getIssamplereport();
				this.catchLeader= detail.getHeroState();
				this.captureHeroLevel= detail.getCaptureHeroLevel();
				this.captureHeroId= detail.getCaptureHeroId();
				attualContent = new Content();
				if (detail.getWarPoint() != null)
					attualContent.setWarPoint(detail.getWarPoint());
				else
					attualContent.setWarPoint("");
				if (detail.getDefUser() != null && detail.getDefUser().getNpcId() != null && !detail.getDefUser().getNpcId().equals(""))
				{
					attualContent.setNpcId(detail.getDefUser().getNpcId());
					attualContent.setDefName("");
				}
				else
				{
					if (detail.getDefUser() != null && detail.getDefUser().getName() != null)
						attualContent.setDefName(detail.getDefUser().getName());
					else
						attualContent.setDefName("");
					attualContent.setNpcId("");
				}
				if (detail.getAtkUser() != null && detail.getAtkUser().getName() != null)
					attualContent.setAtkName(detail.getAtkUser().getName());
				else
					attualContent.setAtkName("");

				if (detail.getWinner() == null || detail.getWinner().equals(""))
				{
					attualContent.setWin(2);
				}
				else if (detail.getWinner().equals(UserManager.getInstance().getCurrentUserId()))
				{
					attualContent.setWin(0);
				}
				else
				{
					attualContent.setWin(1);
				}

				int battleType = detail.getBattleType();
				hasMailOpend = true;
				hasParseForKnight = true;

				if (detail.getMsReport() == 1)
					isKnightActivityFinishMail = true;
				if (battleType == 6)
					isKnightMail = true;

				if (battleType == 10)
				{
					isShaMogameMail = true;
				}


				if (detail.getBattleMailType()==9)
				{
					isBattlegameMail=true;
				}

				if (detail.getIsBattlefieldServer()==1)
				{
					isArenagameMail=true;
				}

				if (battleType != 6 && needParseByForce)
					return;

				String kill = "";
				String dead = "";
				String loss = "";

				boolean isbigLoss = false;
				if (detail.getBigLose()==true)
				{
					isbigLoss = true;
				}
				else
				{
					String killAndLoss = calculateKillandLoss(detail);
					if (StringUtils.isNotEmpty(killAndLoss))
					{
						String[] strArr = killAndLoss.split("_");
						if (strArr.length >= 3)
						{
							kill = strArr[0];
							dead = strArr[1];
							loss = strArr[2];
							if (StringUtils.isNumeric(kill))
								kill = MathUtil.getFormatNumber(Integer.parseInt(kill));
							if (StringUtils.isNumeric(loss))
								loss = MathUtil.getFormatNumber(Integer.parseInt(loss));
						}
					}
				}
				String des = "";
				String str = "";
				String playerUid = UserManager.getInstance().getCurrentUserId();
				int battleMailType = detail.getBattleMailType();

				boolean isAtt = false;
				boolean isHelp = false;
				String attUid = "";
				String attAbbr = "";
				int addServerId = -1;
				if (detail.getAtkUser() != null && StringUtils.isNotEmpty(detail.getAtkUser().getUid()))
					attUid = detail.getAtkUser().getUid();
				String attName = "";
				if (detail.getAtkUser() != null){
					if(StringUtils.isNotEmpty(detail.getAtkUser().getName())){
						attName = detail.getAtkUser().getName();
					}
					if(StringUtils.isNotEmpty(detail.getAtkUser().getAlliance())){
						attAbbr = detail.getAtkUser().getAlliance();
					}
					if(detail.getAtkUser().GetCrossFightSrcServerId()!=null&&detail.getAtkUser().getServerId()!=null)
						addServerId = Integer.parseInt(detail.getAtkUser().GetCrossFightSrcServerId()) > 0 ?
								Integer.parseInt(detail.getAtkUser().GetCrossFightSrcServerId()):Integer.parseInt(detail.getAtkUser().getServerId());
				}

				if(detail.getAtkUser() != null && StringUtils.isNotEmpty(detail.getAtkUser().getAttType()) && detail.getAtkUser().getAttType().equals("1"))
				    attName = LanguageManager.getLangByKey(attName);
				if(UserManager.getInstance().getCurrentUser()!=null)
					attName = UserManager.getInstance().getCurrentUser().getNameWithServerId(attAbbr,attName,addServerId);
				String defName = "";
                String defAbbr = "";
				if (detail.getDefUser() != null){
					if(StringUtils.isNotEmpty(detail.getAtkUser().getName())){
						defName = detail.getDefUser().getName();
					}
					if(StringUtils.isNotEmpty(detail.getDefUser().getAlliance())){
						defAbbr = detail.getDefUser().getAlliance();
					}
					if(detail.getDefUser().GetCrossFightSrcServerId()!=null&&detail.getDefUser().getServerId()!=null)
					addServerId = Integer.parseInt(detail.getDefUser().GetCrossFightSrcServerId()) > 0 ?
							Integer.parseInt(detail.getDefUser().GetCrossFightSrcServerId()):Integer.parseInt(detail.getDefUser().getServerId());
				}

				if(detail.getDefUser() != null && StringUtils.isNotEmpty(detail.getDefUser().getDefType()) && detail.getDefUser().getDefType().equals("1"))
					defName = LanguageManager.getLangByKey(defName);

				if(battleMailType == 7)
				{
					defName = LanguageManager.getLangByKey(defName);
				}
				if (detail.getDefUser() != null && detail.getDefUser().getNpcId() != null && !detail.getDefUser().getNpcId().equals(""))
				{
					defName = JniController.getInstance().excuteJNIMethod("getNPCNameById", new Object[] { detail.getDefUser().getNpcId() });
					if(defName==null||defName=="")
					{
						defName="";
					}
					else
					{
						addServerId=-1;
					}

				}
				if(UserManager.getInstance().getCurrentUser()!=null)
					defName = UserManager.getInstance().getCurrentUser().getNameWithServerId(defAbbr,defName,addServerId);
				if (detail.getAtkHelper() != null)
				{
					for (int i = 0; i < detail.getAtkHelper().size(); i++)
					{
						String helpUid = detail.getAtkHelper().get(i);
						if (StringUtils.isNotEmpty(helpUid) && helpUid.equals(playerUid))
						{
							isAtt = true;
							isHelp = true;
							break;
						}
					}
				}

				if (detail.getDefHelper() != null)
				{
					for (int i = 0; i < detail.getDefHelper().size(); i++)
					{
						String helpUid = detail.getDefHelper().get(i);
						if (StringUtils.isNotEmpty(helpUid) && helpUid.equals(playerUid))
						{
                            isAtt = false;
							isHelp = true;
							break;
						}
					}
				}

				if (StringUtils.isNotEmpty(attUid) && playerUid.equals(attUid))
				{
					isAtt = true;
				}

				int reportState;
				if (StringUtils.isEmpty(detail.getWinner()))
				{
					reportState = DRAW;
				}
				else if (detail.getWinner().equals(attUid))
				{
					if (isAtt)
					{
						reportState = WIN;
					}
					else
					{
						reportState = LOOSE;
					}
				}
				else
				{
					if (isAtt)
					{
						reportState = LOOSE;
					}
					else
					{
						reportState = WIN;
					}
				}

				if (isAtt)
				{
					if(isHelp){
						str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERARMY,attName);
					}else{
						str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);
					}
					des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK,str, defName);

				}
				else
				{
					if (battleMailType != 1)
					{
                        if(isHelp){
                            str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERARMY,defName);
                        }else{
                            str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);
                        }
						des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, attName,
								str);
					}
					else
					{
                        str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYCASTLE);
                        if(isHelp){
                            str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERCASTLE,defName);
                        }
						des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, attName,
								str);
					}
				}

				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT);
				if(getType() == MAIL_SC_FINGHT_RUINS || getType() == MAIL_SC_FINGHT_CITY){
				if(reportState ==0 ){
					if(isAtt){
						nameText = LanguageManager.getLangByKey("174544"); //174544=攻击分城胜利
					}else{
						nameText = LanguageManager.getLangByKey("174546"); //174546=防守分城胜利
					}
				}else{
					if(isAtt){
						nameText = LanguageManager.getLangByKey("174545"); //174545=攻击分城失败
					}else{
						nameText = LanguageManager.getLangByKey("174547"); //174547=防守分城失败
					}
				}
			}
				int pointType = detail.getPointType();

				int warServerType= detail.getWarServerType();

				if (reportState == WIN)   //战斗胜利
				{
					if (battleType == 6)//黑骑士
					{
						isKnightMail = true;
						knight = new ArrayList<NewVersionBattleMailContents>();
						detail.setUid(getUid());
						detail.setType(getType());
						long time = ((long) getCreateTime()) * 1000;
						detail.setCreateTime("" + time);
						knight.add(detail);
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105579);
					}
					else if (detail.getMsReport() == 1)
					{
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_133083);
					}
					else
					{
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105869, kill, loss);
						switch (battleMailType)
						{
							case 1:
								if (isAtt)		//攻城胜利
								{
									mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_VICTORY);
								}
								else			//守城胜利
								{
									mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_DEFENT_VICTORY);
								}
								break;
							case 2:				//资源战胜利
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CAPTURE);
								break;
							case 3:				//营地战胜利
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CAMP);
								break;
							case 4:				//联盟中心战胜利
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_ALLIANCE_CENTER);
								break;
							case 5:				//前哨战胜利
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_ALLIANCE_OUTPOST);
								break;
							case 6:				//总统站胜利
							{
								if (isAtt) {
									if (pointType == 10) {
										if (warServerType == 3) {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_171851);
										} else {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105700);
										}
									} else if (pointType == 12) {
										nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105704);
									}
								} else {
									if (pointType == 10) {
										if (warServerType == 3) {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_171853);
										} else {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105702);
										}
									} else if (pointType == 12) {
										nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105706);
									}
								}
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_PRESSIDENT);
							}
								break;
							case 12:				//总统站胜利
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_FUBAO_WIN);
								break;
							default:
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_VICTORY);
								break;
						}
					}

				}
				else		//战斗失败
				{
					if (battleType == 6) //黑骑士
					{
						isKnightMail = true;
						knight = new ArrayList<NewVersionBattleMailContents>();
						detail.setUid(getUid());
						detail.setType(getType());
						long time = ((long) getCreateTime()) * 1000;
						detail.setCreateTime("" + time);
						knight.add(detail);
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105580);
					}
					else if (detail.getMsReport() == 1)
					{
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_133083);
					}
					else
					{
						if (isbigLoss)
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105535);
							//105535=由于您的实力与敌人差距过大，您的部队被全部消灭，战斗结果没有被送回来！
						}
						else
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105869, kill, loss);
						}

						switch (battleMailType)
						{
							case 1:
								if (isAtt)			//攻城失败
								{
									mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_FAILURE);
								}
								else				//守城失败
								{
									mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_DEFENT_FAILURE);
								}
								break;
							case 2:					//资源战失败
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CAPTURE);
								break;
							case 3:					//营地战失败
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CAMP);
								break;
							case 4:					//联盟中心战失败
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_ALLIANCE_CENTER);
								break;
							case 5:					//前哨战失败
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_ALLIANCE_OUTPOST);
								break;
							case 6:					//总统站失败
							{
								if (isAtt) {
									if (pointType == 10) {
										if (warServerType == 3) {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_171852);
										} else {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105701);
										}
									} else if (pointType == 12) {
										nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105705);
									}
								} else {
									if (pointType == 10) {
										if (warServerType == 3) {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_171854);
										} else {
											nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105703);
										}
									} else if (pointType == 12) {
										nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105707);
									}
								}
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_PRESSIDENT);
							}
								break;
							case 12:					//总统站失败
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_FUBAO_FAIL);
								break;
							default:
								mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_FAILURE);
								break;
						}
					}
				}

				if (contentText.length() > 50)
				{
					contentText = contentText.substring(0, 50);
					contentText = contentText + "...";
				}

				if(battleType != 6 && detail.getMsReport() != 1)
				{
					if (StringUtils.isNotEmpty(des))
						contentText = des + "\n" + contentText;
				}

			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[BattleMailData parseContents error]: contents:" + getContents());
			}
		}
	}

	private String calculateKillandLoss(NewVersionBattleMailContents detail)
	{
		boolean isAtt = false;
		String attUid = "";
		if (detail.getAtkUser() != null && detail.getAtkUser().getUid() != null)
			attUid = detail.getAtkUser().getUid();
		String playerUid = UserManager.getInstance().getCurrentUserId();
		if (StringUtils.isNotEmpty(playerUid) && playerUid.equals(attUid))
		{
			isAtt = true;
		}
		else
		{
			if (detail.getAtkHelper() != null && detail.getAtkHelper().size() > 0)
			{
				for (int i = 0; i < detail.getAtkHelper().size(); i++)
				{
					String helpUid = detail.getAtkHelper().get(i);
					if (StringUtils.isNotEmpty(helpUid) && helpUid.equals(playerUid))
					{
						isAtt = true;
						break;
					}
				}
			}
		}

		UserParams attUserInfo = null;
		if (isAtt)
		{
			attUserInfo = detail.getAtkUser();
		}
		else
		{
			attUserInfo = detail.getDefUser();
		}

		String npc = "";
		if (attUserInfo != null && StringUtils.isNotEmpty(attUserInfo.getNpcId()))
			npc = attUserInfo.getNpcId();
		int dead = 0;
		int num = 0;
		int hurt = 0;
		int kill = 0;
		int rescue = 0;
		int total = 0;
		int loss = 0;

		if (StringUtils.isNotEmpty(npc))
		{
			if (detail.getDefReport() != null)
			{
				int count = detail.getDefReport().size();
				for (int i = 0; i < count; i++)
				{
					ArmyParams army = detail.getDefReport().get(i);
					if (army != null)
					{
						dead += army.getDead();
						num += army.getNum();
						hurt += army.getHurt();
						kill += army.getKill();
						rescue += army.getRescue();
					}
				}
			}
			loss = dead + hurt +rescue;
			total = dead + num + hurt +rescue;
			if (total <= 0)
				total = 1;
		}
		else
		{
			if (isAtt)
			{
				if (detail.getAtkArmyTotal() != null)
				{
					kill += detail.getAtkArmyTotal().getKill();
					dead += detail.getAtkArmyTotal().getDead();
					hurt += detail.getAtkArmyTotal().getHurt();
					num += detail.getAtkArmyTotal().getNum();
					rescue += detail.getAtkArmyTotal().getRescue();
				}
				loss = dead + hurt +rescue;

				List<Integer> genKillArr = detail.getAtkGenKill();
				if (genKillArr != null)
				{
					for (int i = 0; i < genKillArr.size(); i++)
					{
						kill += genKillArr.get(i).intValue();
					}
				}
			}
			else
			{
				if (detail.getDefArmyTotal() != null)
				{
					kill += detail.getDefArmyTotal().getKill();
					dead += detail.getDefArmyTotal().getDead();
					hurt += detail.getDefArmyTotal().getHurt();
					num += detail.getDefArmyTotal().getNum();
					rescue += detail.getDefArmyTotal().getRescue();
				}
				loss = dead + hurt +rescue;

				List<Integer> genKillArr = detail.getDefGenKill();
				if (genKillArr != null)
				{
					for (int i = 0; i < genKillArr.size(); i++)
					{
						kill += genKillArr.get(i).intValue();
					}
				}
				if (detail.getDefTowerKill() != null)
				{

					for (int i = 0; i < detail.getDefTowerKill().size(); i++)
					{
						TowerKillParams toweKill = detail.getDefTowerKill().get(i);
						if (toweKill != null)
							kill += toweKill.getKill();
					}
				}
				if (detail.getDefFortLost() != null)
				{
					for (int i = 0; i < detail.getDefFortLost().size(); i++)
					{
						ArmyParams armyParm = detail.getDefFortLost().get(i);
						if (armyParm != null)
						{
							kill += armyParm.getKill();
						}
					}
				}
			}
		}
		String ret = kill + "_" + dead + "_" + loss;
		return ret;
	}
}
