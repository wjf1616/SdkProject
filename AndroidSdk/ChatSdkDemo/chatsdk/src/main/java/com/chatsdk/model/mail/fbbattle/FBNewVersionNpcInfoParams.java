package com.chatsdk.model.mail.fbbattle;

public class FBNewVersionNpcInfoParams
{

	private int	serverId;  // 所在服id

	private int	pointId;  ////位置id

	private String	npcId; // Npcid

	private String	npcName; // npcName

	private String	npcPic; // npcPic

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}

	public String getNpcId() {
		return npcId;
	}

	public void setNpcId(String npcId) {
		this.npcId = npcId;
	}

	public String getNpcName() {
		return npcName;
	}

	public void setNpcName(String npcName) {
		this.npcName = npcName;
	}

	public String getNpcPic() {
		return npcPic;
	}

	public void setNpcPic(String npcPic) {
		this.npcPic = npcPic;
	}
}
