namespace MS
{
	abstract public class IService 
	{
		public abstract void ProcessMessage(ConnectBase conn, ByteBuffer data);
	}
}
