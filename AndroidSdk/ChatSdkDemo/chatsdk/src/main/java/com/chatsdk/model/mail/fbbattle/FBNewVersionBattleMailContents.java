package com.chatsdk.model.mail.fbbattle;

import com.chatsdk.model.mail.battle.AllianceParams;
import com.chatsdk.model.mail.battle.ArmyAlterLevelParams;
import com.chatsdk.model.mail.battle.ArmyParams;
import com.chatsdk.model.mail.battle.ArmyTotalParams;
import com.chatsdk.model.mail.battle.BuffParams;
import com.chatsdk.model.mail.battle.CkfWarInfoParams;
import com.chatsdk.model.mail.battle.GenParams;
import com.chatsdk.model.mail.battle.HelpReportParams;
import com.chatsdk.model.mail.battle.RemainResourceParams;
import com.chatsdk.model.mail.battle.RewardParams;
import com.chatsdk.model.mail.battle.TowerKillParams;
import com.chatsdk.model.mail.battle.UserParams;
import com.chatsdk.model.mail.battle.CaptureHeroParams;
import com.chatsdk.model.mail.battle.WorldFortressParams;
import com.chatsdk.model.mail.monster.BossRewardParams;
import com.chatsdk.model.mail.monster.RateRewardParams;

import java.util.List;

public class FBNewVersionBattleMailContents
{
	private boolean					isResourceShieldState;
	private int						atkPowerLost;
	private int						defPowerLost;
	private String					winner;
	private int						battleType;
	private int						isBattlefieldServer;//末日角斗场
	private int						battleMailType;
	private String					reportUid;
	private int						pointType;
	private int						warServerType;
	private String					warPoint;
	private int                     warServerId;

	private int                     protectSide;

	public boolean getBigLose() {
		return bigLose;
	}

	public void setBigLose(boolean bigLose) {
		this.bigLose = bigLose;
	}

	private boolean                     bigLose;

	private ArmyTotalParams atkArmyTotal;
	private ArmyTotalParams			defArmyTotal;
	private UserParams				defUser;
	private UserParams atkUser;
	private AllianceParams			atkAlliance;
	private AllianceParams defAlliance;

	private List<HelpReportParams>	atkHelpReport;
	private List<HelpReportParams>	defHelpReport;
	private List<String>			atkHelper;
	private List<String>			defHelper;
	private List<GenParams>			defGen;
	private List<GenParams>			atkGen;

	private List<ArmyAlterLevelParams> defTroopTranList;
	private List<ArmyAlterLevelParams> attTroopTranList;

	private List<ArmyParams>		atkReport;
	private List<ArmyParams>		defReport;
	private List<String>			atkWarEffect;
	private List<String>			dfWarEffect;

	private List<WorldFortressParams>			atkWorldFortress;
	private List<WorldFortressParams>			defWorldFortress;


	private List<String>			defskills;
	private List<String>			attskills;

	public boolean isResourceShieldState() {
		return isResourceShieldState;
	}

	public void setResourceShieldState(boolean resourceShieldState) {
		isResourceShieldState = resourceShieldState;
	}



	private List<BuffParams>		attBuffList;
	private List<BuffParams>		defBuffList;
	private List<ArmyParams>		defFortLost;
	private List<RewardParams>		reward;
	private List<RateRewardParams>	knightReward;
	private List<TowerKillParams>	defTowerKill;
	private List<Integer>			atkGenKill;
	private List<Integer>			defGenKill;

	private int						userKill;
	private int						failTimes;
	private int						winPercent;
	private int						monsterLevel;
	private int						userScore;
	private int						userDesertExp;
	private int						allKill;
	private int						msReport;
	private int						killRound;
	private int						roundNum;
	private int						isDemoFight;
	private int						isFreeAll;
	private int						heroState;
	private int                     captureHeroId;
	private int                     captureHeroLevel;
	private int						defBeKilledCount;

	private boolean					defProtectActivate;
	private boolean					hideArmyDetail;
	private int						level;

	private int						ckf;
	private CkfWarInfoParams ckfWarInfo;
	private RemainResourceParams remainResource;
	private int						serverType;
	private String					uid;
	private int						type;
	private String					createTime;





