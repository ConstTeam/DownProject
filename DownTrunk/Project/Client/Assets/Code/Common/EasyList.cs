using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class EasyList<T> where T : MonoBehaviour
	{
		private List<T> _pool = new List<T>();
		private string _path;

		public EasyList(string path)
		{
			_path = path;
		}

		public T Show()
		{
			GameObject go;
			for(int i = 0; i < _pool.Count; ++i)
			{
				go = _pool[i].gameObject;
				if(!go.activeSelf)
				{
					go.SetActive(true);
					return _pool[i];
				}
			}

			T ret = ResourceLoader.LoadAssetAndInstantiate(_path).GetComponent<T>();
			_pool.Add(ret);
			return ret;
		}

		public void HideAll()
		{
			for(int i = 0; i < _pool.Count; ++i)
			{
				_pool[i].gameObject.SetActive(false);
			}
		}
	}
}
