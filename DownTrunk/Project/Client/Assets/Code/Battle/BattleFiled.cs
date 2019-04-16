using UnityEngine;

namespace MS
{
	public class BattleFiled : MonoBehaviour
	{
		private Transform _transform;

		private Object[] _boxes = new Object[3];

		private void Awake()
		{
			_transform = transform;
			_boxes[0] = ResourceLoader.LoadAssets("Prefab/BoxR");
			_boxes[1] = ResourceLoader.LoadAssets("Prefab/BoxG");
			_boxes[2] = ResourceLoader.LoadAssets("Prefab/BoxY");

			Load();
		}

		private void Update()
		{
			_transform.localPosition += Vector3.up * 0.02f;
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
			for(int i = 0; i < 100; ++i)
			{
				posX = rand.Next(-5, 6) / 2f;
				posY = lastY - rand.Next(2, 7) / 2f;
				lastY = posY;
				index = rand.Next(0, 3);

				go = Instantiate(_boxes[index]) as GameObject;
				tran = go.transform;
				tran.SetParent(_transform);
				tran.localPosition = new Vector3(posX, posY, 0f);
			}
		}
	}
}
