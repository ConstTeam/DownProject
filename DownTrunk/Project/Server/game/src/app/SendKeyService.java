package app;

import net.DataAccessException;
import net.IByteBuffer;
import net.IEncrypt;
import net.ISession;
import net.NetConnection;
import net.Servicelet;
import codec.CorrespondMessage;

public class SendKeyService extends Servicelet {

	@Override
	public void access(ISession session, IByteBuffer data)
			throws DataAccessException {
		NetConnection netConnection = (NetConnection) session;
		String codeKey = netConnection.getCodeKey();
		IEncrypt encrypt = netConnection.getMessageEncrypt();
		byte[] bytes = codeKey.getBytes();
		encrypt.coding(bytes);
		netConnection.send(CorrespondMessage.getEncryptKeySendMessage(new String(
				bytes)));
	}

}
