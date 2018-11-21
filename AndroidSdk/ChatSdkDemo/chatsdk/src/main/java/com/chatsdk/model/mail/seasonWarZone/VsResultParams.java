package com.chatsdk.model.mail.seasonWarZone;

public class VsResultParams
{

	private int	score;
	private int	serverId;
	private String		name;
	private String		banner; //国旗

	private int	vsScore;
	private int	vsServerId;
	private String		vsName;
	private String		vsBanner;

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	public int getVsScore() {
		return vsScore;
	}

	public void setVsScore(int vsScore) {
		this.vsScore = vsScore;
	}

	public int getVsServerId() {
		return vsServerId;
	}

	public void setVsServerId(int vsServerId) {
		this.vsServerId = vsServerId;
	}

	public String getVsName() {
		return vsName;
	}

	public void setVsName(String vsName) {
		this.vsName = vsName;
	}

	public String getVsBanner() {
		return vsBanner;
	}

	public void setVsBanner(String vsBanner) {
		this.vsBanner = vsBanner;
	}
}
