using UnityEngine;

namespace MS
{
	public class InputManager : MonoBehaviour
	{
#if UNITY_IOS || UNITY_ANDROID
		private float fLastZ	= 0.0f;
		private float fCurZ		= 0.0f;
		private float fTempZ	= 0.0f;
#endif

		void Update()
		{
			if(Input.GetKeyDown(KeyCode.Escape))
			{
				if(!PlatformBase.sdkInterface.OnExit())
					MsgBoxPanel.ShowMsgBox(string.Empty, (string)ApplicationConst.dictStaticText["25"], 2, new MsgBoxPanel.MsgCallback(() => { ApplicationEntry.HandleExit(); }), null, "ExitGame");
			}
#if UNITY_IOS || UNITY_ANDROID
			else if(ApplicationConst.bGM)
			{
				fCurZ = Input.acceleration.z;
				if(fCurZ > 0f)
				{
					fTempZ = fCurZ - fLastZ;
					fLastZ = fCurZ;
					if(fTempZ > 0.8f && null != GMPanel.GetInst())
						GMPanel.GetInst().OpenPanel();
				}
			}
#elif UNITY_STANDALONE
			else if(ApplicationConst.bGM && Input.GetKeyDown(KeyCode.F12))
			{
				if(null != GMPanel.GetInst())
					GMPanel.GetInst().BeShowWnd();
			}

			if(BattleManager.GetInst() != null && BattleManager.GetInst().IsBattleRun)
			{
				if(Input.GetKey(KeyCode.A))
					BattleManager.GetInst().RunLeft(PlayerData.PlayerId);
				else if(Input.GetKey(KeyCode.D))
					BattleManager.GetInst().RunRight(PlayerData.PlayerId);
				else if(Input.GetKeyUp(KeyCode.A) || Input.GetKeyUp(KeyCode.D))
					BattleManager.GetInst().Idle(PlayerData.PlayerId);
			}
#endif
		}
	}
}
