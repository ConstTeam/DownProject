#if PLATFORM_IOS_SDK
using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;

public class SDKInterfaceIOS : SDKInterfaceBase
{
	[DllImport("__Internal")]
	public static extern string GetIAPKey();

	[DllImport("__Internal")]
	public static extern string GetIDFA();
	
	[DllImport("__Internal")]
	public static extern string GetIDFV();

	[DllImport("__Internal")]
	public static extern void Initialization();

	[DllImport("__Internal")]
	public static extern void OnShowLogin();
	
	[DllImport("__Internal")]
	public static extern void BuyProduct(string sIAP, string sOrder, int iPrice, int iCount, string sName, string sGoldName, string sUrl);

	[DllImport("__Internal")]
	public static extern void OnUserLogout();

	[DllImport("__Internal")]  
	public static extern void SetExtData(string id, string roldId, string roleName, int roleLevel, int zoneId, string zoneName, int balance, int vip, string partyName);


	public SDKInterfaceIOS():base(GetIAPKey()){}

	public override void SetIdfa(ref string idfa, ref string idfv)
	{
		idfa = GetIDFA();
		idfv = GetIDFV();
	}

	public override void OnInit(GameObject go)
	{
		Initialization();
	}

	public override bool OnLogin()
	{
		OnShowLogin();
		return true;
	}

	public override bool OnPay(int id, string sIAPID, int iPrice, string sName, int iCount,
								string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
								string sServerId, string sServerName, string sZoneId, string sGoldName, string sSingleName)
	{
		Connecting.GetInst().Show();
		string extInfo = string.Format("{0}|{1}|{2}|{3}", System.Guid.NewGuid(), sServerId, id, iRoleId);

		string sUrl = "";
		if("LJ" == m_sPlatformID)
			sUrl  = ConfigData.GetValue("InitValues_Common", "LJ_PAY_URL", "Value");

		BuyProduct(sIAPID, extInfo, iPrice, iCount, sName, sGoldName, sUrl);
		return true;
	}

	public override void OnLoginSuccessEvent
		(
			string sFlag,
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName
			)
	{
		SetExtData("enterServer", iRoleId.ToString(), sRoleName, iRoleLv, int.Parse(sZoneId), sServerName, iGoldNum, iVipLv, sFamilyName);	
	}
	
	public override void OnCreateRoleSuccessEvent
		(
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName,
			long lServerTime
			)
	{
		SetExtData("createRole", iRoleId.ToString(), sRoleName, iRoleLv, int.Parse(sZoneId), sServerName, iGoldNum, iVipLv, sFamilyName);
	}
	
	public override void OnLevelUpOrExpAddEvent
		(
			int iOldExp, int iRoleExp, int iOldLevel,
			string sAccount, int iRoleId, string sRoleName, int iRoleLv, int iVipLv, int iGoldNum, string sFamilyName,
			string sServerId, string sServerName, string sZoneId, string sZoneFormatName, string sAccessToken, string sAccountCreateTime, string sRoleType, string sNoName,
			long lServerTime
		)
	{
		if(iOldLevel < iRoleLv)
		{
			SetExtData("levelUp", iRoleId.ToString(), sRoleName, iRoleLv, int.Parse(sZoneId), sServerName, iGoldNum, iVipLv, sFamilyName);	
		}
	}
}
#endif
