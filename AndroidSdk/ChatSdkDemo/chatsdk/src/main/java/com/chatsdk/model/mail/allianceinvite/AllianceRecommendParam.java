package com.chatsdk.model.mail.allianceinvite;

public class AllianceRecommendParam
{
	private String	iconAlliance;
	private String	allianceLang;
	private int		fightpower;
	private String	learderName;
	private String	alliancename;

	public String getIconAlliance() {
		return iconAlliance;
	}

	public void setIconAlliance(String iconAlliance) {
		this.iconAlliance = iconAlliance;
	}

	public String getAllianceLang() {
		return allianceLang;
	}

	public void setAllianceLang(String allianceLang) {
		this.allianceLang = allianceLang;
	}

	public int getFightpower() {
		return fightpower;
	}

	public void setFightpower(int fightpower) {
		this.fightpower = fightpower;
	}

	public String getLearderName() {
		return learderName;
	}

	public void setLearderName(String learderName) {
		this.learderName = learderName;
	}

	public String getAlliancename() {
		return alliancename;
	}

	public void setAlliancename(String alliancename) {
		this.alliancename = alliancename;
	}

	public int getCurMember() {
		return curMember;
	}

	public void setCurMember(int curMember) {
		this.curMember = curMember;
	}

	public String getAllianceId() {
		return allianceId;
	}

	public void setAllianceId(String allianceId) {
		this.allianceId = allianceId;
	}

	public int getMaxMember() {
		return maxMember;
	}

	public void setMaxMember(int maxMember) {
		this.maxMember = maxMember;
	}

	private int		curMember;
	private String	allianceId;
	private int		maxMember;



}

