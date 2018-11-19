package com.chatsdk.model.mail.battle;

import java.util.List;

public class GenParams
{
	private int					level;
	private int					defence;
	private List<SkillParams>	skill;
	private int					status;
	private int					att;
	private String				uuid;
	private String				generalId;
	private List<String>		ability;

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public int getDefence()
	{
		return defence;
	}

	public void setDefence(int defence)
	{
		this.defence = defence;
	}

	public List<SkillParams> getSkill()
	{
		return skill;
	}

	public void setSkill(List<SkillParams> skill)
	{
		this.skill = skill;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public int getAtt()
	{
		return att;
	}

	public void setAtt(int att)
	{
		this.att = att;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getGeneralId()
	{
		return generalId;
	}

	public void setGeneralId(String generalId)
	{
		this.generalId = generalId;
	}

	public List<String> getAbility()
	{
		return ability;
	}

	public void setAbility(List<String> ability)
	{
		this.ability = ability;
	}

}