	//------------------------------------------------------华丽的分割线------------------------------------------------------//

	private int battleResult; //战斗结果 0 攻击方胜利 1 防守方胜利 2 平局

	private String firstAtkUid; //进攻 第一个uid
	private String firstDefUid; //防守 第一个uid

	private String atkPowerLostTotal; //攻击 战力损失
	private String atkKillTotal; // 攻击 击杀数
	private String atkDeadTotal; //攻击 死亡数
	private String atkHurtTotal; // 攻击 受伤
	private String atkRemain; //攻击 剩余到少
	private String atkPoison; //攻击 剩余到少
    private String atkRecoverTotal;

	private String defPowerLostTotal; //攻击 战力损失
	private String defKillTotal; // 攻击 击杀数
	private String defDeadTotal; //攻击 死亡数
	private String defHurtTotal; // 攻击 受伤
	private String  defRemain; //攻击 剩余到少
	private String  defPoison; //攻击 剩余到少
    private String defRecoverTotal;

	private String  defCityFailHurt; //攻击 剩余到少

	//打怪邮件


	public String getDefCityFailHurt() {
		return defCityFailHurt;
	}

	public void setDefCityFailHurt(String defCityFailHurt) {
		this.defCityFailHurt = defCityFailHurt;
	}

	private String firstDefNpcId; //防守NPC uid
	private String firstAtkNpcId; //防守NPC uid
	private int npcType;

	public String getFirstDefNpcId() {
		return firstDefNpcId;
	}

	public void setFirstDefNpcId(String firstDefNpcId) {
		this.firstDefNpcId = firstDefNpcId;
	}

	public String getFirstAtkNpcId() {
		return firstAtkNpcId;
	}

	public void setFirstAtkNpcId(String firstAtkNpcId) {
		this.firstAtkNpcId = firstAtkNpcId;
	}

	public int getNpcType() {
		return npcType;
	}

	public void setNpcType(int npcType) {
		this.npcType = npcType;
	}

	private String rewardHeroExp;//击杀丧尸的经验


	/**
	 *   打普通丧尸军官精英详情
	 *  泛型为 FBHeroExpInfoParams
	 */

	private List<FBHeroExpInfoParams>	heroExpInfo;

	private  FBNewVersionDesertExpInfoParams desertExpInfo;

	public List<FBHeroExpInfoParams> getHeroExpInfo() {
		return heroExpInfo;
	}

	public void setHeroExpInfo(List<FBHeroExpInfoParams> heroExpInfo) {
		this.heroExpInfo = heroExpInfo;
	}

	public FBNewVersionDesertExpInfoParams getDesertExpInfo() {
		return desertExpInfo;
	}

	public void setDesertExpInfo(FBNewVersionDesertExpInfoParams desertExpInfo) {
		this.desertExpInfo = desertExpInfo;
	}

	/**

	 *   打普通丧尸或打地块邮件 奖励
	 *  泛型为 RateRewardParams（monster下）
	 */

	private List<RateRewardParams>	monsterReward;

	/**

	 *   打普通丧尸或打地块邮件 奖励
	 *  泛型为 RateRewardParams（monster下）
	 */

	private List<BossRewardParams>	monsterBossReward;

	/**
	 *  打普通丧尸（首杀奖励）
	 *  泛型为 RateRewardParams（monster下）
	 */
	private List<RateRewardParams>	firstKillReward;


	public String getRewardHeroExp() {
		return rewardHeroExp;
	}

	public void setRewardHeroExp(String rewardHeroExp) {
		this.rewardHeroExp = rewardHeroExp;
	}

	public List<RateRewardParams> getMonsterReward() {
		return monsterReward;
	}

	public void setMonsterReward(List<RateRewardParams> monsterReward) {
		this.monsterReward = monsterReward;
	}

	public List<BossRewardParams> getMonsterBossReward() {
		return monsterBossReward;
	}

	public void setMonsterBossReward(List<BossRewardParams> monsterBossReward) {
		this.monsterBossReward = monsterBossReward;
	}

	public List<RateRewardParams> getFirstKillReward() {
		return firstKillReward;
	}

	public void setFirstKillReward(List<RateRewardParams> firstKillReward) {
		this.firstKillReward = firstKillReward;
	}



