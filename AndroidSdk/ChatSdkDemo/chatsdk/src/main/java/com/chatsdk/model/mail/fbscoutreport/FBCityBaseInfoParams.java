package com.chatsdk.model.mail.fbscoutreport;

public class FBCityBaseInfoParams
{
	private int		cityDefMax;//城防值 上限;
	private int		 cityDefValue;//当前城防值
	private int		 isResourceShieldState;//资源保护的技能 有这个技能的时候 抢不了资源
	private int		 wood_plunder;//可掠夺油
	private int		 food_plunder;//可掠粮食
	private int		 iron_plunder;//可掠铁
	private int		stone_plunder;//可掠合金
	private int		 money_plunder;////可掠钱 不一定

	public int getCityDefMax() {
		return cityDefMax;
	}

	public void setCityDefMax(int cityDefMax) {
		this.cityDefMax = cityDefMax;
	}

	public int getCityDefValue() {
		return cityDefValue;
	}

	public void setCityDefValue(int cityDefValue) {
		this.cityDefValue = cityDefValue;
	}

	public int getIsResourceShieldState() {
		return isResourceShieldState;
	}

	public void setIsResourceShieldState(int isResourceShieldState) {
		this.isResourceShieldState = isResourceShieldState;
	}

	public int getWood_plunder() {
		return wood_plunder;
	}

	public void setWood_plunder(int wood_plunder) {
		this.wood_plunder = wood_plunder;
	}

	public int getFood_plunder() {
		return food_plunder;
	}

	public void setFood_plunder(int food_plunder) {
		this.food_plunder = food_plunder;
	}

	public int getIron_plunder() {
		return iron_plunder;
	}

	public void setIron_plunder(int iron_plunder) {
		this.iron_plunder = iron_plunder;
	}

	public int getStone_plunder() {
		return stone_plunder;
	}

	public void setStone_plunder(int stone_plunder) {
		this.stone_plunder = stone_plunder;
	}

	public int getMoney_plunder() {
		return money_plunder;
	}

	public void setMoney_plunder(int money_plunder) {
		this.money_plunder = money_plunder;
	}
}
