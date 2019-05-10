namespace MS
{
	public class PlatformBase
	{
		public static SDKInterfaceBase sdkInterface;

		public static void Init()
		{
	#if PLATFORM_NONE
			sdkInterface = new SDKInterfaceBase(string.Empty);
	#elif PLATFORM_IOS_SDK
			sdkInterface = new SDKInterfaceIOS();
	#elif PLATFORM_ANDROID_SDK
			sdkInterface = new SDKInterfaceAndroid();
	#else
			ApplicationConst.bDynamicRes = false;
			sdkInterface = new SDKInterfaceBase(string.Empty);
	#endif
		}
	}
}
