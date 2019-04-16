using UnityEngine;
using UnityEngine.UI;

public class ConsolePanel : MonoBehaviour
{
	public Text Label;

	private float	_fUpdateInterval = 0.5f;
	private float	_fLastInterval;
	private int		_iFrames = 0;
	private float	_fFps;

	private static bool _bShow = false;

	private static ConsolePanel m_Inst;
	public static ConsolePanel GetInst()
	{
		return m_Inst;
	}

	private void Awake()
	{
		m_Inst = this;
		gameObject.SetActive(_bShow);
	}

	private void OnDestroy()
	{
		m_Inst = null;
	}

	private void Start()
	{
		_fLastInterval = Time.realtimeSinceStartup;
		_iFrames = 0;
	}

	public void BeShow()
	{
		_bShow = !_bShow;
		gameObject.SetActive(_bShow);
	}

	void Update()
	{
		if(_bShow)
		{
			++_iFrames;
			if (Time.realtimeSinceStartup > _fLastInterval + _fUpdateInterval)
			{
				_fFps = _iFrames / (Time.realtimeSinceStartup - _fLastInterval);
				_iFrames = 0;
				_fLastInterval = Time.realtimeSinceStartup;
				Label.text = string.Format("FPS:{0:.00}", _fFps);
			}
		}
	}
	
}
