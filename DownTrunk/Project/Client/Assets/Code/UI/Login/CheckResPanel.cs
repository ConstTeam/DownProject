using UnityEngine;
using UnityEngine.UI;
using MS;

namespace MS
{
	public class CheckResPanel : MonoBehaviour
	{
		public Text				CheckText;

		private GameObject		_gameObject;
		private LoadIpAndConfig	_loadIpAndConfig;

		void Awake()
		{
			_gameObject					= gameObject;
			_loadIpAndConfig			= _gameObject.AddComponent<LoadIpAndConfig>();
			_loadIpAndConfig.CheckState	= SetCheckStateText;
		}

		void Start()
		{
			LoadConfigIp();
		}

		private void LoadConfigIp()
		{
			_loadIpAndConfig.LoadConfigIp(LoadConfigIpCallback);
		}

		public void LoadConfigIpCallback()
		{
			LoginPanel.GetInst().OnConfigLoadEnd();
			ServerListPanel.GetInst().SetServerInfo();
			_gameObject.SetActive(false);

			ApplicationEntry.GetInst().OnConfigLoadEnd();
		}

		private void SetCheckStateText(string text)
		{
			CheckText.text = text;
		}
	}
}
