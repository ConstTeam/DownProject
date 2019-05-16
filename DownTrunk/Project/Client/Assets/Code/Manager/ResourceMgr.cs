using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class ResourceMgr
	{
		private static int _iSceneTypeCount	= 2;
		private static int _iItemTypeCount	= 3;
		private static Object[,]	_boxes = new Object[_iSceneTypeCount, ApplicationConst.iPlatTypeCount];
		private static Object[]		_items = new Object[_iItemTypeCount];
		private static Dictionary<int, Dictionary<int, Stack<PlatBase>>> _dicBoxes = new Dictionary<int, Dictionary<int, Stack<PlatBase>>>();
		private static Dictionary<int, Stack<BattleItem>> _dicItems = new Dictionary<int, Stack<BattleItem>>();

		public static void Init()
		{
			for(int i = 0; i < _iSceneTypeCount; ++i)
			{
				for(int j = 0; j < ApplicationConst.iPlatTypeCount; ++j)
					_boxes[i, j] = ResourceLoader.LoadAssets(string.Format("Prefab/Scene/{0}/Plat{1}", i, j));
			}

			for(int i = 0; i < _iItemTypeCount; ++i)
			{
				_items[i] = ResourceLoader.LoadAssets(string.Format("Prefab/Item/Item{0}", i + 1));
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

		public static BattleItem PopItem(int type)
		{
			if(!_dicItems.ContainsKey(type))
				_dicItems.Add(type, new Stack<BattleItem>());

			if(_dicItems[type].Count > 0)
				return _dicItems[type].Pop();

			BattleItem item = (Object.Instantiate(_items[type]) as GameObject).GetComponent<BattleItem>();
			return item;
		}

		public static void PushItem(int type, BattleItem item)
		{
			if(!_dicItems[type].Contains(item))
				_dicItems[type].Push(item);
		}
	}
}
