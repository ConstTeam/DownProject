using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class ResourceMgr
	{
		private static int _iSceneTypeCount = 1;
		private static Object[,] _boxes = new Object[_iSceneTypeCount, ApplicationConst.iPlatTypeCount];
		private static Dictionary<int, Dictionary<int, Stack<PlatBase>>> _dicBoxes = new Dictionary<int, Dictionary<int, Stack<PlatBase>>>();

		public static void Init()
		{
			for(int i = 0; i < _iSceneTypeCount; ++i)
			{
				for(int j = 0; j < ApplicationConst.iPlatTypeCount; ++j)
					_boxes[i, j] = ResourceLoader.LoadAssets(string.Format("Prefab/Scene/{0}/Plat{1}", i, j));
			}
		}

		public static PlatBase PopBox(int scene, int type)
		{
			if(!_dicBoxes.ContainsKey(scene))
				_dicBoxes.Add(scene, new Dictionary<int, Stack<PlatBase>>());

			if(!_dicBoxes[scene].ContainsKey(type))
				_dicBoxes[scene].Add(type, new Stack<PlatBase>());

			if(_dicBoxes[scene][type].Count > 0)
				return _dicBoxes[scene][type].Pop();

			PlatBase plat = (Object.Instantiate(_boxes[scene, type]) as GameObject).GetComponent<PlatBase>();
			plat.Type = type;
			return plat;
		}

		public static void PushBox(int scene, int type, PlatBase plat)
		{
			if(!_dicBoxes[scene][type].Contains(plat))
				_dicBoxes[scene][type].Push(plat);
		}
	}
}
