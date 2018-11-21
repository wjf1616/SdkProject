package com.chatsdk.model.mail.fbscoutreport;

import com.chatsdk.model.mail.detectreport.UserInfoParams;
import java.util.List;

public class FBDetectReportMailContents
{

	private FBCityBaseInfoParams cityBaseInfo;
	private UserInfoParams userInfo;

	private FBUserArmyAllParams userArmy;
	private FBMemberInfoParams memberInfo;


	private long								pointId;//侦查的位置 不一定有

	public FBCityBaseInfoParams getCityBaseInfo() {
		return cityBaseInfo;
	}

	public void setCityBaseInfo(FBCityBaseInfoParams cityBaseInfo) {
		this.cityBaseInfo = cityBaseInfo;
	}

	public UserInfoParams getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfoParams userInfo) {
		this.userInfo = userInfo;
	}

	public FBUserArmyAllParams getUserArmy() {
		return userArmy;
	}

	public void setUserArmy(FBUserArmyAllParams userArmy) {
		this.userArmy = userArmy;
	}

	public FBMemberInfoParams getMemberInfo() {
		return memberInfo;
	}

	public void setMemberInfo(FBMemberInfoParams memberInfo) {
		this.memberInfo = memberInfo;
	}

	public long getPointId() {
		return pointId;
	}

	public void setPointId(long pointId) {
		this.pointId = pointId;
	}


	public int getServerId() {
		return serverId;
	}

	public String getCareerId() {
		return careerId;
	}

	public void setCareerId(String careerId) {
		this.careerId = careerId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getWarServerType() {
		return warServerType;
	}

	public void setWarServerType(int warServerType) {
		this.warServerType = warServerType;
	}

	public int getPointType() {
		return pointType;
	}

	public void setPointType(int pointType) {
		this.pointType = pointType;
	}

	public int getUserArmyType() {
		return userArmyType;
	}

	public void setUserArmyType(int userArmyType) {
		this.userArmyType = userArmyType;
	}

	public int getMemberArmyType() {
		return memberArmyType;
	}

	public void setMemberArmyType(int memberArmyType) {
		this.memberArmyType = memberArmyType;
	}

    public void setIsKingNew(int isKingNew){
        this.isKingNew = isKingNew;
    }
    
    public int getIsKingNew(){
        return isKingNew;
    }
	private String									careerId; //职业
	private int									serverId;//所在服
	private int									warServerType;//服务器的类型服 //大王座等特殊地图
	private int									pointType;

//侦查类型
	private int									userArmyType;
	private int									memberArmyType;
    private int                                 isKingNew;
}
