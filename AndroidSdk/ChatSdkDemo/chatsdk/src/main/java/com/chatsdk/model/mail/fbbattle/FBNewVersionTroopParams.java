package com.chatsdk.model.mail.fbbattle;

public class FBNewVersionTroopParams
{
	private String	armyId;
	private int	armyKill;
	private int	armyHurt;
	private int	armyDead;
	private int	armyRemain;
	private int	armyRecover;
	private String armyEffect;
	private int	armyPoison;
    private int recoverTotal;
    private String armyDetail;
    
	public int getArmyRecover() {
		return armyRecover;
	}

	public void setArmyRecover(int armyRecover) {
		this.armyRecover = armyRecover;
	}

	public String getArmyId() {
		return armyId;
	}

	public void setArmyId(String armyId) {
		this.armyId = armyId;
	}

	public int getArmyKill() {
		return armyKill;
	}

	public void setArmyKill(int armyKill) {
		this.armyKill = armyKill;
	}

	public int getArmyHurt() {
		return armyHurt;
	}

	public void setArmyHurt(int armyHurt) {
		this.armyHurt = armyHurt;
	}

	public int getArmyDead() {
		return armyDead;
	}

	public void setArmyDead(int armyDead) {
		this.armyDead = armyDead;
	}

	public int getArmyRemain() {
		return armyRemain;
	}

	public void setArmyRemain(int armyRemain) {
		this.armyRemain = armyRemain;
	}

	public String getArmyEffect() {
		return armyEffect;
	}

	public void setArmyEffect(String armyEffect) {
		this.armyEffect = armyEffect;
	}
    
    public String getArmyDetail() {
        return armyDetail;
    }
    
    public void setArmyDetail(String armyDetail) {
        this.armyDetail = armyDetail;
    }

	public int getArmyPoison() {
		return armyPoison;
	}

	public void setArmyPoison(int armyPoison) {
		this.armyPoison = armyPoison;
	}
    
    public int getRecoverTotal(){
        return recoverTotal;
    }
    
    public void setRecoverTotal(int recoverTotal){
        this.recoverTotal = recoverTotal;
    }
}
