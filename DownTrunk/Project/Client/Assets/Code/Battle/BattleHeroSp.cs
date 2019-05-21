using UnityEngine;

namespace MS
{
	public class BattleHeroSp : MonoBehaviour
	{
		public SpriteRenderer SpRenderer;
		public Sprite[] Sp;

		private void Awake()
		{
			SpRenderer = gameObject.GetComponent<SpriteRenderer>();
		}
	}
}
