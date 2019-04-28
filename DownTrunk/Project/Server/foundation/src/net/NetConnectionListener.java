package net;

public interface NetConnectionListener {

	public abstract void messageArrived(NetConnection netconnection,
			IByteBuffer message);
}
