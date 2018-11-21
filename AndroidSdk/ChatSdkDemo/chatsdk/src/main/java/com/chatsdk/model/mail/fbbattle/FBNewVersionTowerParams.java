package com.chatsdk.model.mail.fbbattle;

import java.util.List;

public class FBNewVersionTowerParams
{
	private String	skillId;     //技能id

	private String	level;       //等级

	private String	killEnemy;   //击杀数

	private String	costEnergy;  //消耗能量

	private String	skillLevel;  //技能等级

	private String	attackTimes; //攻击次数

	public String getSkillId() {
		return skillId;
	}

	public void setSkillId(String skillId) {
		this.skillId = skillId;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getKillEnemy() {
		return killEnemy;
	}

	public void setKillEnemy(String killEnemy) {
		this.killEnemy = killEnemy;
	}

	public String getCostEnergy() {
		return costEnergy;
	}

	public void setCostEnergy(String costEnergy) {
		this.costEnergy = costEnergy;
	}

	public String getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(String skillLevel) {
		this.skillLevel = skillLevel;
	}

	public String getAttackTimes() {
		return attackTimes;
	}

	public void setAttackTimes(String attackTimes) {
		this.attackTimes = attackTimes;
	}
}
