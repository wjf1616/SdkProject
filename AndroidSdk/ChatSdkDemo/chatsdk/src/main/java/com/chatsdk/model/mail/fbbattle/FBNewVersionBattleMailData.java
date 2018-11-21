package com.chatsdk.model.mail.fbbattle;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailIconName;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.battle.Content;
import com.chatsdk.model.mail.battle.UserParams;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;

import com.chatsdk.controller.ChatServiceController;




import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.chatsdk.model.MailManager.Mail_CITY_FIGHT_FB;
import static com.chatsdk.model.MailManager.Mail_MANOR_FIGHT_FB;

public class FBNewVersionBattleMailData extends MailData
{
	private FBNewVersionBattleMailContents	detail;
	private Content attualContent;
	private List<FBNewVersionBattleMailContents>	knight;
	private int							unread;
	private int							totalNum;

	private static final int			WIN		= 0;
	private static final int			DRAW	= 1;
	private static final int			LOOSE	= 2;

	public FBNewVersionBattleMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(FBNewVersionBattleMailContents detail)
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

	public List<FBNewVersionBattleMailContents> getKnight()
	{
		return knight;
	}

	public void setKnight(List<FBNewVersionBattleMailContents> knight)
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
				detail = JSON.parseObject(getContents(), FBNewVersionBattleMailContents.class);

				if (detail == null)
					return;
				this.isFreeAll = detail.getIsFreeAll();
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
				if (StringUtils.isNotEmpty(detail.getNewBigLose()))
				{
					if(detail.getNewBigLose().equals("1")){

						isbigLoss = true;
					}else{
						isbigLoss = false;
					}
				}else if(detail.getBigLose()==true){
					isbigLoss = true;
				}
				if(!isbigLoss)
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
				boolean isatkHelp = false;
				boolean isdefHelp = false;
				boolean isOwner = false;

				int addServerId = -1;

				String attAbbr = "";
				String attUid = detail.getFirstAtkUid();
				String attName = "";


				if (detail.getAtkPlayerInfo() != null && detail.getAtkPlayerInfo().size() > 0)
				{
					for (int i = 0; i < detail.getAtkPlayerInfo().size(); i++)
					{
						UserParams helpUid = detail.getAtkPlayerInfo().get(i);
						if (helpUid.getUid().equals(attUid))
						{

							if(StringUtils.isNotEmpty(helpUid.getName())){
								attName = helpUid.getName();
							}
							if(StringUtils.isNotEmpty(helpUid.getAlAbbr())){
								attAbbr = helpUid.getAlAbbr();
							}
							if(helpUid.GetCrossFightSrcServerId()!=null&&helpUid.getServerId()!=null)
								addServerId = Integer.parseInt(helpUid.GetCrossFightSrcServerId()) > 0 ?
										Integer.parseInt(helpUid.GetCrossFightSrcServerId()):Integer.parseInt(helpUid.getServerId());

							if( StringUtils.isNotEmpty(helpUid.getAttType()) && helpUid.getAttType().equals("1"))
								attName = LanguageManager.getLangByKey(attName);
						}
					}
				}

				if(UserManager.getInstance().getCurrentUser()!=null)
					attName = UserManager.getInstance().getCurrentUser().getNameWithServerId(attAbbr,attName,addServerId);



				String defUid = detail.getFirstDefUid();
				String defName = "";
                String defAbbr = "";

				if (detail.getDefPlayerInfo() != null && detail.getDefPlayerInfo().size() > 0)
				{
					for (int i = 0; i < detail.getDefPlayerInfo().size(); i++)
					{
						UserParams helpUid = detail.getDefPlayerInfo().get(i);
						if (helpUid.getUid().equals(defUid))
						{

							if(StringUtils.isNotEmpty(helpUid.getName())){
								defName = helpUid.getName();
							}
							if(StringUtils.isNotEmpty(helpUid.getAlAbbr())){
								defAbbr = helpUid.getAlAbbr();
							}
							if(helpUid.GetCrossFightSrcServerId()!=null&&helpUid.getServerId()!=null)
								addServerId = Integer.parseInt(helpUid.GetCrossFightSrcServerId()) > 0 ?
										Integer.parseInt(helpUid.GetCrossFightSrcServerId()):Integer.parseInt(helpUid.getServerId());

							if( StringUtils.isNotEmpty(helpUid.getDefType()) && helpUid.getDefType().equals("1"))
								defName = LanguageManager.getLangByKey(attName);
						}
					}
				}

				if(UserManager.getInstance().getCurrentUser()!=null)
					defName = UserManager.getInstance().getCurrentUser().getNameWithServerId(defAbbr,defName,addServerId);

