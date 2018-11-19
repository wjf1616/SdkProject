package com.chatsdk.model.mail.detectreport;

import java.util.List;

public class DetectReportMailContents
{

	private List<ReinDetailParams>				rein_detail;
	private List<TowerParams>					tower;
	private DefenceParams						defence;
	private String								name;
	private ResourceParams						resource;
	private long								pointId;
	private List<List<ReinAboutDetailParams>>	rein_about_detail;
	private ReinAboutParams						rein_about;
	private FortParams							fort;
	private UserInfoParams						user;
	private List<ScienceParams>					science;
	private int									pointType;
	private int									isBattlefieldServer;//末日角斗场标记
	private String								contents;
	private List<String>						ability;
	private int									ckf;
	private int									warServerType;
	private int									attUserServerId;
    private boolean                             isScoutArmy;//是否是侦查兵侦查
    private int                                 scoutArmyLoss;//侦查兵损失人数
	private MissileAboutParams					missile;

	public ReinAboutParams getRein_about()
	{
		return rein_about;
	}

	public void setRein_about(ReinAboutParams rein_about)
	{
		this.rein_about = rein_about;
	}

	public List<ReinDetailParams> getRein_detail()
	{
		return rein_detail;
	}

	public void setRein_detail(List<ReinDetailParams> rein_detail)
	{
		this.rein_detail = rein_detail;
	}

	public List<TowerParams> getTower()
	{
		return tower;
	}

	public void setTower(List<TowerParams> tower)
	{
		this.tower = tower;
	}

	public DefenceParams getDefence()
	{
		return defence;
	}

	public void setDefence(DefenceParams defence)
	{
		this.defence = defence;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ResourceParams getResource()
	{
		return resource;
	}

	public void setResource(ResourceParams resource)
	{
		this.resource = resource;
	}

	public long getPointId()
	{
		return pointId;
	}

	public void setPointId(long pointId)
	{
		this.pointId = pointId;
	}

	public List<List<ReinAboutDetailParams>> getRein_about_detail()
	{
		return rein_about_detail;
	}

	public void setRein_about_detail(List<List<ReinAboutDetailParams>> rein_about_detail)
	{
		this.rein_about_detail = rein_about_detail;
	}

	public FortParams getFort()
	{
		return fort;
	}

	public void setFort(FortParams fort)
	{
		this.fort = fort;
	}

	public UserInfoParams getUser()
	{
		return user;
	}

	public void setUser(UserInfoParams user)
	{
		this.user = user;
	}

	public List<ScienceParams> getScience()
	{
		return science;
	}

	public void setScience(List<ScienceParams> science)
	{
		this.science = science;
	}

	public int getPointType()
	{
		return pointType;
	}

	public void setPointType(int pointType)
	{
		this.pointType = pointType;
	}

	public int getIsBattlefieldServer()
	{
		return isBattlefieldServer;
	}

	public void setIsBattlefieldServer(int isBattlefieldServer)
	{
		this.isBattlefieldServer = isBattlefieldServer;
	}

	public String getContents()
	{
		return contents;
	}

	public void setContents(String contents)
	{
		this.contents = contents;
	}

	public List<String> getAbility()
	{
		return ability;
	}

	public void setAbility(List<String> ability)
	{
		this.ability = ability;
	}

	public int getCkf()
	{
		return ckf;
	}

	public void setCkf(int ckf)
	{
		this.ckf = ckf;
	}

	public int getWarServerType() {
		return warServerType;
	}

	public void setWarServerType(int warServerType) {
		this.warServerType = warServerType;
	}

	public int getAttUserServerId(){return attUserServerId;}

	public void setAttUserServerId(int attUserServerId){ this.attUserServerId = attUserServerId;}
	public MissileAboutParams getMissile() {
		return missile;
	}

	public void setMissile(MissileAboutParams missile) {
		this.missile = missile;
	}

    
    public void setIsScoutArmy(boolean isScoutArmy)
    {
        this.isScoutArmy = isScoutArmy;
    }
    
    public boolean getIsScoutArmy(){
        return isScoutArmy;
    }
    
    public void setScoutArmyLoss(int scoutArmyLoss){
        this.scoutArmyLoss = scoutArmyLoss;
    }
    
    public int getScoutArmyLoss(){
        return scoutArmyLoss;
    }
}
