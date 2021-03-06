using MS;
using System.Collections;
using UnityEngine;

public class ApplicationEntry : MonoBehaviour
{
	public Transform			uiRoot;
	public GameObject			Communicate;

	private GameObject			_gameObject;
	private ApplicationConst	_appConst;
	
	private static ApplicationEntry _inst = null;
	static public ApplicationEntry GetInst()
	{
		return _inst;
	}

	void OnDestroy()
	{
		_inst = null;
    }

	private void Awake()
	{
		Application.targetFrameRate	= 45;
		Application.runInBackground	= true;
		Input.multiTouchEnabled		= false;
		Screen.sleepTimeout			= SleepTimeout.NeverSleep;
		
		_inst		= this;
		_gameObject	= gameObject;
		_appConst	= _gameObject.AddComponent<ApplicationConst>();

		_gameObject.AddComponent<InputManager>();
		_gameObject.AddComponent<SocketHandler>();
		_gameObject.AddComponent<ServiceManager>();

		PlatformSet();
		SocketHandler.GetInst().Init(2);
	}

	private void Start()
	{
		PlatformBase.sdkInterface.OnInit();
		ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Loading/LoadingPanel",	uiRoot);
		ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Common/Connecting",	uiRoot);
		ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Common/GMPanel",		uiRoot);
		ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Common/ConsolePanel",	uiRoot);
		ResourceLoader.LoadAssetAndInstantiate("PrefabUI/Common/BorderPanel",	uiRoot);

		SceneLoader.ToLoginScene(false);
	}

	public void OnConfigLoadEnd()
	{
		_appConst.OnConfigLoadEnd();
		ResourceMgr.Init();
	}

	public static void HandleExit()
	{
		if(!PlatformBase.sdkInterface.CloseApp())
			Application.Quit();
	}

	public static void ToLoginScene()
	{   
		Connecting.GetInst().ForceHide();
		Time.timeScale = 1;
		SocketHandler.GetInst().LongClose(0);
		SocketHandler.GetInst().LongClose(1);
		ClearCache();
		SceneLoader.ToLoginScene(true);
	}

	private static void ClearCache()
	{

    }

	private void PlatformSet()
	{
#if UNITY_IOS
		Communicate.AddComponent<CommunicateWithOC>();
#elif UNITY_ANDROID
		Communicate.AddComponent<CommunicateWithJava>();
#endif
        PlatformBase.Init();
    }

	private void OnApplicationPause(bool pause)
	{
		BattleManager mgr = BattleManager.GetInst();
		if(mgr != null && mgr.IsBattleRun && pause)
		{
			BattleManager.GetInst().DisableHero(PlayerData.PlayerId);
			CommonCommand.ExecuteLongBattle(Client2ServerList.GetInst().C2S_BATTLE_HERO_FAILED, new ArrayList() { PlayerData.PlayerId });
		}
	}
}
