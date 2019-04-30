using UnityEngine;

namespace MS
{
	public class BattleField : MonoBehaviour
	{
		public Transform Background;
		public DisappearTrigger DisTrigger;

		private Transform _transform;
		private Vector3 _tempPos = new Vector3();

		private void Awake()
		{
			_transform = transform;
			DisTrigger.TriggerCbFunc = RemovePlat;
		}

		public void SetPos(float y)
		{
			_tempPos.y = y;
			Background.localPosition = _tempPos;
		}

		public void RemovePlat(PlatBase plat)
		{
			plat.m_Transform.SetParent(BattleManager.GetInst().BattlePoorTran);
			plat.m_Transform.localPosition = PositionMgr.vecHidePos;
			ResourceMgr.PushBox(plat.Type, plat);
			int newIndex = plat.Index + 30;
			if(newIndex < BattleManager.GetInst().Stairs)
				AddPlat(plat.Index + 30);
		}

		public void AddPlat(int index)
		{
			BattleFieldData field = BattleManager.GetInst().GetFieldData(index);
			PlatBase plat = ResourceMgr.PopBox(field.Type);
			plat.Index = index;
			plat.m_Transform.SetParent(Background);
			plat.m_Transform.localPosition = new Vector3(field.X, field.Y, 0f);
		}

		public void Load()
		{
			for(int i = 0; i < 30; ++i)
			{
				AddPlat(i);
			}
		}
	}
}
