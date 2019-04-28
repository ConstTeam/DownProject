using UnityEngine;

namespace MS
{
	public class BattleFieldBase : MonoBehaviour
	{
		public Transform m_Transform;
		protected Object[] _boxes = new Object[3];

		private void Awake()
		{
			m_Transform = transform;
			_boxes[0] = ResourceLoader.LoadAssets("Prefab/BoxR");
			_boxes[1] = ResourceLoader.LoadAssets("Prefab/BoxG");
			_boxes[2] = ResourceLoader.LoadAssets("Prefab/BoxY");
		}

		private Vector3 _tempPos = new Vector3();
		public void SetPos(float y)
		{
			_tempPos.y = y;
			m_Transform.localPosition = _tempPos;
		}

		public void Load()
		{
			int index;
			float posX;
			float posY;
			float lastY = 0;
			GameObject go;
			Transform tran;

			System.Random rand = new System.Random(123);
			for(int i = 0; i < 50; ++i)
			{
				posX = rand.Next(-5, 6) / 2f;
				posY = lastY - rand.Next(2, 7) / 2f;
				lastY = posY;
				index = rand.Next(0, 3);

				go = Instantiate(_boxes[index]) as GameObject;
				tran = go.transform;
				tran.SetParent(m_Transform);
				tran.localPosition = new Vector3(posX, posY, 0f);
			}
		}
	}
}
