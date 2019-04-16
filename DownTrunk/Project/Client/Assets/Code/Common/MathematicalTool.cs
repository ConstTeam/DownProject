using System;

public class MathematicalTool
{
	public static float Round (float f)
	{
		return (float)Math.Round ((double)f, MidpointRounding.AwayFromZero);
	}
	
	public static int RoundToInt (float f)
	{
		return (int)Math.Round ((double)f, MidpointRounding.AwayFromZero);
	}
}
