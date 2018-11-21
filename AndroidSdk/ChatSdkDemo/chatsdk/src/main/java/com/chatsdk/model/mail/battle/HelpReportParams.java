package com.chatsdk.model.mail.battle;

import java.util.List;

public class HelpReportParams
{
	private List<Integer>		genKill;
	private List<GenParams>		genInfo;
	private String				name;
	private List<ArmyParams>	armyInfo;
	private String				heroId;
	private String              heroLv;
	private List<String>		skills;
	private List<String>		warEffect;

	private List<WorldFortressParams> worldFortress;


	public List<Integer> getGenKill()
	{
		return genKill;
	}

	public void setGenKill(List<Integer> genKill)
	{
		this.genKill = genKill;
	}

	public List<GenParams> getGenInfo()
	{
		return genInfo;
	}

	public void setGenInfo(List<GenParams> genInfo)
	{
		this.genInfo = genInfo;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<ArmyParams> getArmyInfo()
	{
		return armyInfo;
	}

	public void setArmyInfo(List<ArmyParams> armyInfo)
	{
		this.armyInfo = armyInfo;
	}

	public String getHeroId()
	{
		return heroId;
	}

	public void setHeroId(String heroId)
	{
		this.heroId = heroId;
	}

	public String getHeroLv()
	{
		return heroLv;
	}

	public void setHeroLv(String heroLv)
	{
		this.heroLv = heroLv;
	}

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}

	public List<String> getWarEffect() {
		return warEffect;
	}

	public void setWarEffect(List<String> warEffect) {
		this.warEffect = warEffect;
	}

	public List<WorldFortressParams> getWorldFortress() {
		return worldFortress;
	}

	public void setWorldFortress(List<WorldFortressParams> worldFortress) {
		this.worldFortress = worldFortress;
	}
}
