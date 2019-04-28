package net;

import java.sql.Connection;

import db.ConnectionPool;

public class DBConnect {

	protected ConnectionPool connectPool;

	public Connection getDBConnect() {
		if (connectPool == null) {
			return null;
		}
		return connectPool.getConnection();
	}

	public ConnectionPool getConnectPool() {
		return connectPool;
	}

	public void setConnectPool(ConnectionPool connectPool) {
		this.connectPool = connectPool;
	}

	public void closeConnect(Connection connect) {
		if (connectPool == null || connect == null) {
			return;
		}
		connectPool.close(connect);

	}
}
