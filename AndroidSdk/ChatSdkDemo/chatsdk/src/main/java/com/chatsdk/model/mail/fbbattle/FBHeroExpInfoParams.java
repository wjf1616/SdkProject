package com.chatsdk.model.mail.fbbattle;

public class FBHeroExpInfoParams
{
	private String	heroId;
	private int	oldExp;
	private int	newExp;
	private int	addExp;
	private int	newLv;
	private int	oldLv;

	public String getHeroId() {
		return heroId;
	}

	public void setHeroId(String heroId) {
		this.heroId = heroId;
	}

	public int getOldExp() {
		return oldExp;
	}

	public void setOldExp(int oldExp) {
		this.oldExp = oldExp;
	}

	public int getNewExp() {
		return newExp;
	}

	public void setNewExp(int newExp) {
		this.newExp = newExp;
	}

	public int getAddExp() {
		return addExp;
	}

	public void setAddExp(int addExp) {
		this.addExp = addExp;
	}

	public int getNewLv() {
		return newLv;
	}

	public void setNewLv(int newLv) {
		this.newLv = newLv;
	}

	public int getOldLv() {
		return oldLv;
	}

	public void setOldLv(int oldLv) {
		this.oldLv = oldLv;
	}
}
