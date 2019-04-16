#if UNITY_IOS
using System.Runtime.InteropServices;
using UnityEngine;

namespace MS
{
    public class CommunicateWithOC : MonoBehaviour
    {
        [DllImport("__Internal")]
        public static extern string GetBundleShortVersion();

        [DllImport("__Internal")]
        public static extern string GetBundleVersion();

        [DllImport("__Internal")]
        public static extern string GetIPv6(string host);

        [DllImport("__Internal")]
        public static extern void SetNotBackup();

        [DllImport("__Internal")]
        public static extern void GotoAppStore(string s);


        void Awake()
        {
            SetNotBackup();
        }

        public void OnLoginSuccess(string msg)
        {
            RoleData.Account = string.Empty;
            RoleData.Token = msg;
            SocketHandler.GetInst().LongConnect(0);
        }

        public void OnLogoutSuccess(string msg)
        {
            //ApplicationEntry.ToLoginScene();
        }

        public void BuyProductFailed(string msg)
        {
            //NGUIDebug.Log("BuyProductFail");
            //Connecting.GetInst().ForceHide();
        }

        public void BuyProductSuccess(string msg)
        {
            //NGUIDebug.Log("BuyProductSuccess");
        }

        public void ProvideContent(string s)
        {
            //BuyDiamond.GetInst().DoRecharge(s);
        }

        //-------------------------------------------------------------------------------------
        //public List<string> productInfo = new List<string>();

        public void ShowProductList(string s)
        {
            //productInfo.Add(s);
        }

        public void RestoreTransaction(string s)
        {
            //NGUIDebug.Log("RestoreTransaction");
        }

        public void PurchasingTransaction(string s)
        {
            //NGUIDebug.Log("PurchasingTransaction");
        }

        public void DeferredTransaction(string s)
        {
            //NGUIDebug.Log("DeferredTransaction");
        }
    }
}
#endif
