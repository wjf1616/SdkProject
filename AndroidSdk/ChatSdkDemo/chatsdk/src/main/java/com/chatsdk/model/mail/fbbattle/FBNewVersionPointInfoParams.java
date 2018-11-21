package com.chatsdk.model.mail.fbbattle;

import java.util.List;

public class FBNewVersionPointInfoParams
{

	private int	battleServerId;  // 所在服id

	private int	serverType;  //所在服 服务器类型

	private int	pointId; //位置id

	private int	worldId; // worldId 竞技场世界分层

	private int	pointType; // pointType 类型


	public int getBattleServerId() {
		return battleServerId;
	}

	public void setBattleServerId(int battleServerId) {
		this.battleServerId = battleServerId;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}

	public int getWorldId() {
		return worldId;
	}

	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}

	public int getPointType() {
		return pointType;
	}

	public void setPointType(int pointType) {
		this.pointType = pointType;
	}
}
