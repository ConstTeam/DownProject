using UnityEngine;

namespace MS
{
	public class DeviceInfo
	{
		public static string GetDeviceInfo()
		{
			string idfa = "-1", idfv = "-1";
			PlatformBase.sdkInterface.SetIdfa(ref idfa, ref idfv);

			string devicesInfo = string.Format("os,{0}|dm,{1}|dn,{2}|dt,{3}|sms,{4}|gdn,{5}|gms,{6}|pf,{7}|sl,{8}|idfa,{9}|idfv,{10}",
											   SystemInfo.operatingSystem,
											   SystemInfo.deviceModel,
											   SystemInfo.deviceName,
											   SystemInfo.deviceType,
											   SystemInfo.systemMemorySize,
											   SystemInfo.graphicsDeviceName,
											   SystemInfo.graphicsMemorySize,
											   Application.platform,
											   Application.systemLanguage,
											   idfa,
											   idfv);

			return devicesInfo;
		}
	}
}
