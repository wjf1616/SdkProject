package com.chatsdk.model.mail.newbattle;

import com.chatsdk.model.mail.battle.ArmyParams;
import com.chatsdk.model.mail.battle.TowerKillParams;

import java.util.List;

public class NewVersionOneRoundReportParams
{

	private String	uid;

	private int	side;

	private String powerLost;

	private List<ArmyParams>	trap;

	private List<ArmyParams> troop;

	private List<TowerKillParams>	tower;

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public List<TowerKillParams> getTower() {
		return tower;
	}

	public void setTower(List<TowerKillParams> tower) {
		this.tower = tower;
	}

	public List<ArmyParams> getTrap() {
		return trap;
	}

	public void setTrap(List<ArmyParams> trap) {
		this.trap = trap;
	}

	public List<ArmyParams> getTroop() {
		return troop;
	}

	public void setTroop(List<ArmyParams> troop) {
		this.troop = troop;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPowerLost() {
		return powerLost;
	}

	public void setPowerLost(String powerLost) {
		this.powerLost = powerLost;
	}
}
