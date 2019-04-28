package db.module.card;

/**
 * mysql������Ϣ����ģ��
 *
 */
public class CardGroup {

	/* ---------------- ���� --------------- */
	/** ����Id */
	private int groupId;
	/** ����ICON */
	private int icon;
	/** �������� */
	private String name;
	/** ����Ԫ��1 */
	private int element1;
	/** ����Ԫ��2 */
	private int element2;
	/** ����Ԫ��3 */
	private int element3;
	/** �Ƿ���� */ 
	private boolean enable;
	
	public int getGroupId() {
		return groupId;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public int getIcon() {
		return icon;
	}
	
	public void setIcon(int icon) {
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getElement1() {
		return element1;
	}
	
	public void setElement1(int element) {
		this.element1 = element;
	}
	
	public int getElement2() {
		return element2;
	}
	
	public void setElement2(int element) {
		this.element2 = element;
	}
	
	public int getElement3() {
		return element3;
	}
	
	public void setElement3(int element) {
		this.element3 = element;
	}
	
	public boolean getEnable() {
		return enable;
	}
	
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

}
