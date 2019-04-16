using System.Collections.Generic;

namespace MS
{
	public class ConfigTable
	{
		public Dictionary<string, ConfigRow> m_Data;
		public Dictionary<string, int> m_dicColKeys;
		private int _iColLength;
	
		public ConfigTable()
		{
			m_Data			= new Dictionary<string, ConfigRow>();
			m_dicColKeys	= new Dictionary<string, int>();
			_iColLength		= 0;
		}

		public void AddRow(string rowKey, ConfigRow rowData)
		{
			try
			{
				rowData.SetColKeys(m_dicColKeys);
				m_Data.Add(rowKey, rowData);
			}
			catch(System.Exception e)
			{
				throw new System.Exception(string.Format("{0}.行名:{1}", e, rowKey));
			}
		}

		public void AddColKey(string colKey)
		{
			try
			{
				m_dicColKeys.Add(colKey, _iColLength++);
			}
			catch(System.Exception e)
			{
				throw new System.Exception(string.Format("{0}\n配置表列名重复.列名:{1}", e.Message, colKey));
			}
		}

		public string GetValue(string rowKey, string colKey)
		{
            ConfigRow _configRow;
			if(!m_Data.TryGetValue(rowKey, out _configRow))
				return null;
			return _configRow.GetValue(colKey);
		}

		public ConfigRow GetRow(string rowKey)
		{
            ConfigRow _configRow;
            if (!m_Data.TryGetValue(rowKey, out _configRow))
				return null;
			return _configRow;;
		}

		public bool HasRow(string rowKey)
		{
			return m_Data.ContainsKey(rowKey);
		}
	}
}
