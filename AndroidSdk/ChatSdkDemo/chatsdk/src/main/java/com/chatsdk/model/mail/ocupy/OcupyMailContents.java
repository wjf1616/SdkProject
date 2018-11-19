package com.chatsdk.model.mail.ocupy;

import java.util.List;

public class OcupyMailContents
{

	private int					pointId;
	private List<ArmysParams>	arms;
	private UserInfoParams		user;
	private int					pointType;
	private boolean				isTreasureMap;
	private int					ckf;
	private int					serverType;
	private int					warServerType;

	public int getPointId()
	{
		return pointId;
	}

	public void setPointId(int pointId)
	{
		this.pointId = pointId;
	}

	public List<ArmysParams> getArms()
	{
		return arms;
	}

	public void setArms(List<ArmysParams> arms)
	{
		this.arms = arms;
	}

	public UserInfoParams getUser()
	{
		return user;
	}

	public void setUser(UserInfoParams user)
	{
		this.user = user;
	}

	public int getPointType()
	{
		return pointType;
	}

	public void setPointType(int pointType)
	{
		this.pointType = pointType;
	}

	public boolean isTreasureMap()
	{
		return isTreasureMap;
	}

	public void setTreasureMap(boolean isTreasureMap)
	{
		this.isTreasureMap = isTreasureMap;
	}

	public int getCkf()
	{
		return ckf;
	}

	public void setCkf(int ckf)
	{
		this.ckf = ckf;
	}

	public int getServerType()
	{
		return serverType;
	}

	public void setServerType(int serverType)
	{
		this.serverType = serverType;
	}

	public int getWarServerType() {
		return warServerType;
	}

	public void setWarServerType(int warServerType) {
		this.warServerType = warServerType;
	}

}
