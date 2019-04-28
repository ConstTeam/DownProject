package module.scene;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.IByteBuffer;
import util.Tools;

public class SelectObject {

	private static final Logger logger = LoggerFactory.getLogger(SelectObject.class);
	
	private int targetUid;
	
	private int areaIndex;
	
	private ArrayList<Integer> target = new ArrayList<>();
	
	public static SelectObject spellCardPlaySelectObj(IByteBuffer data) {
		SelectObject o = new SelectObject();
		String str = data.readUTF();
		if (Tools.isEmptyString(str)) {
			return o;
		}
		String[] split = str.split("\\|");
		for (String s : split) {
			try {
				o.getTarget().add(Integer.parseInt(s));
			} catch (Exception e) {
				logger.error("选择目标参数异常：str", str);
				return o;
			}
		}
		return o;
	}
	
	public int getAreaIndex() {
		return areaIndex;
	}

	public void setAreaIndex(int areaIndex) {
		this.areaIndex = areaIndex;
	}

	public int getTargetUid() {
		return targetUid;
	}

	public void setTargetUid(int targetUid) {
		this.targetUid = targetUid;
	}

	public ArrayList<Integer> getTarget() {
		return target;
	}

	public void setTarget(ArrayList<Integer> target) {
		this.target = target;
	}
}
