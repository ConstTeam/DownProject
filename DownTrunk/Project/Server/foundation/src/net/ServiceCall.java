package net;

public abstract class ServiceCall implements IService {

	protected IContext context;

	public void doMessage(ISession session, IByteBuffer data) {
		try {
			int id = data.readInt();

			try {
				IByteBuffer result = access(session, data);
				if (result == null) {
					throw new DataAccessException(500, "system.unknow");
				}

				ByteBuffer message = new ByteBuffer();
				message.writeByte(0);
				message.writeByte(2);
				message.writeInt(id);
				message.writeShort(200);
				message.writeByteBuffer(result, result.available());
				session.send(message);
			} catch (DataAccessException e) {
				e.printStackTrace();
				data.clear();
				data.writeByte(0);
				data.writeByte(2);
				data.writeInt(id);
				data.writeShort(e.getType());

				LanguageKit.getlanguage(data, e.getMessage(), e.getMessages());
				session.send(data);
			} catch (Exception e) {
				e.printStackTrace();
				data.clear();
				data.writeByte(0);
				data.writeByte(2);
				data.writeInt(id);
				data.writeShort(404);

				LanguageKit.getlanguage(data, "system.unknow", new String[0]);
				session.send(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println(getBackletInfo() + " service !" + e.getMessage());
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

	public abstract IByteBuffer access(ISession paramISession,
			IByteBuffer paramIByteBuffer) throws DataAccessException;
}
