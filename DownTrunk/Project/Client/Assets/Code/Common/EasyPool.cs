using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class EasyPool<T> where T : MonoBehaviour
	{
		private Stack<T> _pool = new Stack<T>();
		private string _path;

		public EasyPool(string path)
		{
			_path = path;
		}

		public T Pop()
		{
			T ret;
			if(_pool.Count <= 0)
				ret = ResourceLoader.LoadAssetAndInstantiate(_path).GetComponent<T>();
			else
				ret = _pool.Pop();

			return ret;
		}

		public void Push(T item)
		{
			_pool.Push(item);
		}
	}
}
