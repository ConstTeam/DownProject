using TMPro;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class HeroPanelItem : MonoBehaviour
	{
		public TextMeshProUGUI Name;
		public Toggle Toggle;

		public int HeroId { get; set; }

		public void SetInfo(int heroId, string name, ToggleGroup tg)
		{
			HeroId = heroId;
			Name.text = name;
			ResourceLoader.LoadAssetAndInstantiate(string.Format("PrefabUI/Main/Role/{0}", HeroId), transform, new Vector3(-136f, 50f));
			ResourceLoader.LoadAssetAndInstantiate(string.Format("PrefabUI/Main/Item/{0}", HeroId), transform, new Vector3(-5.7f, 10f));
			Toggle.group = tg;
		}
	}
}
