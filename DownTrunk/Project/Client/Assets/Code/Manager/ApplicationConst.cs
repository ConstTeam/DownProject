using System.Collections;
using UnityEngine;
using MS;

public class ApplicationConst : MonoBehaviour
{
	public static bool			bDynamicRes			= true;
	public static bool			bGM;
	public static bool			bAppRunning			= true;
	public static string		sSvnVersion			= "";
	public static string		sAccessToken;
	public static string		sServerId;
	public static string		sServerStartTime	= "";

	public static Hashtable		dictStaticInfo;
	public static Hashtable		dictStaticText;

	public static Rect			sceneCamRect;
	public static float			matchWidthOrHeight;

	public static int			iPlatTypeCount		= 6;

    public static string BundleVersion
	{
		get{ return dictStaticInfo["BundleVer"] as string; }
	}

	public static string SvnVersion
	{
		get{ return dictStaticInfo["SvnVer"] as string; }
	}

	public static string PlatformID
	{
		get{ return dictStaticInfo["PlatformID"] as string; }
	}

	public static string ChannelID
	{
		get{ return dictStaticInfo["ChannelID"] as string; }
	}

	void Awake()
	{
		TextAsset text = Resources.Load<TextAsset>("Text/StaticInfo");
		dictStaticInfo = MiniJSON.jsonDecode(text.text) as Hashtable;

		text = Resources.Load<TextAsset>("Text/StaticText");
		dictStaticText = MiniJSON.jsonDecode(text.text) as Hashtable;

		SetSceneCamRect();
	}

	public void OnConfigLoadEnd()
	{

    }

	private void SetSceneCamRect()
	{
		float ratio = (1920f * Screen.height) / (1080f * Screen.width);
		if(ratio < 1f)
		{
			ratio = 1f;
			matchWidthOrHeight = 1f;
		}
		else
		{
			ratio = 1f/ratio;
			matchWidthOrHeight = 0f;
		}
		sceneCamRect = new Rect(0f, (1f - ratio) / 2f, 1f, ratio);
	}
}
