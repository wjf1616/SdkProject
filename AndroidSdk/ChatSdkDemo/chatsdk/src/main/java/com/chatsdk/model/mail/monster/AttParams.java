package com.chatsdk.model.mail.monster;

public class AttParams
{
	private int	total;
	private int	exp;
	private int	hurt;
	private int	survived;
	private int	kill;
	private int	powerLost;
	private int	dead;

	private long desertExp;

	private String		allKill;

	public String getAllKill() {
		return allKill;
	}

	public void setAllKill(String allKill) {
		this.allKill = allKill;
	}

	public long getDesertExp()
	{
		return desertExp;
	}

	public void setDesertExp(long desertExp)
	{
		this.desertExp = desertExp;
	}


	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public int getExp()
	{
		return exp;
	}

	public void setExp(int exp)
	{
		this.exp = exp;
	}

	public int getHurt()
	{
		return hurt;
	}

	public void setHurt(int hurt)
	{
		this.hurt = hurt;
	}

	public int getSurvived()
	{
		return survived;
	}

	public void setSurvived(int survived)
	{
		this.survived = survived;
	}

	public int getKill()
	{
		return kill;
	}

	public void setKill(int kill)
	{
		this.kill = kill;
	}

	public int getPowerLost()
	{
		return powerLost;
	}

	public void setPowerLost(int powerLost)
	{
		this.powerLost = powerLost;
	}

	public int getDead()
	{
		return dead;
	}

	public void setDead(int dead)
	{
		this.dead = dead;
	}

}
