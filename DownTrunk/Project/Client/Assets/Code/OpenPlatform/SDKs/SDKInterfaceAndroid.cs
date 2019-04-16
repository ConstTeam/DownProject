#if PLATFORM_ANDROID_SDK
using MS;
using UnityEngine;

public class SDKInterfaceAndroid : SDKInterfaceBase
{

    public SDKInterfaceAndroid() : base(""){}

    public override void OnInit()
	{
		CommunicateWithJava.GetInst().m_AJObj.Call("onInit");
		TalkingDataGA.OnStart(ApplicationConst.talkingDataAppId, ApplicationConst.ChannelID);
	}

	public override void OnBuyEvent(string sGameTrade, SetBuyEventInfoCallback cb)
	{
		string sDiamond = "0";
		int iPrice = 0;
		cb(sGameTrade, ref sDiamond, ref iPrice);
		CommunicateWithJava.GetInst().m_AJObj.Call("onBuyEvent", sDiamond, iPrice);
	}
	
	public override bool OnExit()
	{
		return CommunicateWithJava.GetInst().m_AJObj.Call<bool>("onExit");
	}

	public override bool OnLogout()
	{
		CommunicateWithJava.GetInst().m_AJObj.Call("onLogout");
		return true;
	}

	public override bool OnLogin()
	{
	//	Connecting.GetInst().Show();
        CommunicateWithJava.GetInst().m_AJObj.Call("onLogin");
		return true;
	}

	public override bool OnPay(int id, string sIAPID, int iPrice, string sName, int iCount,
								string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
								string sServerId, string sServerName, string sZoneId, string sGoldName, string sSingleName)
	{
		CommunicateWithJava.GetInst().m_AJObj.Call("onPay", id, sIAPID, iPrice, sName, iCount,
													sAccount, iRoleId, sRoleName, iRoleLv, iVipLv, iGoldNum, sFamilyName,
													sServerId, sServerName, sZoneId, sGoldName, sSingleName, "", System.Guid.NewGuid().ToString());
		return true;
	}

	//----------------------------------------------------------------------------------
	public override void OnCreateRoleEvent()
	{
		CommunicateWithJava.GetInst().m_AJObj.Call("onRoleCreate");
	}

	public override void OnLoginSuccessEvent(string sFlag,
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName)
	{
		CommunicateWithJava.GetInst().m_AJObj.Call("enterServer", sFlag,
													sAccount, iRoleId, sRoleName, iRoleLv, iVipLv, iGoldNum, sFamilyName,
													sServerId, sServerName, sZoneId, sZoneFormatName, sAccessToken, sAccountCreateTime, sRoleType, sNoName);
	}

	public override void OnCreateRoleSuccessEvent(
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName,
			long lServerTime)
	{
		CommunicateWithJava.GetInst().m_AJObj.Call("createRole", sAccount, iRoleId, sRoleName, iRoleLv, iVipLv, iGoldNum, sFamilyName,
													sServerId, sServerName, sZoneId, sZoneFormatName, sAccessToken, sAccountCreateTime, sRoleType, sNoName,
													lServerTime);
	}

	public override void OnLevelUpOrExpAddEvent(int iOldExp, int iRoleExp, int iOldLevel,
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName,
			long lServerTime)
	{
			CommunicateWithJava.GetInst().m_AJObj.Call("levelUp", iOldExp, iRoleExp, iOldLevel,
														sAccount, iRoleId, sRoleName, iRoleLv, iVipLv, iGoldNum, sFamilyName,
														sServerId, sServerName, sZoneId, sZoneFormatName, sAccessToken, sAccountCreateTime, sRoleType, sNoName,
														lServerTime);
	}
}
#endif
