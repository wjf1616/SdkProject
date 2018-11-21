package com.chatsdk.model.mail.detectreport;

public class ResourceParams
{
	private long	wood;
	private long	cityDefMax;
	private long	food;
	private long	food_not_collected;
	private long	stone;
	private boolean	isResourceShieldState;
	private long	cityDefValue;
	private long	stone_not_collected;
	private long	wood_not_collected;
	private long	iron;
	private long	iron_not_collected;
	private long	iron_plunder;
	private long	wood_plunder;
	private long	food_plunder;
	private long	stone_plunder;

	public boolean isResourceShieldState() {
		return isResourceShieldState;
	}

	public void setResourceShieldState(boolean resourceShieldState) {
		isResourceShieldState = resourceShieldState;
	}

	public long getMoney() {
		return money;
	}

	public void setMoney(long money) {
		this.money = money;
	}

	private long	money;

	public long getWood()
	{
		return wood;
	}

	public void setWood(long wood)
	{
		this.wood = wood;
	}

	public long getCityDefMax()
	{
		return cityDefMax;
	}

	public void setCityDefMax(long cityDefMax)
	{
		this.cityDefMax = cityDefMax;
	}

	public long getFood()
	{
		return food;
	}

	public void setFood(long food)
	{
		this.food = food;
	}

	public long getFood_not_collected()
	{
		return food_not_collected;
	}

	public void setFood_not_collected(long food_not_collected)
	{
		this.food_not_collected = food_not_collected;
	}

	public long getStone()
	{
		return stone;
	}

	public void setStone(long stone)
	{
		this.stone = stone;
	}

	public boolean getIsResourceShieldState()
	{
		return isResourceShieldState;
	}

	public void setIsResourceShieldState(boolean isResourceShieldState)
	{
		this.isResourceShieldState = isResourceShieldState;
	}

	public long getCityDefValue()
	{
		return cityDefValue;
	}

	public void setCityDefValue(long cityDefValue)
	{
		this.cityDefValue = cityDefValue;
	}

	public long getStone_not_collected()
	{
		return stone_not_collected;
	}

	public void setStone_not_collected(long stone_not_collected)
	{
		this.stone_not_collected = stone_not_collected;
	}

	public long getWood_not_collected()
	{
		return wood_not_collected;
	}

	public void setWood_not_collected(long wood_not_collected)
	{
		this.wood_not_collected = wood_not_collected;
	}

	public long getIron()
	{
		return iron;
	}

	public void setIron(long iron)
	{
		this.iron = iron;
	}

	public long getIron_not_collected()
	{
		return iron_not_collected;
	}

	public void setIron_not_collected(long iron_not_collected)
	{
		this.iron_not_collected = iron_not_collected;
	}

	public void setIron_plunder(long iron_plunder) { this.iron_plunder = iron_plunder; }

	public long getIron_plunder() { return iron_plunder; }

	public void setWood_plunder(long wood_plunder) { this.wood_plunder = wood_plunder; }

	public long getWood_plunder() { return wood_plunder; }

	public void setFood_plunder(long food_plunder) { this.food_plunder = food_plunder; }

	public long getFood_plunder() { return food_plunder; }

	public void setStone_plunder(long stone_plunder) { this.stone_plunder = stone_plunder; }

	public long getStone_plunder() { return stone_plunder; }

}
