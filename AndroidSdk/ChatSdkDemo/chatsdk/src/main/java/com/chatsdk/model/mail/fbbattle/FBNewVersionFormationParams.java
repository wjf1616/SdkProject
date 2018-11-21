package com.chatsdk.model.mail.fbbattle;

import java.util.List;

public class FBNewVersionFormationParams
{
	private FBNewVersionTroopParams	army;

	private FBNewVersionHeroParams	hero;
    
    private FBNewVersionTowerParams tower;

	public FBNewVersionHeroParams getHero() {
		return hero;
	}

	public void setHero(FBNewVersionHeroParams hero) {
		this.hero = hero;
	}

	public FBNewVersionTroopParams getArmy() {
		return army;
	}

	public void setArmy(FBNewVersionTroopParams army) {
		this.army = army;
	}

    public FBNewVersionTowerParams getTower() {
        return tower;
    }
    
    public void setTower(FBNewVersionTowerParams tower) {
        this.tower = tower;
    }
}
