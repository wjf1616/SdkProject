package com.chatsdk.model.mail.updatedata;

import java.util.List;

public class MailUpdateData
{
	private List<String>		delete;
	private List<UpdateParam>	update;

	public List<String> getDelete()
	{
		return delete;
	}

	public void setDelete(List<String> delete)
	{
		this.delete = delete;
	}

	public List<UpdateParam> getUpdate()
	{
		return update;
	}

	public void setUpdate(List<UpdateParam> update)
	{
		this.update = update;
	}

}
