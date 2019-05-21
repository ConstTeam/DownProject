#if !UNITY_EDITOR
using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class RLog : MonoBehaviour
{
	private static string _sLogUrl;
	private static List<LogType> _listLogTypes;

	public void Init(string content)
	{
		if(content.IndexOf("http") > -1)
		{
			_listLogTypes = new List<LogType>();
			string[] info = content.Split('|');
			if(info.Length > 0)
			{
				_sLogUrl = info[0];
				string[] type = info[1].Split('#');
				for(int i = 0; i < type.Length; ++i)
				{
					_listLogTypes.Add(GetLogType(type[i]));
				}

				Application.logMessageReceived += UploadLog;
			}
		}
	}

	public void GMCall(string[] info)
	{
		if("1" == info[1])
			Application.logMessageReceived += ShowLog;
		else
			Application.logMessageReceived -= ShowLog;
	}
	
	private static LogType GetLogType(string type)
	{
		switch(type)
		{
			case "e": return LogType.Error;
			case "a": return LogType.Assert;
			case "w": return LogType.Warning;
			case "l": return LogType.Log;
			default: return LogType.Exception;
		}
	}

	private void UploadLog(string cond, string stack, LogType type)
	{
		if(_listLogTypes.Contains(type))
		{
			string sLog = string.Format("{0}\n{1}", cond, stack);
			StartCoroutine(_SendLog(sLog, (int)type));
		}
	}

	private void ShowLog(string cond, string stack, LogType type)
	{
		//string sLog = string.Format("{0}\n{1}", cond, stack);
	}

	IEnumerator _SendLog(string message, int logType)
	{
		WWWForm form  = new WWWForm();
		form.AddField("RoleId", RoleData.RoleID);
		form.AddField("Message", message);
		form.AddField("LogType", logType);
		form.AddField("OS", (int)Application.platform);
		form.AddField("PlatformId", ApplicationConst.PlatformID);
		form.AddField("ChannelId", ApplicationConst.ChannelID);
		form.AddField("BundleVersion", ApplicationConst.BundleVersion);
		form.AddField("SvnVersion", ApplicationConst.SvnVersion);
		
		WWW getData = new WWW(_sLogUrl, form);
		yield return getData;

		getData.Dispose();
		getData = null;     
	}
}
#endif
