using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class ResourceMgr
	{
		private static Object[] _boxes = new Object[3];
		private static Dictionary<int, Stack<PlatBase>> _dicBoxes = new Dictionary<int, Stack<PlatBase>>();

		public static void Init()
		{
			_boxes[0] = ResourceLoader.LoadAssets("Prefab/BoxR");
			_boxes[1] = ResourceLoader.LoadAssets("Prefab/BoxG");
			_boxes[2] = ResourceLoader.LoadAssets("Prefab/BoxY");
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
			_dicBoxes[type].Push(plat);
		}
	}
}
