using UnityEngine;

public class SceneLoaderBase : MonoBehaviour
{
	protected void Clear()
	{
		Resources.UnloadUnusedAssets();
		System.GC.Collect();
	}
}
