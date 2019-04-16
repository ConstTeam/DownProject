using MS;
using System.Collections;
using UnityEngine;
using UnityEngine.UI;

public class LoginPanel : MonoBehaviour
{
	public Button		loginBtn;
	public Button		curServerBtn;
	public Button		changeBtn;
	public Text			curServerIndexLabel;
	public Text			curServerNameLabel;
	public Text			changeLabel;
	public Text			versionLabel;
	public Text			checkInfoLabel;
	public InputField	accountInput;
	public InputField	passwordInput;
	public GameObject	accountPanel;
	public GameObject	hideNode;
    public GameObject   wxNode;
    public Button       wxBtn;
	
	private ArrayList	_loginParam = null;

	private static LoginPanel _inst;
	public static LoginPanel GetInst()
	{
		return _inst;
	}

	private void OnDestroy()
	{
		_inst = null;
	}

	private void Awake()
	{
		_inst = this;
		loginBtn.onClick.AddListener(OnLogin);
		curServerBtn.onClick.AddListener(OnShowServerList);
		changeBtn.onClick.AddListener(OnShowAccount);
		wxBtn.onClick.AddListener(OnLogin);

		accountPanel.SetActive(false);
		wxNode.SetActive(false);
		hideNode.SetActive(false);
#if PLATFORM_IOS_SDK || PLATFORM_ANDROID_SDK
		changeBtn.gameObject.SetActive(false);
        wxNode.SetActive(true);
        hideNode.SetActive(false);
#endif
    }

    private void Start()
	{
		versionLabel.text = string.Format("v {0}", ApplicationConst.BundleVersion);
		accountInput.text = PlayerPrefs.HasKey("LoginAccount") ? PlayerPrefs.GetString("LoginAccount") : "";
		passwordInput.text = PlayerPrefs.HasKey("LoginPassword") ? PlayerPrefs.GetString("LoginPassword") : "";
	}

	private void OnLogin()
	{
        if (!PlatformBase.sdkInterface.OnLogin())
            OnDefaultLogin();
    }

    private void OnDefaultLogin()
	{
		string account = accountInput.text;
		if(account == string.Empty)
		{
			if(!accountPanel.activeSelf)
				accountPanel.SetActive(true);
			else
				MsgBoxPanel.ShowMsgBox(string.Empty, ConfigData.GetStaticText("15001"), 1);
			
			return;
		}

		RoleData.Account = account;
		RoleData.Token	= passwordInput.text;
		SocketHandler.GetInst().LongConnect(0);
	}

	public void OnPlatformLogin()
	{
        _loginParam = new ArrayList()
		{
			ApplicationConst.PlatformID,
			ApplicationConst.ChannelID,
			DeviceInfo.GetDeviceInfo(),
			System.Guid.NewGuid().ToString(),
			SystemInfo.deviceUniqueIdentifier,
			RoleData.Account,
			RoleData.Token
		};

		CommonCommand.ExecuteLongMain(Client2ServerList.GetInst().C2S_LOGIN_LONG, _loginParam);
	}

	private void OnShowServerList()
	{
		ServerListPanel.GetInst().OpenPanel();
	}

	private void OnShowAccount()
	{
		accountPanel.SetActive(!accountPanel.activeSelf);
	}

	public void OnConfigLoadEnd()
	{
		SetCurServer();
#if PLATFORM_IOS_SDK || PLATFORM_ANDROID_SDK
        wxNode.SetActive(true);
        hideNode.SetActive(false);
#else
        wxNode.SetActive(false);
        hideNode.SetActive(true);
#endif
        checkInfoLabel.text = string.Format(ConfigData.GetValue("InitValues_Common", "CHECK_INFO", "Value"), ApplicationConst.BundleVersion);

		if(IpData.GetServerCount() == 1)
			curServerBtn.gameObject.SetActive(false);
	}

	public void SetCurServer()
	{
		string[] info = IpData.GetCurServerData();
		curServerIndexLabel.text	= info[0];
		curServerNameLabel.text		= info[3];
	}

	public void SaveAccount()
	{
		PlayerPrefs.SetString("LoginAccount", accountInput.text);
		PlayerPrefs.SetString("LoginPassword", passwordInput.text);
		IpData.WriteLastServerIndex();
	}
}
