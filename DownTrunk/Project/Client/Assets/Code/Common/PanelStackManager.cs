using System.Collections.Generic;
using UnityEngine;

public class PanelStackManager
{
	private static List<GameObject> _goList = new List<GameObject>();

	public static void SetActive(GameObject go, bool active)
	{
		go.SetActive(active);
		if(active)
			PushPanel(go);
		else
			PopPanel(go);
	}

	private static void PushPanel(GameObject go)
	{
		int count = _goList.Count;
		for(int i = 0; i < count; ++i)
		{
			if(_goList[i] == go)
				return;
		}
		if(count > 0)
			_goList[_goList.Count - 1].SetActive(false);
		_goList.Add(go);
	}

	private static void PopPanel(GameObject go)
	{
		int count = _goList.Count;
		for(int i = 0; i < count; ++i)
		{
			if(_goList[i] == go)
			{
				if(i == count - 1)
					_goList[i - 1].SetActive(true);
				_goList.RemoveAt(i);
				break;
			}
		}
	}

	public static void ClearStack()
	{
		_goList.Clear();
	}

	public static void PopAll()
	{
		int count = _goList.Count;
		for(int i = count - 1; i > 0; --i)
		{
			_goList[i - 1].SetActive(true);
			_goList[i].SetActive(false);
			_goList.RemoveAt(i);
		}
	}
}
