package com.chatsdk.model.mail.newbattle;

import java.util.List;

public class NewVersionCombatantParams
{
	private List<String> skills;

	private List<String> effect;

	private NewVersionHeroParams hero;

	private NewVersionTroopParams troop;

	public List<String> getEffect() {
		return effect;
	}

	public void setEffect(List<String> effect) {
		this.effect = effect;
	}

	public NewVersionHeroParams getHero() {
		return hero;
	}

	public void setHero(NewVersionHeroParams hero) {
		this.hero = hero;
	}

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}

	public NewVersionTroopParams getTroop() {
		return troop;
	}

	public void setTroop(NewVersionTroopParams troop) {
		this.troop = troop;
	}

}
