using UnityEngine;
using UnityEngine.UI;
using MS;

public class ServerListItem : MonoBehaviour
{
	public Button	itemBtn;
	public Text		indexLabel;
	public Text		nameLabel;
	private int		_index;

    void Awake()
	{ 
        itemBtn.onClick.AddListener(OnClickServer);        
	}

	public void SetInfo(int index, string sIndex, string sName)
	{
		_index			= index;
		indexLabel.text	= sIndex;
		nameLabel.text	= sName;
	}

	private void OnClickServer()
	{
        if(_index < 0) return;
		IpData.SetCurServerData(_index);
		LoginPanel.GetInst().SetCurServer();
		ServerListPanel.GetInst().ClosePanel();
	}
}
