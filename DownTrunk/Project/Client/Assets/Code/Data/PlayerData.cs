using MS;

public class PlayerData
{
	public static string	Account			{ set; get; }
	public static string	Token			{ set; get; }
	public static int		PlayerId		{ set; get; }
	public static string	Nickname		{ set; get; }
	public static int		CurHP			{ set; get; }

	private static int _iCoin;
	public static int Coin
	{
		get { return _iCoin; }
		set { _iCoin = value; if(HeroPanel._inst != null) HeroPanel.GetInst().SetCoin(); if(ScenePanel._inst != null) ScenePanel.GetInst().SetCoin(); }
	}

	private static int _iCurHero;
	public static int CurHero
	{
		get { return _iCurHero; }
		set { _iCurHero = value; if(HeroPanel._inst != null) HeroPanel.GetInst().SetCurHero(); }
	}

	private static int _iCurScene;
	public static int CurScene
	{
		get { return _iCurScene; }
		set { _iCurScene = value; if(HeroPanel._inst != null) ScenePanel.GetInst().SetCurScene(); }
	}

	private static int _iStateHero;
	public static int StateHero
	{
		get { return _iStateHero; }
		set { _iStateHero = value; if(HeroPanel._inst != null) HeroPanel.GetInst().SetState(); }
	}

	private static int _iStateScene;
	public static int StateScene
	{
		get { return _iStateScene; }
		set { _iStateScene = value; if(ScenePanel._inst != null) ScenePanel.GetInst().SetState(); }
	}
}
