using System.Collections;

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
    #if CHANNEL_FACEBOOK
        sdkInterface = new FacebookPlatform();
    #else
        sdkInterface = new SDKInterfaceAndroid();
    #endif
#else
		ApplicationConst.bDynamicRes = false;
		sdkInterface = new SDKInterfaceBase(string.Empty);
#endif
    }
}
