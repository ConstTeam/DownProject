namespace MS
{
	public struct BattleFieldData
	{
		public float X	{ get; set; }
		public float Y	{ get; set; }
		public int Type	{ get; set; }

		public BattleFieldData(float x, float y, int type)
		{
			X = x;
			Y = y;
			Type = type;
		}
	}
}
