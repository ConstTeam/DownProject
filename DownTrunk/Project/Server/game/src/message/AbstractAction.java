package message;

import net.IByteBuffer;
import net.ISession;

/**
 * ����Action
 * 
 */
public abstract class AbstractAction {
	
	/** Session */
	protected ISession session;
	/** ��Ϣ�� */
	protected IByteBuffer data;
	/** ִ�� */
	public abstract void execute();
	
	public AbstractAction(ISession session, IByteBuffer data) {
		this.session = session;
		this.data = data;
	}
	public ISession getSession() {
		return session;
	}
	public IByteBuffer getData() {
		return data;
	}
}
