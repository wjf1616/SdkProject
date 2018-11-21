package com.chatsdk.model.mail.fbbattle;

import java.util.List;

public class FBNewVersionHeroParams
{
	private int	heroKill;

	private int	heroSkillTimes;

	private String	heroId;

	private String	heroLv;

	public int getHeroKill() {
		return heroKill;
	}

	public void setHeroKill(int heroKill) {
		this.heroKill = heroKill;
	}

	public int getHeroSkillTimes() {
		return heroSkillTimes;
	}

	public void setHeroSkillTimes(int heroSkillTimes) {
		this.heroSkillTimes = heroSkillTimes;
	}

	public String getHeroId() {
		return heroId;
	}

	public void setHeroId(String heroId) {
		this.heroId = heroId;
	}

	public String getHeroLv() {
		return heroLv;
	}

	public void setHeroLv(String heroLv) {
		this.heroLv = heroLv;
	}

	public List<FBHeroSkillInfoParams> getHeroSkillInfo() {
		return heroSkillInfo;
	}

	public void setHeroSkillInfo(List<FBHeroSkillInfoParams> heroSkillInfo) {
		this.heroSkillInfo = heroSkillInfo;
	}

	private List<FBHeroSkillInfoParams> heroSkillInfo;


}
