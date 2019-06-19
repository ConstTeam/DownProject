using MS;

public class PlayerData
{
	public static string	Account		{ set; get; }
	public static string	Token		{ set; get; }
	public static int		PlayerId	{ set; get; }
	public static string	Nickname	{ set; get; }
	public static int		CurHP		{ set; get; }
	public static int		CurScene	{ get; set; }
	public static int		CurHero		{ get; set; }

	private static int _iCoin;
	public static int Coin
	{
		get { return _iCoin; }
		set
		{
			_iCoin = value;
			HeroPanel.GetInst().CoinText.text = value.ToString();
			ScenePanel.GetInst().CoinText.text = value.ToString();
		}
	}
}
