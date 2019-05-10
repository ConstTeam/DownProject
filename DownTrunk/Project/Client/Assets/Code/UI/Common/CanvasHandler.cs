using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class CanvasHandler : MonoBehaviour
	{
		void Start()
		{
			gameObject.GetComponent<CanvasScaler>().matchWidthOrHeight = ApplicationConst.matchWidthOrHeight;
		}
	}
}
