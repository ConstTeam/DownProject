using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class ResourceMgr
	{
		private static int _iPlatTypeCount = 4;
		private static Object[] _boxes = new Object[_iPlatTypeCount];
		private static Dictionary<int, Stack<PlatBase>> _dicBoxes = new Dictionary<int, Stack<PlatBase>>();

		public static void Init()
		{
			for(int i = 0; i < _iPlatTypeCount; ++i)
			{
				_boxes[i] = ResourceLoader.LoadAssets(string.Format("Prefab/Plat{0}", i));
			}
		}

		public static PlatBase PopBox(int type)
		{
			if(!_dicBoxes.ContainsKey(type))
				_dicBoxes.Add(type, new Stack<PlatBase>());

			if(_dicBoxes[type].Count > 0)
				return _dicBoxes[type].Pop();

			PlatBase plat = (Object.Instantiate(_boxes[type]) as GameObject).GetComponent<PlatBase>();
			plat.Type = type;
			return plat;
		}

		public static void PushBox(int type, PlatBase plat)
		{
			if(!_dicBoxes[type].Contains(plat))
				_dicBoxes[type].Push(plat);
		}
	}
}
