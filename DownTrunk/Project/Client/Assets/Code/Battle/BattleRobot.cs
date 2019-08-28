using UnityEngine;

namespace MS
{
	public class BattleRobot : MonoBehaviour
	{
		private int oldPlatIndex = 0;
		private int oldPlatTimes = 0;

		public void RobotControl(BattleHero hero)
		{
			if(hero.CurPlat == null)
				return;

			if(hero.CurPlat.Index == oldPlatIndex)
				++oldPlatTimes;
			else
			{
				oldPlatIndex = hero.CurPlat.Index;
				oldPlatTimes = 0;
			}

			if(oldPlatTimes < Random.Range(5, 35))
				hero.Idle();
			else
			{
				BattlePlat nextPlat = BattleManager.GetInst().GetPlat(hero.PlayerId, hero.CurPlat.Index + 1);
				if(nextPlat != null)
				{
					if(nextPlat.Type == 5)
						nextPlat = BattleManager.GetInst().GetPlat(hero.PlayerId, hero.CurPlat.Index + 2);

					if(nextPlat.Y + BattleManager.GetInst().GetFieldPos(hero.PlayerId) < -4.5f)
						hero.Idle();
					else
					{
						if(hero.m_Transform.localPosition.x > nextPlat.X + 1.1f)
							hero.RunLeft();
						else if(hero.m_Transform.localPosition.x < nextPlat.X - 1.1f)
							hero.RunRight();
						else
						{
							if(nextPlat.X > hero.CurPlat.X)
								hero.RunRight();
							else
								hero.RunLeft();
						}
					}
				}
			}
		}
	}
}
