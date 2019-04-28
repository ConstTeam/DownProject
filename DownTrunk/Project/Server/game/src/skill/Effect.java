package skill;

import module.fight.BattleRole;

public class Effect {
	
	/** �����¼� */
	private String triggerEvent;
	/** Ч������ */
	private String effectType;
	/** ��ֵ*/
	private int value;
	/** �Ƿ�Ϊ��ʱЧ�� */
	private boolean temp;
	/** �Ƿ�Ϊ�ۼ�Ч�� */
	private boolean repeat;
	
	public Effect(String triggerEvent, String effectType, int value, boolean temp, boolean repeat) {
		this.triggerEvent = triggerEvent;
		this.effectType = effectType;
		this.value = value;
		this.temp = temp;
		this.repeat = repeat;
		if (BattleRole.AMPLIFY.equals(effectType)) {
			this.temp = true;
		}
	}

	public String getEffectType() {
		return effectType;
	}

	public void setEffectType(String effectType) {
		this.effectType = effectType;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getTriggerEvent() {
		return triggerEvent;
	}

	public void setTriggerEvent(String triggerEvent) {
		this.triggerEvent = triggerEvent;
	}

	public boolean isTemp() {
		return temp;
	}

	public void setTemp(boolean temp) {
		this.temp = temp;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	
}
