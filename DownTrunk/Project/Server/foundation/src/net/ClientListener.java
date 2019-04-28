package net;

public interface ClientListener {
	public void onClosed(NIOClientConnection netconnection);

	public void messageArrived(NIOClientConnection netconnection,
			IByteBuffer message);
}
