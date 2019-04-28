package net;

public interface IEncrypt {

	public void coding(byte[] data, int start, int lenght, String codeKey);

	public void coding(byte[] data);
}
