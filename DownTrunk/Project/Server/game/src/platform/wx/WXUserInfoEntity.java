package platform.wx;

import java.util.List;

public class WXUserInfoEntity {

	/** �û��ı�ʶ���Ե�ǰ�������ʺ�Ψһ */
	private String openid;
	/** �û��ǳ� */
	private String nickname;
	/** �û��Ա�1Ϊ���ԣ�2ΪŮ�� */
	private int sex;
	/** �û�����������д��ʡ�� */
	private String province;
	/** �û�����������д�ĳ��� */
	private String city;
	/** ���ң����й�ΪCN */
	private String country;
	/** �û�ͷ�����һ����ֵ����������ͷ���С����0��46��64��96��132��ֵ��ѡ��0����640*640������ͷ�񣩣��û�û��ͷ��ʱ����Ϊ�� */
	private String headimgurl;
	/** �û���Ȩ��Ϣ��json���飬��΢���ֿ��û�Ϊ��chinaunicom�� */
	private List<String> privilege;
	/** �û�ͳһ��ʶ�����һ��΢�ſ���ƽ̨�ʺ��µ�Ӧ�ã�ͬһ�û���unionid��Ψһ�ġ� */
	private String unionid;
	
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	public List<String> getPrivilege() {
		return privilege;
	}
	public void setPrivilege(List<String> privilege) {
		this.privilege = privilege;
	}
	public String getUnionid() {
		return unionid;
	}
	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	
}
