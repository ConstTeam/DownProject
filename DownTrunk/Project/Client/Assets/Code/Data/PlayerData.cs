using MS;

public class PlayerData
{
	public static string	Account			{ set; get; }
	public static string	Token			{ set; get; }
	public static int		PlayerId		{ set; get; }
	public static string	Nickname		{ set; get; }
	public static int		CurHP			{ set; get; }
	public static int		CurScene		{ get; set; }

	private static int _iCurHero;
	public static int CurHero
	{
		get { return _iCurHero; }
		set { _iCurHero = value; if(HeroPanel._inst != null) HeroPanel.GetInst().SetCurHero(); }
	}

	private static int _iCoin;
	public static int Coin
	{
		get { return _iCoin; }
		set { _iCoin = value; if(HeroPanel._inst != null) HeroPanel.GetInst().SetCoin(); if(ScenePanel._inst != null) ScenePanel.GetInst().SetCoin(); }
	}

	private static int _iAllHeroState;
	public static int AllHeroState
	{
		get { return _iAllHeroState; }
		set { _iAllHeroState = value; if(HeroPanel._inst != null) HeroPanel.GetInst().SetState(); }
	}

	private static int _iAllSceneState;
	public static int AllSceneState
	{
		get { return _iAllSceneState; }
		set { _iAllSceneState = value; if(ScenePanel._inst != null) ScenePanel.GetInst().SetState(); }
	}
}
