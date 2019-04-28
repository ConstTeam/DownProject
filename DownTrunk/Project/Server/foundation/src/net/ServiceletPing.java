package net;


public abstract class ServiceletPing implements IService {
	protected IContext context;

	public void doMessage(ISession session, IByteBuffer data) {
		try {
			access(session, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getBackletInfo() {
		return getClass().getName();
	}

	public IContext getContext() {
		return this.context;
	}

	public void init(IContext context) {
		this.context = context;
	}

	public void destroy() {
	}

	public abstract void access(ISession paramISession,
			IByteBuffer paramIByteBuffer) throws DataAccessException;
}