				if (attUid.equals("")==false)
				{
					if (playerUid.equals(attUid))
					{
						isAtt = true;
						isOwner = true;
						if (detail.getAtkPlayerInfo() != null && detail.getAtkPlayerInfo().size() > 1)
						{
							isdefHelp = true;
						}

					}
					else if (detail.getAtkPlayerInfo() != null && detail.getAtkPlayerInfo().size() > 0)
					{
						for (int i = 0; i < detail.getAtkPlayerInfo().size(); i++)
						{
							UserParams helpUid = detail.getAtkPlayerInfo().get(i);
							if (helpUid.getUid().equals(playerUid))
							{
								isatkHelp = true;
								isAtt = true;
								break;
							}
						}
					}
				}

				if(!isAtt)
				{

					if (playerUid.equals(defUid))
					{
						isOwner = true;
						if (detail.getDefPlayerInfo() != null && detail.getDefPlayerInfo().size() > 1)
						{
							isdefHelp = true;
						}
					}
					else
					{
						if (detail.getDefPlayerInfo() != null && detail.getDefPlayerInfo().size() > 1)
						{
							isdefHelp = true;
						}
					}

				}
				else
				{
					if (detail.getDefPlayerInfo() != null && detail.getDefPlayerInfo().size() > 1)
					{
						isdefHelp = true;
					}
				}
				
				int reportState;

				 if (detail.getBattleResult()==0)
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

				if(detail.getDurableChangeInfo() != null && StringUtils.isNotEmpty(detail.getDurableChangeInfo().getConfigId()) && !isAtt){
					if(detail.getDurableChangeInfo().getType() == 48){
						String name = JniController.getInstance().excuteJNIMethod("getPropByIdType", new Object[] { detail.getDurableChangeInfo().getConfigId(),"name","",34 });
						defName = LanguageManager.getLangByKey(name);
					}else if(detail.getDurableChangeInfo().getType() == 49){
						String name = JniController.getInstance().excuteJNIMethod("getPropByIdType", new Object[] { detail.getDurableChangeInfo().getConfigId(),"name","",261 });
						defName = LanguageManager.getLangByKey(name);
					}else if (detail.getDurableChangeInfo().getType() == 53){
						defName = LanguageManager.getLangByKey("90600217");
					}
					des = LanguageManager.getLangByKey("105547",attName,LanguageManager.getLangByKey("90800302",defName));
				}
				else if (detail.getFirstDefUid().isEmpty()&&(getType()==MailManager.Mail_ATTACKMONSTER_FIGHT_FB||getType()== Mail_MANOR_FIGHT_FB || getType() == MailManager.Mail_CONTRAST_WORLD_BOSS))
				{

					str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);
					String name = "";
					String level = "";
					if(!detail.getFirstDefNpcId().isEmpty())
					{
						if (getType()==MailManager.Mail_ATTACKMONSTER_FIGHT_FB || getType()==MailManager.Mail_CONTRAST_WORLD_BOSS)
						{
							name = JniController.getInstance().excuteJNIMethod("getPropByIdType", new Object[] { detail.getFirstDefNpcId(),"name","field_monster",273 });
							name = LanguageManager.getLangByKey(name);
							level = JniController.getInstance().excuteJNIMethod("getPropByIdType", new Object[] { detail.getFirstDefNpcId(), "level","field_monster",273 });

							if (!level.isEmpty())
							{
								name =name + " Lv." +level;
							}
						}
						else
						{
							name = JniController.getInstance().excuteJNIMethod("getNPCNameById", new Object[] { detail.getFirstDefNpcId() });
						}

					}

