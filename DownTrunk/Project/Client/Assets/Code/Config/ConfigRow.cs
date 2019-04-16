using System.Collections.Generic;

namespace MS
{
	public class ConfigRow
	{
		private List<string> _rowData;
		private Dictionary<string, int> _colKeys;

		public ConfigRow()
		{
			_rowData = new List<string>();
		}

		public void SetColKeys(Dictionary<string, int> colKeys)
		{
			_colKeys = colKeys;
		}

		public void AddValue(string value)
		{
			_rowData.Add(value);
		}

		public string GetValue(string colKey)
		{
            int iVal;
			if(!_colKeys.TryGetValue(colKey, out iVal))
				return null;
			return _rowData[iVal];
		}
	}
}
