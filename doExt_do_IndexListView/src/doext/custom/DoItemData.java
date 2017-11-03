package doext.custom;

import org.json.JSONObject;

public class DoItemData {

	private int groupId;
	private JSONObject data;
	private boolean isFrist;
	private boolean isEnd;
	private boolean isGroup;
	private String groupName;
	private int groupPosition;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getGroupPosition() {
		return groupPosition;
	}

	public void setGroupPosition(int groupPosition) {
		this.groupPosition = groupPosition;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public JSONObject getData() {
		return data;
	}

	public void setData(JSONObject data) {
		this.data = data;
	}

	public boolean isEnd() {
		return isEnd;
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean isFrist() {
		return isFrist;
	}

	public void setFrist(boolean isFrist) {
		this.isFrist = isFrist;
	}

}
