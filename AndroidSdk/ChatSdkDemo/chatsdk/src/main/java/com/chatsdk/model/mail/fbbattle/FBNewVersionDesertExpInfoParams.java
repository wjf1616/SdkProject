package com.chatsdk.model.mail.fbbattle;

import java.util.List;

public class FBNewVersionDesertExpInfoParams
{

	private int	oldExp;  // //老的经验

	private int	addExp;  //加的经验

	private int	oldLv;  // 老的等级

	private int	oldDeductExp;  ///老的 负数经验值

	private int	lv;  //新等级

	public int getOldExp() {
		return oldExp;
	}

	public void setOldExp(int oldExp) {
		this.oldExp = oldExp;
	}

	public int getAddExp() {
		return addExp;
	}

	public void setAddExp(int addExp) {
		this.addExp = addExp;
	}

	public int getOldLv() {
		return oldLv;
	}

	public void setOldLv(int oldLv) {
		this.oldLv = oldLv;
	}

	public int getOldDeductExp() {
		return oldDeductExp;
	}

	public void setOldDeductExp(int oldDeductExp) {
		this.oldDeductExp = oldDeductExp;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public long getOldExpLimit() {
		return oldExpLimit;
	}

	public void setOldExpLimit(long oldExpLimit) {
		this.oldExpLimit = oldExpLimit;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public long getDeductExp() {
		return deductExp;
	}

	public void setDeductExp(long deductExp) {
		this.deductExp = deductExp;
	}

	public long getExpLimit() {
		return expLimit;
	}

	public void setExpLimit(long expLimit) {
		this.expLimit = expLimit;
	}

	private long	oldExpLimit;  // 老的 经验上限

	private long	exp;  //升级后的经验

	private long	deductExp;  // 新的 负数经验值

	private long	expLimit;  // /新的 经验上限
}