	private int  plunderDomainPointId; //掠夺的地块的坐标
	private String domainId;//掠夺的地块配置信息

	private String fightId; ///战斗id  用来播战斗过程


	private List<RewardParams>	plunderResArray;


	private FBNewVersionPointInfoParams battlePointInfo;//战报发生 位置信息


	private List<UserParams> defPlayerInfo;


	private List<UserParams> atkPlayerInfo;
    
    private List<CaptureHeroParams> captureHero;

	public int getPlunderDomainPointId() {
		return plunderDomainPointId;
	}

	public void setPlunderDomainPointId(int plunderDomainPointId) {
		this.plunderDomainPointId = plunderDomainPointId;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public List<FBNewVersionNpcInfoParams> getAtkNpcInfo() {
		return atkNpcInfo;
	}

	public void setAtkNpcInfo(List<FBNewVersionNpcInfoParams> atkNpcInfo) {
		this.atkNpcInfo = atkNpcInfo;
	}

	public List<FBNewVersionNpcInfoParams> getDefNpcInfo() {
		return defNpcInfo;
	}

	public void setDefNpcInfo(List<FBNewVersionNpcInfoParams> defNpcInfo) {
		this.defNpcInfo = defNpcInfo;
	}

	private List<FBNewVersionNpcInfoParams> atkNpcInfo;


	private List<FBNewVersionNpcInfoParams> defNpcInfo;

	private FBNewVersoinDurableChangeInfo durableChangeInfo;






	public boolean isBigLose() {
		return bigLose;
	}

	public int getBattleResult() {
		return battleResult;
	}

	public void setBattleResult(int battleResult) {
		this.battleResult = battleResult;
	}

	public String getFirstAtkUid() {
		return firstAtkUid;
	}

	public void setFirstAtkUid(String firstAtkUid) {
		this.firstAtkUid = firstAtkUid;
	}

	public String getFirstDefUid() {
		return firstDefUid;
	}

	public void setFirstDefUid(String firstDefUid) {
		this.firstDefUid = firstDefUid;
	}

	public String getAtkPowerLostTotal() {
		return atkPowerLostTotal;
	}

	public void setAtkPowerLostTotal(String atkPowerLostTotal) {
		this.atkPowerLostTotal = atkPowerLostTotal;
	}

	public String getAtkKillTotal() {
		return atkKillTotal;
	}

	public void setAtkKillTotal(String atkKillTotal) {
		this.atkKillTotal = atkKillTotal;
	}

	public String getAtkDeadTotal() {
		return atkDeadTotal;
	}

	public void setAtkDeadTotal(String atkDeadTotal) {
		this.atkDeadTotal = atkDeadTotal;
	}

	public String getAtkHurtTotal() {
		return atkHurtTotal;
	}

	public void setAtkHurtTotal(String atkHurtTotal) {
		this.atkHurtTotal = atkHurtTotal;
	}

	public String getAtkRemain() {
		return atkRemain;
	}

	public void setAtkRemain(String atkRemain) {
		this.atkRemain = atkRemain;
	}

	public String getAtkPoison() {
		return atkPoison;
	}

	public void setAtkPoison(String atkPoison) {
		this.atkPoison = atkPoison;
	}

	public String getDefPoison() {
		return defPoison;
	}

	public void setDefPoison(String defPoison) {
		this.defPoison = defPoison;
	}

	public String getDefRecoverTotal() {
		return defRecoverTotal;
	}

	public void setDefRecoverTotal(String defRecoverTotal) {
		this.defRecoverTotal = defRecoverTotal;
	}

	public String getAtkRecoverTotal() {
		return atkRecoverTotal;
	}

	public void setAtkRecoverTotal(String atkRecoverTotal) {
		this.atkRecoverTotal = atkRecoverTotal;
	}

	public String getDefPowerLostTotal() {
		return defPowerLostTotal;
	}

	public void setDefPowerLostTotal(String defPowerLostTotal) {
		this.defPowerLostTotal = defPowerLostTotal;
	}

	public String getDefKillTotal() {
		return defKillTotal;
	}

	public void setDefKillTotal(String defKillTotal) {
		this.defKillTotal = defKillTotal;
	}

	public String getDefDeadTotal() {
		return defDeadTotal;
	}

	public void setDefDeadTotal(String defDeadTotal) {
		this.defDeadTotal = defDeadTotal;
	}

	public String getDefHurtTotal() {
		return defHurtTotal;
	}

	public void setDefHurtTotal(String defHurtTotal) {
		this.defHurtTotal = defHurtTotal;
	}

	public String getDefRemain() {
		return defRemain;
	}

	public void setDefRemain(String defRemain) {
		this.defRemain = defRemain;
	}

	public String getFightId() {
		return fightId;
	}

	public void setFightId(String fightId) {
		this.fightId = fightId;
	}

	public List<RewardParams> getPlunderResArray() {
		return plunderResArray;
	}

	public void setPlunderResArray(List<RewardParams> plunderResArray) {
		this.plunderResArray = plunderResArray;
	}

	public FBNewVersionPointInfoParams getBattlePointInfo() {
		return battlePointInfo;
	}

	public void setBattlePointInfo(FBNewVersionPointInfoParams battlePointInfo) {
		this.battlePointInfo = battlePointInfo;
	}

	public List<UserParams> getDefPlayerInfo() {
		return defPlayerInfo;
	}

	public void setDefPlayerInfo(List<UserParams> defPlayerInfo) {
		this.defPlayerInfo = defPlayerInfo;
	}

	public List<UserParams> getAtkPlayerInfo() {
		return atkPlayerInfo;
	}

	public void setAtkPlayerInfo(List<UserParams> atkPlayerInfo) {
		this.atkPlayerInfo = atkPlayerInfo;
	}
    
    public List<CaptureHeroParams> getCaptureHero() {
        return captureHero;
    }
    
    public void setCaptureHero(List<CaptureHeroParams> captureHero) {
        this.captureHero = captureHero;
    }

	public List<FBNewVersionOneRoundReportParams> getRoundFightArray() {
		return roundFightArray;
	}

	public void setRoundFightArray(List<FBNewVersionOneRoundReportParams> roundFightArray) {
		this.roundFightArray = roundFightArray;
	}

	private List<FBNewVersionOneRoundReportParams> roundFightArray;



//=============================================================================


	private String					worldFortressDefenceLoss; //副堡被打掉的城防值


	private String					userDesertReward;


	private String					showWordReport;//战报文字版
    
    private String					newBigLose; //差距过大无战报

	public String getShowWordReportGM() {
		return showWordReportGM;
	}

	public void setShowWordReportGM(String showWordReportGM) {
		this.showWordReportGM = showWordReportGM;
	}

	public String getShowAnimationReport() {
		return showAnimationReport;
	}

	public void setShowAnimationReport(String showAnimationReport) {
		this.showAnimationReport = showAnimationReport;
	}

	public String getShowAnimationReportGM() {
		return showAnimationReportGM;
	}

	public void setShowAnimationReportGM(String showAnimationReportGM) {
		this.showAnimationReportGM = showAnimationReportGM;
	}

	private String					showWordReportGM;//战报文字版

	private String					showAnimationReport;//战报文字版

	private String					showAnimationReportGM;//战报文字版

	private String					reportAbstractAnalysis;

	private String					reportAbstracttips;

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getShowWordReport()
	{
		return showWordReport;
	}

	public void setShowWordReport(String showWordReport)
	{
		this.showWordReport = showWordReport;
	}

	public String getWorldFortressDefenceLoss()
	{
		return worldFortressDefenceLoss;
	}

	public void setWorldFortressDefenceLoss(String worldFortressDefenceLoss)
	{
		this.worldFortressDefenceLoss = worldFortressDefenceLoss;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(String createTime)
	{
		this.createTime = createTime;
	}

	public boolean getIsResourceShieldState()
	{
		return isResourceShieldState;
	}

	public void setIsResourceShieldState(boolean isResourceShieldState)
	{
		this.isResourceShieldState = isResourceShieldState;
	}

	public int getAtkPowerLost()
	{
		return atkPowerLost;
	}

	public void setAtkPowerLost(int atkPowerLost)
	{
		this.atkPowerLost = atkPowerLost;
	}

	public int getDefPowerLost()
	{
		return defPowerLost;
	}

	public void setDefPowerLost(int defPowerLost)
	{
		this.defPowerLost = defPowerLost;
	}

	public String getWinner()
	{
		return winner;
	}

	public void setWinner(String winner)
	{
		this.winner = winner;
	}

	public int getBattleType()
	{
		return battleType;
	}

	public void setBattleType(int battleType)
	{
		this.battleType = battleType;
	}

	public int getProtectSide()
	{
		return protectSide;
	}

	public void setProtectSide(int protectSide)
	{
		this.protectSide = protectSide;
	}

	public int getBattleMailType() {
		return battleMailType;
	}

	public void setBattleMailType(int battleMailType) {
		this.battleMailType = battleMailType;
	}

	public int getIsBattlefieldServer() {
		return isBattlefieldServer;
	}

	public void setIsBattlefieldServer(int isBattlefieldServer) {
		this.isBattlefieldServer = isBattlefieldServer;
	}

	public String getReportUid()
	{
		return reportUid;
	}

	public void setReportUid(String reportUid)
	{
		this.reportUid = reportUid;
	}

	public int getPointType()
	{
		return pointType;
	}

	public void setPointType(int pointType)
	{
		this.pointType = pointType;
	}

	public String getWarPoint()
	{
		return warPoint;
	}

	public void setWarPoint(String warPoint)
	{
		this.warPoint = warPoint;
	}
	
	public int getWarServerId()
	{
		return warServerId;
	}
	
	public void setWarServerId(int warServerId){
		this.warServerId = warServerId;
	}

	public ArmyTotalParams getAtkArmyTotal()
	{
		return atkArmyTotal;
	}

	public void setAtkArmyTotal(ArmyTotalParams atkArmyTotal)
	{
		this.atkArmyTotal = atkArmyTotal;
	}

	public ArmyTotalParams getDefArmyTotal()
	{
		return defArmyTotal;
	}

	public void setDefArmyTotal(ArmyTotalParams defArmyTotal)
	{
		this.defArmyTotal = defArmyTotal;
	}

	public UserParams getDefUser()
	{
		return defUser;
	}

	public void setDefUser(UserParams defUser)
	{
		this.defUser = defUser;
	}

	public UserParams getAtkUser()
	{
		return atkUser;
	}

	public void setAtkUser(UserParams atkUser)
	{
		this.atkUser = atkUser;
	}

	public AllianceParams getAtkAlliance()
	{
		return atkAlliance;
	}

	public void setAtkAlliance(AllianceParams atkAlliance)
	{
		this.atkAlliance = atkAlliance;
	}

	public AllianceParams getDefAlliance()
	{
		return defAlliance;
	}

	public void setDefAlliance(AllianceParams defAlliance)
	{
		this.defAlliance = defAlliance;
	}

	public List<HelpReportParams> getAtkHelpReport()
	{
		return atkHelpReport;
	}

	public void setAtkHelpReport(List<HelpReportParams> atkHelpReport)
	{
		this.atkHelpReport = atkHelpReport;
	}

	public List<HelpReportParams> getDefHelpReport()
	{
		return defHelpReport;
	}

	public void setDefHelpReport(List<HelpReportParams> defHelpReport)
	{
		this.defHelpReport = defHelpReport;
	}

	public List<String> getAtkHelper()
	{
		return atkHelper;
	}

	public void setAtkHelper(List<String> atkHelper)
	{
		this.atkHelper = atkHelper;
	}

	public List<String> getDefHelper()
	{
		return defHelper;
	}

	public void setDefHelper(List<String> defHelper)
	{
		this.defHelper = defHelper;
	}

	public List<GenParams> getDefGen()
	{
		return defGen;
	}

	public void setDefGen(List<GenParams> defGen)
	{
		this.defGen = defGen;
	}

	public List<GenParams> getAtkGen()
	{
		return atkGen;
	}

	public void setAtkGen(List<GenParams> atkGen)
	{
		this.atkGen = atkGen;
	}

	public List<ArmyParams> getAtkReport()
	{
		return atkReport;
	}

	public void setAtkReport(List<ArmyParams> atkReport)
	{
		this.atkReport = atkReport;
	}

	public List<ArmyParams> getDefReport()
	{
		return defReport;
	}

	public void setDefReport(List<ArmyParams> defReport)
	{
		this.defReport = defReport;
	}

	public List<String> getAtkWarEffect()
	{
		return atkWarEffect;
	}

	public void setAtkWarEffect(List<String> atkWarEffect)
	{
		this.atkWarEffect = atkWarEffect;
	}

	public List<String> getDefskills() {
		return defskills;
	}

	public void setDefskills(List<String> defskills) {
		this.defskills = defskills;
	}




	public List<WorldFortressParams> getAtkWorldFortress() {
		return atkWorldFortress;
	}

	public void setAtkWorldFortress(List<WorldFortressParams> atkWorldFortress) {
		this.atkWorldFortress = atkWorldFortress;
	}

	public List<WorldFortressParams> getDefWorldFortress() {
		return defWorldFortress;
	}

	public void setDefWorldFortress(List<WorldFortressParams> defWorldFortress) {
		this.defWorldFortress = defWorldFortress;
	}

	public List<String> getAttskills() {
		return attskills;
	}

	public void setAttskills(List<String> attskills) {
		this.attskills = attskills;
	}

	public List<BuffParams> getAttBuffList() {
		return attBuffList;
	}

	public void setAttBuffList(List<BuffParams> attBuffList) {
		this.attBuffList = attBuffList;
	}

	public List<BuffParams> getDefBuffList() {
		return defBuffList;
	}

	public void setDefBuffList(List<BuffParams> defBuffList) {
		this.defBuffList = defBuffList;
	}

	public List<String> getDfWarEffect()
	{
		return dfWarEffect;
	}

	public void setDfWarEffect(List<String> dfWarEffect)
	{
		this.dfWarEffect = dfWarEffect;
	}

	public List<ArmyParams> getDefFortLost()
	{
		return defFortLost;
	}

	public void setDefFortLost(List<ArmyParams> defFortLost)
	{
		this.defFortLost = defFortLost;
	}

	public List<RewardParams> getReward()
	{
		return reward;
	}

	public void setReward(List<RewardParams> reward)
	{
		this.reward = reward;
	}

	public List<RateRewardParams> getKnightReward()
	{
		return knightReward;
	}

	public void setKnightReward(List<RateRewardParams> knightReward)
	{
		this.knightReward = knightReward;
	}
	

	public List<TowerKillParams> getDefTowerKill()
	{
		return defTowerKill;
	}

	public void setDefTowerKill(List<TowerKillParams> defTowerKill)
	{
		this.defTowerKill = defTowerKill;
	}

	public List<Integer> getAtkGenKill()
	{
		return atkGenKill;
	}

	public void setAtkGenKill(List<Integer> atkGenKill)
	{
		this.atkGenKill = atkGenKill;
	}

	public List<Integer> getDefGenKill()
	{
		return defGenKill;
	}

	public void setDefGenKill(List<Integer> defGenKill)
	{
		this.defGenKill = defGenKill;
	}

	public boolean isDefProtectActivate()
	{
		return defProtectActivate;
	}

	public void setDefProtectActivate(boolean defProtectActivate)
	{
		this.defProtectActivate = defProtectActivate;
	}

	public boolean isHideArmyDetail()
	{
		return hideArmyDetail;
	}

	public void setHideArmyDetail(boolean hideArmyDetail)
	{
		this.hideArmyDetail = hideArmyDetail;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public int getUserKill()
	{
		return userKill;
	}

	public void setUserKill(int userKill)
	{
		this.userKill = userKill;
	}

	public int getFailTimes()
	{
		return failTimes;
	}

	public void setFailTimes(int failTimes)
	{
		this.failTimes = failTimes;
	}

	public int getWinPercent()
	{
		return winPercent;
	}

	public void setWinPercent(int winPercent)
	{
		this.winPercent = winPercent;
	}

	public int getMonsterLevel()
	{
		return monsterLevel;
	}

	public void setMonsterLevel(int monsterLevel)
	{
		this.monsterLevel = monsterLevel;
	}

	public int getUserScore()
	{
		return userScore;
	}

	public void setUserScore(int userScore)
	{
		this.userScore = userScore;
	}


	public String getUserDesertReward()
	{
		return userDesertReward;
	}

	public void setUserDesertReward(String userDesertReward)
	{
		this.userDesertReward = userDesertReward;
	}




	public int getUserDesertExp()
	{
		return userDesertExp;
	}

	public void setUserDesertExp(int userDesertExp)
	{
		this.userDesertExp = userDesertExp;
	}

	public int getAllKill()
	{
		return allKill;
	}

	public void setAllKill(int allKill)
	{
		this.allKill = allKill;
	}

	public int getMsReport()
	{
		return msReport;
	}

	public void setMsReport(int msReport)
	{
		this.msReport = msReport;
	}

	public int getKillRound()
	{
		return killRound;
	}

	public void setKillRound(int killRound)
	{
		this.killRound = killRound;
	}

	public int getDefBeKilledCount()
	{
		return defBeKilledCount;
	}

	public void setDefBeKilledCount(int defBeKilledCount)
	{
		this.defBeKilledCount = defBeKilledCount;
	}

	public int getCkf()
	{
		return ckf;
	}

	public void setCkf(int ckf)
	{
		this.ckf = ckf;
	}

	public CkfWarInfoParams getCkfWarInfo()
	{
		return ckfWarInfo;
	}

	public void setCkfWarInfo(CkfWarInfoParams ckfWarInfo)
	{
		this.ckfWarInfo = ckfWarInfo;
	}

	public RemainResourceParams getRemainResource()
	{
		return remainResource;
	}

	public void setRemainResource(RemainResourceParams remainResource)
	{
		this.remainResource = remainResource;
	}

	public int getServerType()
	{
		return serverType;
	}

	public void setServerType(int serverType)
	{
		this.serverType = serverType;
	}

	public int getIsFreeAll() {
		return isFreeAll;
	}

	public void setIsFreeAll(int isFreeAll) {
		this.isFreeAll = isFreeAll;
	}

	public int getHeroState() {
		return heroState;
	}

	public void setHeroState(int heroState) {
		this.heroState = heroState;
	}


	public int getCaptureHeroLevel() {
		return captureHeroLevel;
	}

	public void setCaptureHeroLevel(int captureHeroLevel) {
		this.captureHeroLevel = captureHeroLevel;
	}

	public int getCaptureHeroId() {
		return captureHeroId;
	}

	public void setCaptureHeroId(int captureHeroId) {
		this.captureHeroId = captureHeroId;
	}

	public int getRoundNum() {
		return roundNum;
	}

	public void setRoundNum(int roundNum) {
		this.roundNum = roundNum;
	}

	public int getIsDemoFight() {
		return isDemoFight;
	}

	public void setIsDemoFight(int isDemoFight) {
		this.isDemoFight = isDemoFight;
	}
	//
	public List<ArmyAlterLevelParams> getDefTroopTranList() {
		return defTroopTranList;
	}

	public void setDefTroopTranList(List<ArmyAlterLevelParams> defTroopTranList) {
		this.defTroopTranList = defTroopTranList;
	}

	public List<ArmyAlterLevelParams> getAttTroopTranList() {
		return attTroopTranList;
	}

	public void setAttTroopTranList(List<ArmyAlterLevelParams> attTroopTranList) {
		this.attTroopTranList = attTroopTranList;
	}

	public int getWarServerType() {
		return warServerType;
	}

	public void setWarServerType(int warServerType) {
		this.warServerType = warServerType;
	}

	public String getReportAbstractAnalysis() {
		return reportAbstractAnalysis;
	}

	public void setReportAbstractAnalysis(String reportAbstractAnalysis) {
		this.reportAbstractAnalysis = reportAbstractAnalysis;
	}

	public String getReportAbstracttips() {
		return reportAbstracttips;
	}

	public void setReportAbstracttips(String reportAbstracttips) {
		this.reportAbstracttips = reportAbstracttips;
	}

	public FBNewVersoinDurableChangeInfo getDurableChangeInfo() {
		return durableChangeInfo;
	}

	public void setDurableChangeInfo(FBNewVersoinDurableChangeInfo durableChangeInfo) {
		this.durableChangeInfo = durableChangeInfo;
	}
    
    public void setNewBigLose(String newBigLose){
        this.newBigLose = newBigLose;
    }
    
    public String getNewBigLose(){
        return newBigLose;
    }
}
