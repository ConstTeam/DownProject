package net;

import java.net.InetAddress;

public class ServerAddress {
	protected InetAddress address;
	protected int port;
	protected String name;
	protected String password;

	public InetAddress getAddress() {
		return this.address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean equals(ServerAddress serverAddress) {
		if (serverAddress == null) {
			return false;
		}
		if ((this.address == null) || (serverAddress.address == null)) {
			return false;
		}
		return (this.address.equals(serverAddress.address))
				&& (this.port == serverAddress.port);
	}

	public String toString() {
		return "ip:" + this.address.getHostAddress() + " port:" + this.port;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ServerAddress clone() {
		ServerAddress serverAddress = new ServerAddress();
		serverAddress.setAddress(this.address);
		serverAddress.setPort(this.port);
		serverAddress.setName(this.name);
		serverAddress.setPassword(this.password);
		return serverAddress;
	}
}