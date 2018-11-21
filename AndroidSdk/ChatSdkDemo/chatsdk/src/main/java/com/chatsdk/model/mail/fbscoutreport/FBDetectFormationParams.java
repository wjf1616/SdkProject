package com.chatsdk.model.mail.fbscoutreport;

import java.util.List;

public class FBDetectFormationParams
{


	public List<FBRowArmyParams> getRowArray() {
		return rowArray;
	}

	public void setRowArray(List<FBRowArmyParams> rowArray) {
		this.rowArray = rowArray;
	}

	/**  // 每一排兵
	 *  泛型为 FBRowArmyParams
	 */
	private List<FBRowArmyParams> rowArray;


}