					des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK,str, name);

				}
				else if(getType() ==MailManager.MAIL_HEROBATTLE_FB)
				{
					des ="";
				}
				else if(getType() ==Mail_CITY_FIGHT_FB&&this.detail.getNpcType()== 2)
				{
					String mosnterId_atk="";
					String atk_npc_str="";
					boolean isAtkNPC=false;
					if(this.detail.getFirstAtkUid().equals("")&&!this.detail.getFirstAtkNpcId().equals(""))
					{
						mosnterId_atk=this.detail.getFirstAtkNpcId();
						isAtkNPC=true;
					}

					String mosnterId_def="";
					if(this.detail.getFirstDefUid().equals(""))
					{
						mosnterId_def=this.detail.getFirstDefNpcId();
					}
					if (isAtkNPC)
					{

						List<FBNewVersionNpcInfoParams> infos =this.detail.getAtkNpcInfo();
						if (infos != null && infos.size() > 0) {

							for (int i=0;i<infos.size();i++)
							{
								FBNewVersionNpcInfoParams dic = (FBNewVersionNpcInfoParams) infos.get(i);
								if (dic != null&&dic.getNpcId() ==mosnterId_atk)
								{
									atk_npc_str = LanguageManager.getLangByKey("89000363", LanguageManager.getLangByKey(dic.getNpcName()));
									break;
								}
							}

						}

						str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);//102187=我的部队
						des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, atk_npc_str, str);
					}
					else
					{
						String def_npc_str="";
						List<FBNewVersionNpcInfoParams> infos_def =this.detail.getDefNpcInfo();
						if (infos_def != null && infos_def.size() > 0)
						{
							for (int j=0;j<infos_def.size();j++)
							{
								FBNewVersionNpcInfoParams dic = (FBNewVersionNpcInfoParams) infos_def.get(j);
								if (dic != null&&dic.getNpcId() ==mosnterId_def)
								{
									def_npc_str = LanguageManager.getLangByKey("89000363", LanguageManager.getLangByKey(dic.getNpcName()));
									break;
								}
							}
						}
						str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);//102187=我的部队
						des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, str, def_npc_str);
					}
				}
				else if (isAtt)
				{
					if(isatkHelp){
						if (isOwner)
						{
							str = LanguageManager.getLangByKey("172205");//我的队伍
						}
						else
						{
							str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERARMY,attName);
						}

						if (isdefHelp)
						{
							defName = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERARMY,defName);
						}
					}else{
						str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);
					}
					des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK,str, defName);

				}
				else
				{
					if (battleMailType != 1)
					{
                        if(isdefHelp){
							if (isOwner)
							{
								str = LanguageManager.getLangByKey("172205");//我的队伍
							}else
							{
								str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERARMY,defName);
							}
							if (isatkHelp)
							{
								attName = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERARMY,attName);
							}
                        }else{
                            str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);
                        }
						des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, attName,
								str);
					}
					else
					{
                        str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYCASTLE);
                        if(isdefHelp){
                            str = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_OTHERCASTLE,defName);
                        }
						des = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, attName,
								str);
					}
				}

				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT);
				if(isAtt){
					nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT_ATK);
				}else{
					nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT_DEF);
				}

				if(getType() == Mail_MANOR_FIGHT_FB){
					if(reportState == WIN){
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105117);
					}else if(reportState == LOOSE){
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105118);
					}
				}else if(getType() == MailManager.Mail_CONTRAST_WORLD_BOSS){
					nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_90400086);
				}

				int pointType = detail.getPointType();

				int warServerType= detail.getWarServerType();

				if (getType()==MailManager.Mail_ATTACKMONSTER_FIGHT_FB || getType() == MailManager.Mail_CONTRAST_WORLD_BOSS)
				{

					if(reportState == WIN)
					{
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MONSTER);
					}
					else
					{
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MONSTER);
					}
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105869, kill, loss);
				}
				else if(getType() ==MailManager.MAIL_HEROBATTLE_FB)
				{
					nameText = LanguageManager.getLangByKey("83351285");
					contentText ="";
				}
				else if (reportState == WIN)   //战斗胜利
				{
					 if (detail.getMsReport() == 1)
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
						knight = new ArrayList<FBNewVersionBattleMailContents>();
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

				if(getType() ==MailManager.Mail_CITY_FIGHT_FB&&detail.getBattleResult()==0&&ChatServiceController.defence_select_switch&&detail.getDefCityFailHurt()!= null)
				{
					if (isAtt)
					{
						contentText = contentText+" "+LanguageManager.getLangByKey("83352106")+detail.getDefCityFailHurt();
					}
					else
					{
						contentText = contentText+" "+LanguageManager.getLangByKey("83352105")+detail.getDefCityFailHurt();
					}

				}

				else if (ChatServiceController.chat_v2_personal)


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

	private String calculateKillandLoss(FBNewVersionBattleMailContents detail)
	{
		boolean isAtt = false;
		String attUid = detail.getFirstAtkUid();

		String playerUid = UserManager.getInstance().getCurrentUserId();
		if (StringUtils.isNotEmpty(playerUid) && playerUid.equals(attUid))
		{
			isAtt = true;
		}
		else
		{
			if (detail.getAtkPlayerInfo() != null && detail.getAtkPlayerInfo().size() > 0)
			{
				for (int i = 0; i < detail.getAtkPlayerInfo().size(); i++)
				{
					UserParams helpUid = detail.getAtkPlayerInfo().get(i);
					if (helpUid.getUid().equals(playerUid))
					{
						isAtt = true;
						break;
					}
				}
			}
		}

		String dead ;
		String kill;
		String loss;

		if (isAtt)
		{
			kill=detail.getAtkKillTotal();
			dead=detail.getAtkDeadTotal();
			int a=Integer.parseInt(detail.getAtkDeadTotal())+ Integer.parseInt(detail.getAtkHurtTotal());
			loss=""+a;
		}
		else
		{
			kill=detail.getDefKillTotal();
			dead=detail.getDefDeadTotal();
			int a=Integer.parseInt(detail.getDefDeadTotal())+ Integer.parseInt(detail.getDefHurtTotal());
			loss=""+a;
		}

		String ret = kill + "_" + dead + "_" + loss;
		return ret;
	}
}
