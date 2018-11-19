package com.chatsdk.model.mail.detectreport;

import java.util.List;

public class FortParams
{
	private int					total;
	private boolean				about;
	private List<ArmsParams>	forts;

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

	public List<ArmsParams> getForts()
	{
		return forts;
	}

	public void setForts(List<ArmsParams> forts)
	{
		this.forts = forts;
	}
}
