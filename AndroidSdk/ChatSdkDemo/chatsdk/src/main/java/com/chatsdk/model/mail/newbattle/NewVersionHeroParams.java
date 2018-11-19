package com.chatsdk.model.mail.newbattle;

public class NewVersionHeroParams
{

	private String	speed;

	private String	defend;

	private String	attack;

	private String	tactics;

	private String	heroId;

	private String	heroLv;

	public String getAttack() {
		return attack;
	}

	public void setAttack(String attack) {
		this.attack = attack;
	}

	public String getDefend() {
		return defend;
	}

	public void setDefend(String defend) {
		this.defend = defend;
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

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getTactics() {
		return tactics;
	}

	public void setTactics(String tactics) {
		this.tactics = tactics;
	}
}
