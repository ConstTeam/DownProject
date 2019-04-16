using UnityEngine;
using UnityEngine.UI;
using System.Collections.Generic;
using MS;

public class ServerListPanel : MonoBehaviour
{
	public Button			closeBtn;
	public Transform		midGrid;
	public ServerListItem	m_lastLoginItem;
	public Text				m_curServerGroup;

	private GameObject		_gameObject;
	private Object			_listItem;

	private static ServerListPanel _inst;
	public static ServerListPanel GetInst()
	{
		return _inst;
	}

	void OnDestroy()
	{
		_inst = null;
	}

	void Awake()
	{
		_inst = this;
		_gameObject = gameObject;
		closeBtn.onClick.AddListener(ClosePanel);
		_listItem = ResourceLoader.LoadAssets("PrefabUI/Login/ServerListItem");
	}

	private void Start()
	{
		ClosePanel();
	}

	public void OpenPanel()
	{
		_gameObject.SetActive(true);
	}

	public void ClosePanel()
	{
		_gameObject.SetActive(false);
	}

	public void SetServerInfo()
	{
		List<string[]> list = IpData.GetServerList();
		SetLastServer(IpData.GetCurServerData());
		SetServerList(list);
	}
	
	private void SetLastServer(string[] firstServer)
	{
		string[] lastinfo = IpData.GetLastServerData();
		if (null == lastinfo || lastinfo.Length <= 0)
			lastinfo = firstServer;

		m_lastLoginItem.SetInfo(-1, lastinfo[0], lastinfo[3]);
	}

	public void SetServerList(List<string[]> list)
	{
		for (int i = list.Count - 1; i >= 0; --i)
		{
			GameObject go = Instantiate(_listItem) as GameObject;
			go.transform.SetParent(midGrid);
			go.transform.localScale = Vector3.one;
			ServerListItem item = go.GetComponent<ServerListItem>();
			item.SetInfo(i, list[i][0], list[i][3]);
		}
	}
}
