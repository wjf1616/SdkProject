package com.chatsdk.model.mail.allianceinvite;


import java.util.List;

public class AllianceInviteMailContents
{
	private String	iconAlliance;
	private String	allianceLang;
	private String	inviteName;
	private String	invitePic;
	private String	invitePicVer;
	private int		deal;
	private String	contents;
	private int		fightpower;
	private String	learderName;
	private String	alliancename;
	private int		curMember;
	private String	allianceId;
	private int		maxMember;


	private List<AllianceRecommendParam>	allianceRecommendArr;

	public List<AllianceRecommendParam> getAllianceRecommendArr() {
		return allianceRecommendArr;
	}

	public void setAllianceRecommendArr(List<AllianceRecommendParam> allianceRecommendArr) {
		this.allianceRecommendArr = allianceRecommendArr;
	}

	public String getIconAlliance()
	{
		return iconAlliance;
	}

	public void setIconAlliance(String iconAlliance)
	{
		this.iconAlliance = iconAlliance;
	}

	public String getAllianceLang()
	{
		return allianceLang;
	}

	public void setAllianceLang(String allianceLang)
	{
		this.allianceLang = allianceLang;
	}

	public int getDeal()
	{
		return deal;
	}

	public void setDeal(int deal)
	{
		this.deal = deal;
	}

	public String getContents()
	{
		return contents;
	}

	public void setContents(String contents)
	{
		this.contents = contents;
	}

	public int getFightpower()
	{
		return fightpower;
	}

	public void setFightpower(int fightpower)
	{
		this.fightpower = fightpower;
	}

	public String getLearderName()
	{
		return learderName;
	}

	public void setLearderName(String learderName)
	{
		this.learderName = learderName;
	}

	public String getAlliancename()
	{
		return alliancename;
	}

	public void setAlliancename(String alliancename)
	{
		this.alliancename = alliancename;
	}

	public int getCurMember()
	{
		return curMember;
	}

	public void setCurMember(int curMember)
	{
		this.curMember = curMember;
	}

	public String getAllianceId()
	{
		return allianceId;
	}

	public void setAllianceId(String allianceId)
	{
		this.allianceId = allianceId;
	}

	public int getMaxMember()
	{
		return maxMember;
	}

	public void setMaxMember(int maxMember)
	{
		this.maxMember = maxMember;
	}
	
	public String getInvitePic()
	{
		return invitePic;
	}

	public void setInvitePic(String invitePic)
	{
		this.invitePic = invitePic;
	}
	
	public String getInvitePicVer()
	{
		return invitePicVer;
	}

	public void setInvitePicVer(String invitePicVer)
	{
		this.invitePicVer = invitePicVer;
	}
	
	public String getInviteName()
	{
		return inviteName;
	}

	public void setInviteName(String inviteName)
	{
		this.inviteName = inviteName;
	}

}
