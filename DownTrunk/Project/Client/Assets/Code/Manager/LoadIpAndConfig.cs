using UnityEngine;
using System.Collections;
using System.IO;
using MS;

public class LoadIpAndConfig : MonoBehaviour
{
	public delegate void LoadConfigIpEnd();
	public delegate void CheckStateFunc(string text);

	public CheckStateFunc CheckState;
	private LoadConfigIpEnd LoadConfigIpEndFunc;
	
	public void LoadConfigIp(LoadConfigIpEnd cb)
	{
		CheckState((string)ApplicationConst.dictStaticText["23"]);
		LoadConfigIpEndFunc = cb;
		if (ApplicationConst.bDynamicRes)
			LoadConfigIpInPersistent();
		else
			StartCoroutine(LoadConfigIpInStreaming());
	}

	public void LoadConfigIpInPersistent()
	{
		string tgtIp = "";
		byte[] tgtCfg = null;
		FileStream fsr = null;
		StreamReader sr = null;
		string[] src = { "{0}/ip.txt", "{0}/cfg.ms" };
		string configPath = PathManager.GetPersistentPath();
		for (int i = 0; i < 2; ++i)
		{
			string path = string.Format(src[i], configPath);
			bool exist = File.Exists(path);
			if (exist)
			{
				fsr = new FileStream(path, FileMode.Open);
				if (i == 0)
				{
					sr = new StreamReader(fsr);
					tgtIp = sr.ReadToEnd();
					sr.Close();
					sr.Dispose();
				}
				else
				{
					int len = (int)fsr.Length;
					tgtCfg = new byte[len];
					fsr.Read(tgtCfg, 0, len);
				}

				fsr.Close();
				fsr.Dispose();
			}
		}
		sr = null;
		fsr = null;

		LoadConfigIpCallback(tgtIp, tgtCfg);
	}

	IEnumerator LoadConfigIpInStreaming()
	{
		string tgtIp = "";
		byte[] tgtCfg = null;
		string[] src = { "{0}/ip.txt", "{0}/cfg.ms" };
		string configPath = PathManager.GetStreamingPath();
		for (int i = 0; i < 2; ++i)
		{
			string path = string.Format(src[i], configPath);
			WWW www = new WWW(path);
			yield return www;

			if (www.isDone)
			{
				if (null == www.error)
				{
					if (i == 0)
					{
						tgtIp = www.text;
						tgtIp = tgtIp.Remove(0, 1);
					}
					else
						tgtCfg = www.bytes;
				}
				else
					Debug.LogError(www.error);
			}
			www.Dispose();
			www = null;
		}

		LoadConfigIpCallback(tgtIp, tgtCfg);
	}

	private void LoadConfigIpCallback(string sIp, byte[] sConfig)
	{
		CheckState((string)ApplicationConst.dictStaticText["24"]);
		IpData.LoadIp(sIp);
		ConfigData.ParseConfig(sConfig);
		LoadConfigIpEndFunc();
	}
}
