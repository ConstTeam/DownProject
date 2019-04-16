#if UNITY_ANDROID
using System.Collections;
using UnityEngine;

namespace MS
{
    public class CommunicateWithJava : MonoBehaviour
    {
        [System.NonSerialized]
        public AndroidJavaObject m_AJObj;
        private AndroidJavaClass m_AJClass;

        private static CommunicateWithJava m_Inst;

        public static CommunicateWithJava GetInst()
        {
            return m_Inst;
        }

        void OnDestroy()
        {
            m_Inst = null;
        }

        void Awake()
        {
            DontDestroyOnLoad(this);
            m_Inst = (CommunicateWithJava)(MonoBehaviour)this;
#if PLATFORM_ANDROID_SDK && CHANNEL_FACEBOOK
#else
            if (Application.platform == RuntimePlatform.Android)
            {
                m_AJClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
                m_AJObj = m_AJClass.GetStatic<AndroidJavaObject>("currentActivity");
            }
#endif
        }

        public void OnInitSuccess(string msg)
        {
            //NGUIDebug.Log("OnInitSuccess" + msg);
        }

        public void OnInitFailed(string msg)
        {
            //NGUIDebug.Log("OnInitFailed" + msg);
        }

        public void OnLoginSuccess(string msg)
        {
           RoleData.Account = string.Empty;
           RoleData.Token = msg;
           SocketHandler.GetInst().LongConnect(0);
        }

        public void OnLoginFailed(string msg)
        {
            //NGUIDebug.Log("OnLoginFailed" + msg);
            Connecting.GetInst().Hide();
        }

        public void OnPaySuccess(string msg)
        {
            //NGUIDebug.Log("OnPaySuccess" + msg);
        }

        public void OnPayFailed(string msg)
        {
            //NGUIDebug.Log("OnPayFail:" + msg);
        }

        public void OnChannelExit(string msg)
        {
            ApplicationEntry.HandleExit();
        }

        public void OnLogoutSuccess(string msg)
        {
            ApplicationEntry.ToLoginScene();
        }

        public void OnLogout(string msg)
        {
            ApplicationEntry.HandleExit();
        }

        public void ExitOk(GameObject go, int index)
        {
            ApplicationEntry.HandleExit();
        }
    }
}
#endif
