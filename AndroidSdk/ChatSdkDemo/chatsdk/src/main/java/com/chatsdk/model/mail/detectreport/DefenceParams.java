package com.chatsdk.model.mail.detectreport;

import java.util.List;

public class DefenceParams
{
	private int					total;
	private boolean				about;
	private List<ArmsParams>	arms;
	private List<SCDefParams>	scDefMarchList;

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public boolean isAbout()
	{
		return about;
	}

	public void setAbout(boolean about)
	{
		this.about = about;
	}

	public List<ArmsParams> getArms()
	{
		return arms;
	}

	public void setArms(List<ArmsParams> arms)
	{
		this.arms = arms;
	}

	public List<SCDefParams> getScDefMarchList() {
		return scDefMarchList;
	}

	public void setScDefMarchList(List<SCDefParams> scDefMarchList) {
		this.scDefMarchList = scDefMarchList;
	}
}
