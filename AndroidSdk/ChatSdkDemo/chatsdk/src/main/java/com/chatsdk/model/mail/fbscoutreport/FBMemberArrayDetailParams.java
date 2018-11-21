package com.chatsdk.model.mail.fbscoutreport;

import java.util.List;
import com.chatsdk.model.mail.detectreport.UserInfoParams;

public class FBMemberArrayDetailParams
{
	private int					total; //可参与防守的总数 不一定有

	private UserInfoParams userInfo;

	public List<FBDetectFormationParams> getFormationArray() {
		return formationArray;
	}

	public void setFormationArray(List<FBDetectFormationParams> formationArray) {
		this.formationArray = formationArray;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public UserInfoParams getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfoParams userInfo) {
		this.userInfo = userInfo;
	}

	/**  //  //编队信息 不一定有
 	*  泛型为 FBDetectFormationParams
	 */

	private List<FBDetectFormationParams>	formationArray;
}
