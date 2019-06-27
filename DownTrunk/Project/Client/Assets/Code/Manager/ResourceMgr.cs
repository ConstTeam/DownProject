using System.Collections.Generic;
using UnityEngine;

namespace MS
{
	public class ResourceMgr
	{
		private static int _iSceneTypeCount	= 5;
		private static int _iItemTypeCount	= 12;
		private static int _iSkillTypeCount = 12;
		private static Object[,]	_boxes = new Object[_iSceneTypeCount, ApplicationConst.iPlatTypeCount];
		private static Object[]		_items = new Object[_iItemTypeCount];
		private static Object[]		_skills = new Object[_iSkillTypeCount];
		private static Dictionary<int, Dictionary<int, Stack<BattlePlat>>>	_dicBoxes	= new Dictionary<int, Dictionary<int, Stack<BattlePlat>>>();
		private static Dictionary<int, Stack<BattleItem>>					_dicItems	= new Dictionary<int, Stack<BattleItem>>();
		private static Dictionary<int, Stack<BattleSkillBtn>>				_dicSkills	= new Dictionary<int, Stack<BattleSkillBtn>>();

		public static void Init()
		{
			Clear();
			for(int i = 0; i < _iSceneTypeCount; ++i)
			{
				for(int j = 0; j < ApplicationConst.iPlatTypeCount; ++j)
					_boxes[i, j] = ResourceLoader.LoadAssets(string.Format("Prefab/Scene/{0}/Plat{1}", i, j));
			}
			for(int i = 0; i < _iItemTypeCount; ++i)
			{
				_items[i] = ResourceLoader.LoadAssets(string.Format("Prefab/Item/{0}", i));
			}
			for(int i = 0; i < _iSkillTypeCount; ++i)
			{
				_skills[i] = ResourceLoader.LoadAssets(string.Format("Prefab/SkillBtn/{0}", i));
			}
		}

		public static void Clear()
		{
			_dicBoxes.Clear();
			_dicItems.Clear();
			_dicSkills.Clear();
		}

		public static BattlePlat PopBox(int scene, int type)
		{
			if(!_dicBoxes.ContainsKey(scene))
				_dicBoxes.Add(scene, new Dictionary<int, Stack<BattlePlat>>());

			if(!_dicBoxes[scene].ContainsKey(type))
				_dicBoxes[scene].Add(type, new Stack<BattlePlat>());

			if(_dicBoxes[scene][type].Count > 0)
				return _dicBoxes[scene][type].Pop();

			BattlePlat plat = (Object.Instantiate(_boxes[scene, type]) as GameObject).GetComponent<BattlePlat>();
			plat.Type = type;
			return plat;
		}

		public static void PushBox(int scene, int type, BattlePlat plat)
		{
			if(!_dicBoxes[scene][type].Contains(plat))
			{
				plat.m_Transform.SetParent(BattleManager.GetInst().BattlePoorTran);
				plat.m_Transform.localPosition = PositionMgr.vecHidePos;
				_dicBoxes[scene][type].Push(plat);
			}
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

		public static void PushItem(BattleItem item)
		{
			int type = item.Type;
			if(!_dicItems[type].Contains(item))
			{
				item.m_Transform.SetParent(BattleManager.GetInst().BattlePoorTran);
				item.m_Transform.localPosition = PositionMgr.vecHidePos;
				_dicItems[type].Push(item);
			}
		}

		public static BattleSkillBtn PopSkill(int skillId)
		{
			if(!_dicSkills.ContainsKey(skillId))
				_dicSkills.Add(skillId, new Stack<BattleSkillBtn>());

			if(_dicSkills[skillId].Count > 0)
				return _dicSkills[skillId].Pop();

			BattleSkillBtn skill = (Object.Instantiate(_skills[skillId]) as GameObject).GetComponent<BattleSkillBtn>();
			return skill;
		}

		public static void PushSkill(int type, BattleSkillBtn skill)
		{
			if(!_dicSkills[type].Contains(skill))
			{
				skill.m_Transform.SetParent(BattleManager.GetInst().BattlePoorTran);
				skill.m_Transform.localPosition = PositionMgr.vecHidePos;
				skill.m_Transform.localScale = Vector3.one;
				_dicSkills[type].Push(skill);
			}
		}
	}
}
