using UnityEngine;
using System.Collections.Generic;
using System.IO;
using System;
using ICSharpCode.SharpZipLib.BZip2;

namespace MS
{
	public class ConfigData
	{
		private static Dictionary<string, ConfigTable> m_ConfigData = new Dictionary<string, ConfigTable>();

		public static void ParseConfig(byte[] bConfig)
		{
            m_ConfigData.Clear();
			EncryptTool.Decrypt(ref bConfig, 20);
			string sConfig = Decompress(bConfig);

			int valuePos = 0;
			int pos = 0;
			while(true)
			{
				pos = sConfig.IndexOf("------@", valuePos);
				if(-1 == pos)
					break;

				pos += 7;
				int nPos = sConfig.IndexOf('\n', pos);
				string cfgName = sConfig.Substring(pos, nPos - pos);

				try
				{
					ConfigTable cfgTbl = new ConfigTable();
					m_ConfigData.Add(cfgName, cfgTbl);

					pos = nPos + 1;
					nPos = sConfig.IndexOf("\n", pos);
					bool bFlag = true;
					while(bFlag)
					{
						try
						{
							cfgTbl.AddColKey(GetNextValue(ref pos, ref nPos, ref bFlag, ref sConfig));
						}
						catch(System.Exception e)
						{
							Debug.LogError(String.Format("{0} 表名：{1}", e, cfgName));
						}
					}

					nPos = sConfig.IndexOf("\n", pos);
					bFlag = true;
					valuePos = nPos + 1;
					while(bFlag)
					{
						string rowName = GetNextValue(ref pos, ref nPos, ref bFlag, ref sConfig);
						int valueNPos = sConfig.IndexOf("\n", valuePos);
						bool valueFlag = true;
						int a = 0;
						ConfigRow rowData = new ConfigRow();
						rowData.AddValue(rowName);
						while(valueFlag)
						{
							string value = GetNextValue(ref valuePos, ref valueNPos, ref valueFlag, ref sConfig);
							value = value.Replace("#r", "\n");
							rowData.AddValue(value);
							++a;
						}
						cfgTbl.AddRow(rowName, rowData);
					}
				}
				catch(System.Exception e)
				{
					Debug.LogError(string.Format("{0}.配置表名:{1}", e, cfgName));
				}
			}
		}

		private static string GetNextValue(ref int refPos, ref int refNPos, ref bool bFlag, ref string sConfig)
		{
			int tPos = sConfig.IndexOf('\t', refPos);
			if(tPos > refNPos || -1 == tPos)
			{
				tPos = refNPos;
				bFlag = false;
			}
			string value = sConfig.Substring(refPos, tPos - refPos);
			refPos = tPos + 1;
			return value;
		}

		private static string Decompress(byte[] buffer)
		{
			MemoryStream ms = new MemoryStream(buffer);
			Stream sm = new BZip2InputStream(ms);
			StreamReader reader = new StreamReader(sm, System.Text.Encoding.UTF8);
			string configText = reader.ReadToEnd();
			sm.Close();
			ms.Close();
			return configText;
		}

		public static Dictionary<string, int> GetColKeys(string sKey1)
		{
			return m_ConfigData[sKey1].m_dicColKeys;
		}

		public static ConfigTable GetValue(string sKey1)
	    {
	        return m_ConfigData[sKey1];
	    }

		public static ConfigRow GetValue(string sKey1, string sKey2)
		{
			return m_ConfigData[sKey1].GetRow(sKey2);
		}
		
	    public static string GetValue(string sKey1, string sKey2, string sKey3)
	    {
			ConfigTable tbl = m_ConfigData[sKey1];
			return tbl.GetValue(sKey2, sKey3);
	    }

		public static int GetSize( string sFileName )
		{
			return m_ConfigData[sFileName].m_Data.Count;
		}

		public static string GetStaticText(string sKey)
		{
			try
			{
				return m_ConfigData["Lan_StaticText_Client"].GetValue(sKey, "Text");
			}
			catch
			{
#if UNITY_EDITOR
				Debug.LogError(sKey + " not found from Lan_StaticText_Client");
#endif
				return "";
			}
		}

		public static void DeleteConfig(string cfgName)
		{
			m_ConfigData.Remove(cfgName);
		}

        public static string GetHUDText(string sKey)
        {
            return GetValue("Lan_Instance_Client", sKey, "Text");
        }
	}
}
