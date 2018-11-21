package com.chatsdk.model.mail.seasonWarZone;

import java.util.List;

public class SeasonWarZoneMailContents
{

	private int							seasonNumber;//赛季id
	private int							serverId;//服id
	private int							group;//组别
	private int							rank;//排名
	private String						name;//国家名字
	private String						banner; //国旗
	private int							riseInfo; //1 晋升 2 降级 0 不升不降
	private String						contributionScore; //贡献分
	private int							contributionRank; //贡献排名 
	private List<VsResultParams>	vsResult;


	public int getSeasonNumber() {
		return seasonNumber;
	}

	public void setSeasonNumber(int seasonNumber) {
		this.seasonNumber = seasonNumber;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
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

	public String getContributionScore() {
		return contributionScore;
	}

	public void setContributionScore(String contributionScore) {
		this.contributionScore = contributionScore;
	}

	public int getRiseInfo() {
		return riseInfo;
	}

	public void setRiseInfo(int riseInfo) {
		this.riseInfo = riseInfo;
	}



	public int getContributionRank() {
		return contributionRank;
	}

	public void setContributionRank(int contributionRank) {
		this.contributionRank = contributionRank;
	}

	public List<VsResultParams> getVsResult() {
		return vsResult;
	}

	public void setVsResult(List<VsResultParams> vsResult) {
		this.vsResult = vsResult;
	}
}
