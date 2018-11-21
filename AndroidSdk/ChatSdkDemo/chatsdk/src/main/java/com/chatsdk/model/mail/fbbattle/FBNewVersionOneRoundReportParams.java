package com.chatsdk.model.mail.fbbattle;

import com.chatsdk.model.mail.battle.ArmyParams;
import com.chatsdk.model.mail.battle.TowerKillParams;

import java.util.List;

public class FBNewVersionOneRoundReportParams
{
	private String atkUid;
	private String defUid;

	public String getDefNpcId() {
		return defNpcId;
	}

	public void setDefNpcId(String defNpcId) {
		this.defNpcId = defNpcId;
	}

	private String defNpcId;


	private int	 round; //第几场

	private int	 maxRound; //这一场几个回合

	public int getMaxRound() {
		return maxRound;
	}

	public void setMaxRound(int maxRound) {
		this.maxRound = maxRound;
	}

	private String reportUid; //战斗动画播放id

	public String getReportUid() {
		return reportUid;
	}

	public void setReportUid(String reportUid) {
		this.reportUid = reportUid;
	}

	private int	 roundResult;//结果  0 攻击方胜利 1 防守方胜利 2 平局


	private int	 dead; //  士气 1表示崩溃

	public int getDead() {
		return dead;
	}

	public void setDead(int dead) {
		this.dead = dead;
	}

	private int	  atkPowerLost;

	private int	  defPowerLost;


	private List<FBNewVersionFormationParams>	atkBattleDetailInfo;

	private List<FBNewVersionFormationParams>	defBattleDetailInfo;

	public String getAtkUid() {
		return atkUid;
	}

	public void setAtkUid(String atkUid) {
		this.atkUid = atkUid;
	}

	public String getDefUid() {
		return defUid;
	}

	public void setDefUid(String defUid) {
		this.defUid = defUid;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getRoundResult() {
		return roundResult;
	}

	public void setRoundResult(int roundResult) {
		this.roundResult = roundResult;
	}

	public int getAtkPowerLost() {
		return atkPowerLost;
	}

	public void setAtkPowerLost(int atkPowerLost) {
		this.atkPowerLost = atkPowerLost;
	}

	public int getDefPowerLost() {
		return defPowerLost;
	}

	public void setDefPowerLost(int defPowerLost) {
		this.defPowerLost = defPowerLost;
	}

	public List<FBNewVersionFormationParams> getAtkBattleDetailInfo() {
		return atkBattleDetailInfo;
	}

	public void setAtkBattleDetailInfo(List<FBNewVersionFormationParams> atkBattleDetailInfo) {
		this.atkBattleDetailInfo = atkBattleDetailInfo;
	}

	public List<FBNewVersionFormationParams> getDefBattleDetailInfo() {
		return defBattleDetailInfo;
	}

	public void setDefBattleDetailInfo(List<FBNewVersionFormationParams> defBattleDetailInfo) {
		this.defBattleDetailInfo = defBattleDetailInfo;
	}
}
