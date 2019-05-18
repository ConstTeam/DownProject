using UnityEngine;

namespace MS
{
	public class BattleRoleSp : MonoBehaviour
	{
		public SpriteRenderer SpRenderer;
		public Sprite[] Sp;

		private void Awake()
		{
			SpRenderer = gameObject.GetComponent<SpriteRenderer>();
		}
	}
}
