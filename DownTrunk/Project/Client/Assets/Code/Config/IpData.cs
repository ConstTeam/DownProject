using UnityEngine;
using System.Collections.Generic;

namespace MS
{
	public class IpData
	{
		private static string[] m_sLastServerInfo = null;
		private static  string[] m_sCurServerInfo = null;

		private static List<string[]> _listLogin = new List<string[]>();

		public static void LoadIp(string ipData)
		{
			_listLogin.Clear();
			string lastServerIndex = PlayerPrefs.GetString("LastLoginServer", "");

			string[] data = ipData.Split('\n');
			for(int i = data.Length-1; i >= 0; --i)
			{
				if("" == data[i])
					continue;

				string[] info = data[i].Replace("\r", "").Split('|');
				_listLogin.Add(info);

				if(info[0] == lastServerIndex)
					m_sCurServerInfo = m_sLastServerInfo = info;
			}

			if(null == m_sCurServerInfo || m_sCurServerInfo.Length <= 0)
				m_sCurServerInfo = _listLogin[data.Length - 1];

			SetUrl(); 
		}

		public static void SetUrl()
		{
			if (null == m_sCurServerInfo)
				Debug.LogError("LoginServer has not defined in ip.xml!");

			SocketHandler.GetInst().LongSetUrl(m_sCurServerInfo[1], m_sCurServerInfo[2], 0);
		}

		public static List<string[]> GetServerList()
		{
			return _listLogin;
		}

		public static int GetServerCount()
		{
			return _listLogin.Count;
		}

		public static string[] GetCurServerData()
		{
			return m_sCurServerInfo;
		}

		public static void SetCurServerData(int index)
		{
			m_sCurServerInfo = _listLogin[index];
			SetUrl();
		}

		public static void WriteLastServerIndex()
		{
			PlayerPrefs.SetString("LastLoginServer", m_sCurServerInfo[0]);	
		}

        public static string[] GetLastServerData()
        {
            return m_sLastServerInfo;
        }
    }
}
