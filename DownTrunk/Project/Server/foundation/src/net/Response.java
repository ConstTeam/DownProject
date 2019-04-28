package net;

public abstract class Response {

	public int TIME_OUT = 30000;

	public int id = 0;
	public int cmd = 0;
	public long timeOut = 0L;

	public Response(int cmd) {
		this.id = hashCode();
		this.cmd = cmd;
		this.timeOut = (System.currentTimeMillis() + this.TIME_OUT);
	}

	public void setTimeOut(int value) {
		this.TIME_OUT = value;
		this.timeOut = (System.currentTimeMillis() + this.TIME_OUT);
	}

	public void sendBackSuccess(ISession session, int sid, IByteBuffer result) {
		ByteBuffer message = new ByteBuffer();
		message.writeByte(0);
		message.writeInt(sid);
		message.writeShort(200);
		message.writeByteBuffer(result, result.available());
		session.send(message);
	}

	public void sendBackFault(ISession session, int sid, IByteBuffer result) {
		ByteBuffer message = new ByteBuffer();
		message.writeByte(0);
		message.writeInt(sid);
		message.writeShort(201);
		message.writeByteBuffer(result, result.available());
		session.send(message);
	}

	public boolean isTimeOut() {
		return System.currentTimeMillis() > this.timeOut;
	}

	public abstract void respondOK(IByteBuffer paramIByteBuffer);

	public abstract void respondFail(IByteBuffer paramIByteBuffer);
}