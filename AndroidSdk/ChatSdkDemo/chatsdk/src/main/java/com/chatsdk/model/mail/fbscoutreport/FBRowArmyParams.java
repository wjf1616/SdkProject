package com.chatsdk.model.mail.fbscoutreport;
import com.chatsdk.model.mail.fbbattle.FBNewVersionHeroParams;

public class FBRowArmyParams
{

	private String	armyId;//// 士兵id


	FBNewVersionHeroParams  heroInfo;


	private int		 number;// 士兵数量

	public String getArmyId() {
		return armyId;
	}

	public void setArmyId(String armyId) {
		this.armyId = armyId;
	}

	public FBNewVersionHeroParams getHeroInfo() {
		return heroInfo;
	}

	public void setHeroInfo(FBNewVersionHeroParams heroInfo) {
		this.heroInfo = heroInfo;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	private int		 row;// 第几排 0 第一排
}
