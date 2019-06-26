using UnityEngine;

namespace MS
{
	public class PositionMgr
	{
		public static Vector3 vecHidePos	= new Vector3(0, 1000, 0);
		public static Vector3 vecFieldPosM	= new Vector3(-4, 0, 0);
		public static Vector3 vecFieldPosE	= new Vector3(4, 0, 0);
		public static Vector3[] arrVecFieldPosE = new Vector3[4]{ new Vector3(2f, 2.45f, 0), new Vector3(6f, 2.45f, 0), new Vector3(2f, -2.4f, 0), new Vector3(6f, -2.4f, 0) };
	}
}
