package com.chatsdk.model.mail.fbscoutreport;

import java.util.List;

public class FBMemberInfoParams
{

	private int	total;//可参与防守的总数 不一定有


	public List<FBMemberArrayDetailParams> getMemberArray() {
		return memberArray;
	}

	public void setMemberArray(List<FBMemberArrayDetailParams> memberArray) {
		this.memberArray = memberArray;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	/**  // 每一排兵
 	*  泛型为 FBDetectFormationParams

 	*/

	private List<FBMemberArrayDetailParams> memberArray;
}
