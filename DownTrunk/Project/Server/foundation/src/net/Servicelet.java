package net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Servicelet implements IService {
	protected IContext context;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Servicelet.class);
	
	public void doMessage(ISession session, IByteBuffer data) {
		try {
//			long timeInMillis = Calendar.getInstance().getTimeInMillis();
//			logger.debug("Servicelet doMessage Begin: ip:{}, port:{}, time:{}", session.getAddress().toString(), session.getPort(), timeInMillis);
			access(session, data);
//			CorrespondMessage.sendColseMessage(session);
//			logger.debug("Servicelet doMessage End: ip:{}, port:{}, time:{}", session.getAddress().toString(), session.getPort(), timeInMillis);
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