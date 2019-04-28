package db.module.log;

import java.util.HashMap;
import java.util.Iterator;

public class LogData extends HashMap<String, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String toString() {
		Iterator<Entry<String, Object>> iterator = 
				this.entrySet().iterator();
		String str = "";
		while(iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			str += entry.getKey() + "=" + entry.getValue();
			if(iterator.hasNext()) {
				str += "&";
			}
		}
		return str;
	}
	
	public String getKeys() {
		Iterator<Entry<String, Object>> iterator = 
				this.entrySet().iterator();
		String str = "";
		while(iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			str += entry.getKey();
			if(iterator.hasNext()) {
				str += ",";
			}
		}
		return str;
	}

	public String getValues() {
		Iterator<Entry<String, Object>> iterator = 
				this.entrySet().iterator();
		String str = "";
		while(iterator.hasNext()) {
			iterator.next();
			str += "?";
			if(iterator.hasNext()) {
				str += ",";
			}
		}
		return str;
	}
}
