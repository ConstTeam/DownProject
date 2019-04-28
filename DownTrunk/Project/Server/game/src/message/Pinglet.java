package message;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ByteBufferFactory;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.ServiceletPing;
import util.ErrorPrint;

/**
 * ÐÄÌø°ü
 *
 */
public class Pinglet extends ServiceletPing {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Pinglet.class);
	
	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		try {
			IByteBuffer result = ByteBufferFactory.getNewByteBuffer();
			result.writeByte(0);
			result.writeByte(2);
			result.writeByteBuffer(data);
			
			session.send(result);
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}
}
