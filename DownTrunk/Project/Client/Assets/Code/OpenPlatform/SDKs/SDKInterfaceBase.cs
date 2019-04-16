public delegate void SetBuyEventInfoCallback(string sGameTrade, ref string sDiamond, ref int iPrice);
public class SDKInterfaceBase
{
	public string m_sIAPKey = "";

	public SDKInterfaceBase(){}

	public SDKInterfaceBase(string iap)
	{
		m_sIAPKey = iap;
	}

	public virtual void OnInit(){}
	public virtual void OnSetCallback(){}
	public virtual void OnApplicationQuit(){}
	public virtual void SetIdfa(ref string idfa, ref string idfv){}
	public virtual void OnPaySuccess(string payResult){}
	public virtual void OnBuyEvent(string sGameTrade, SetBuyEventInfoCallback cb){}
	public virtual bool OnExit(){ return false; }
	public virtual bool CloseApp(){ return false; }
	public virtual bool OnLogout(){ return false; }
	public virtual bool OnLogin(){ return false; }

	public virtual bool OnPay(int id, string sIAPID, int iPrice, string sName, int iCount,
								string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
								string sServerId, string sServerName, string sZoneId, string sGoldName, string sSingleName)
	{ return false; }

	//----------------------------------------------------------------------------------
	public virtual void OnCreateRoleEvent(){}

	public virtual void OnLoginSuccessEvent(string sFlag,
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName)
	{}

	public virtual void OnCreateRoleSuccessEvent(
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName,
			long lServerTime)
	{}

	public virtual void OnLevelUpOrExpAddEvent(int iOldExp, int iRoleExp, int iOldLevel,
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName,
			long lServerTime)
	{}
	public virtual void OnSwitchAccount(){}
	public virtual void OnAccountTransfer(string sId, string sLevel){}
}
