package net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServiceCallNB implements IService {
	private static final Logger log = LoggerFactory.getLogger(ServiceCallNB.class);

	public void doMessage(ISession session, IByteBuffer data) {
		try {
			int id = data.readInt();
			log.debug("access id " + id);
			access(session, id, data);
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println(getBackletInfo() + " service !" + e.getMessage());
		}
	}

	public String getBackletInfo() {
		return getClass().getName();
	}

	public void destroy() {
	}

	public abstract void access(ISession paramISession, int paramInt,
			IByteBuffer paramIByteBuffer) throws DataAccessException;
}