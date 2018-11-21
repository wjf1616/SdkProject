package com.chatsdk.model.mail.fbscoutreport;

import java.util.List;

public class FBUserArmyAllParams
{
	private int					total;//可参与防守的总数 不一定有


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

	/**  // //编队信息 不一定有
 *  泛型为 FBDetectFormationParams

 */
	private List<FBDetectFormationParams>	formationArray;
}
