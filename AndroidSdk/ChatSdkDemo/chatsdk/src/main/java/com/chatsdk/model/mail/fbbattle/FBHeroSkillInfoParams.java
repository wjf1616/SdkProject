package com.chatsdk.model.mail.fbbattle;

public class FBHeroSkillInfoParams
{

	private String	skillId;

	private int	skillLevel;
    
    private int valid;  //0代表该技能在战斗中生效，否则未生效

	public String getSkillId() {
		return skillId;
	}

	public void setSkillId(String skillId) {
		this.skillId = skillId;
	}

	public int getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
	}
    
    public void setValid(int valid){
        this.valid = valid;
    }
    
    public int getValid(){
        return valid;
    }
}
