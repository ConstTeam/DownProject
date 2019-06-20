using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

namespace MS
{
	public class MsgBoxPanel : MonoBehaviour
	{
		public static List<string> lstMsgBox = new List<string>();

		public TextMeshProUGUI		UITitel;
		public Text					UIContent;
		public Button				UIOKBtn;
		public Button				UICancelBtn;
        
        public delegate void MsgCallback();
		private MsgCallback _OKCallBack;
		private MsgCallback _CancelCallBack;
		private string _sSingleMark;

		public static void ShowMsgBox(string sTitle, string sContent, int iType, MsgCallback okCb = null, MsgCallback cancelCb = null, string singleMark = "")
		{
			if(singleMark != string.Empty)
			{
				if(lstMsgBox.Contains(singleMark))
					return;

				lstMsgBox.Add(singleMark);
			}

			GameObject go = Instantiate(Resources.Load<GameObject>("PrefabUI/Common/MessageBox"));
			Transform tran = go.transform;
			tran.SetParent(ApplicationEntry.GetInst().uiRoot, false);
			tran.localScale = Vector3.one;
			go.GetComponent<MsgBoxPanel>().Show(sTitle, sContent, iType, okCb, cancelCb, singleMark);
		}

        private void Awake()
        {
            UIOKBtn.onClick.AddListener(OnOKBtn);
            UICancelBtn.onClick.AddListener(OnCancleBtn);
        }

		private void OnOKBtn()
		{	
			if(null != _OKCallBack)
				_OKCallBack();

			ClosePanel();
		}

		private void OnCancleBtn()
		{
			if(null != _CancelCallBack)
				_CancelCallBack();

			ClosePanel();
		}

		/// <summary>
		/// 显示对话框
		/// </summary>
		/// <param name="sTitle">标题</param>
		/// <param name="sContent">内容</param>
		/// <param name="iType">对话框类型 0无按钮 1一个按钮 2两个按钮</param>
		/// <param name="okCb">确定按钮的点击事件</param>
		/// <param name="cancelCb">取消按钮的点击事件</param>
		/// <param name="singleMark">是否为单一对话框的标记</param>
		public void Show(string sTitle, string sContent, int iType, MsgCallback okCb = null, MsgCallback cancelCb = null, string singleMark = "")
		{
			UITitel.text		= sTitle;
			UIContent.text		= sContent;
			_OKCallBack			= okCb;
			_CancelCallBack		= cancelCb;
			_sSingleMark		= singleMark;

			if(1 == iType)
				UICancelBtn.gameObject.SetActive(false);
		}

		private void ClosePanel()
		{
			if(_sSingleMark != string.Empty)
				lstMsgBox.Remove(_sSingleMark);
			Destroy(gameObject);
		}
	}
}
