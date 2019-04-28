using UnityEngine;

namespace MS
{
	public class BattleFieldM : BattleFieldBase
	{
		public bool IsRunning { get; set; }

		private float _t = 0f;
		private void Update()
		{
			if(IsRunning)
			{
				_t += Time.deltaTime;
				if(_t > 0.02f)
				{
					m_Transform.localPosition += Vector3.up * 0.05f;
					_t = 0f;
				}
			}
			
		}
	}
}
