using System.Collections;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class MainPanel : MonoBehaviour
	{
		public Button PVPBtn;

		private void Awake()
		{
			PVPBtn.onClick.AddListener(OnClickPVP);
		}

		private void OnClickPVP()
		{
			CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_PVP_REQUEST, new ArrayList() { 565 });
		}
	}
}
