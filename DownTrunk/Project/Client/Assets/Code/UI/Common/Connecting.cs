using UnityEngine;

namespace MS
{
	public class Connecting : MonoBehaviour
	{
		public GameObject BG;
		public Transform Ani;

		private static Connecting _Inst;
		public static Connecting GetInst()
		{
			return _Inst;
		}

		void Awake()
		{
			_Inst = this;
			gameObject.SetActive(false);

			SocketHandler.ShortSendExcuteFun	= Show;
			SocketHandler.ShortSendBackFun		= Hide;
			SocketHandler.LongSendExcuteFun		= Show;
		}

		void OnDestroy()
		{
			_Inst = null;
		}

		Vector3 rot = new Vector3(0, 0, -3);
		private void Update()
		{
			Ani.Rotate(rot);
		}

		private void _Show()
		{
			BG.SetActive(true);
			Ani.gameObject.SetActive(true);
		}

		private void _Hide()
		{
			BG.SetActive(false);
			Ani.gameObject.SetActive(false);
		}

		public void Show()
		{
			if(!gameObject.activeSelf)
			{
				_Hide();
				gameObject.SetActive(true);
				Invoke("_Show", 1f);
			}
		}

		public void Hide()
		{
			if(0 == SocketHandler.GetInst().GetShortConnListCount())
			{
				CancelInvoke("_Show");
				gameObject.SetActive(false);
			}
		}

		public void ForceHide()
		{
			CancelInvoke("_Show");
			gameObject.SetActive(false);
		}
	}
}
