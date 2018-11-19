package com.chatsdk.model.mail.newbattle;

import java.util.List;

public class NewVersionFormationParams
{

	private String	uid;
	private int	side;
	private String power; //此次战斗前的玩家战斗力
	private List<NewVersionCombatantParams> detail;

	public List<NewVersionCombatantParams> getDetail() {
		return detail;
	}

	public void setDetail(List<NewVersionCombatantParams> detail) {
		this.detail = detail;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPower() {
		return power;
	}

	public void setPower(String power) {
		this.power = power;
	}
}
