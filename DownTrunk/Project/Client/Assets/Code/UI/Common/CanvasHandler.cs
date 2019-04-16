using UnityEngine;
using UnityEngine.UI;

public class CanvasHandler : MonoBehaviour
{
	void Start()
	{
		gameObject.GetComponent<CanvasScaler>().matchWidthOrHeight = ApplicationConst.matchWidthOrHeight;
	}
}
