using UnityEngine;

namespace MS
{
	public class PathManager 
	{
		public static string GetPersistentPath()
		{
			return string.Format("{0}/NotBackup", Application.persistentDataPath);
		}

		public static string GetStreamingPath()
		{
#if UNITY_EDITOR || UNITY_STANDALONE_WIN || UNITY_IOS
            return string.Format("file://{0}/UpdatePkg", Application.streamingAssetsPath);
#elif UNITY_ANDROID
            return string.Format("{0}/UpdatePkg", Application.streamingAssetsPath);            
#endif
        }
    }
}
