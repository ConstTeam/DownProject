package message;

import net.IByteBuffer;
import net.ISession;

/**
 * 抽象Action
 * 
 */
public abstract class AbstractAction {
	
	/** Session */
	protected ISession session;
	/** 消息体 */
	protected IByteBuffer data;
	/** 执行 */
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
