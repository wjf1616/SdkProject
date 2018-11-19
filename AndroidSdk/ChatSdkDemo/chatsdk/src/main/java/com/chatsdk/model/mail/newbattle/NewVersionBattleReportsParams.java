package com.chatsdk.model.mail.newbattle;

import java.util.List;

public class NewVersionBattleReportsParams
{

	private String	winnerUid;
	private int 	index;

	private List<NewVersionOneRoundReportParams> report;

	public List<NewVersionOneRoundReportParams> getReport() {
		return report;
	}

	public void setReport(List<NewVersionOneRoundReportParams> report) {
		this.report = report;
	}

	public String getWinnerUid() {
		return winnerUid;
	}

	public void setWinnerUid(String winnerUid) {
		this.winnerUid = winnerUid;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
