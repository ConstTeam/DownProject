using UnityEngine;

namespace MS
{
	public class BattleField : MonoBehaviour
	{
		public Transform SpriteMask;
		public DisappearTrigger DisTrigger;
		public Transform Foreground;

		[HideInInspector]
		public Transform Background;

		public int SceneId { get; set; }

		private Transform _transform;
		private Material _matBg;
		private Vector3 _tempPos = new Vector3();

		private void Awake()
		{
			_transform = transform;
			
			DisTrigger.TriggerCbFunc = RemovePlat;
		}

		public void Load()
		{
			Background = ResourceLoader.LoadAssetAndInstantiate(string.Format("Prefab/Scene/{0}/Background", SceneId), SpriteMask).GetComponent<Transform>();
			_matBg = Background.GetComponent<SpriteRenderer>().material;
			for (int i = 0; i < 30; ++i)
			{
				AddPlat(i);
			}
		}

		public void SetPos(float y)
		{
			_tempPos.y = y;
			Foreground.localPosition = _tempPos;

			_tempPos.y = y / -30f;
			_matBg.mainTextureOffset = _tempPos;
		}

		public void RemovePlat(PlatBase plat)
		{
			plat.m_Transform.SetParent(BattleManager.GetInst().BattlePoorTran);
			plat.m_Transform.localPosition = PositionMgr.vecHidePos;
			ResourceMgr.PushBox(SceneId, plat.Type, plat);
			int newIndex = plat.Index + 30;
			if(newIndex < BattleManager.GetInst().Stairs)
				AddPlat(plat.Index + 30);
		}

		public void AddPlat(int index)
		{
			BattleFieldData field = BattleManager.GetInst().GetFieldData(index);
			PlatBase plat = ResourceMgr.PopBox(SceneId, field.Type);
			plat.Index = index;
			plat.m_Transform.SetParent(Foreground);
			plat.m_Transform.localPosition = new Vector3(field.X, field.Y, 0f);
		}
	}
}
