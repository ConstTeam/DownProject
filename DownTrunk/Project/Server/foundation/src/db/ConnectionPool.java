package db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionPool {
	// public static final String ARGSFILE =
	// "/home/hero/webgame/applib/data.dir/ConnectionPool.cfg";

	/* fields */
	public int debug = 0;

	private String driver = null;// JDBC Driver name

	private String url = null;// JDBC Connection URL

	private String user = null;// JDBC Driver name

	private String password = null;// JDBC Connection URL

	private int length = 0;// size of pool

	private int size = 0;// Minimum size of the pool

	private int maxsize = 0;// Maximum size of the pool

	private int timeout = 0;// Maximum connection idle time(in minutes)

	private int usecount = 0;// Maximum number of uses for a single

	// connection, or -1 for none
	private PackConnection[] pool = null;// The Connection pool.

	private boolean check = true;// ��ȡ������ʱ�����ж������Ƿ���ã�ʵ������jdbc��������ping���ӣ�

	private boolean active = true;

	// private int pooltimeout = 180000;

	// private long livetime = 0;

	private int threadworktime = 1;

	private ManageThread mthread = null;// �����߳�

	private Properties properties = new Properties();

	//
	// /* constructors */
	// /* init */
	// public void initialize() throws Exception
	// {
	// this.initialize(ARGSFILE);
	// }

	public void initialize(String argsfile) throws Exception {

		// Map hm = FileKit.loadArgsFile(argsfile);
		// init(hm);
		Properties props = new Properties();

		Reader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(argsfile), "gbk"));
		props.load(in);
		init(props);
		mthread = new ManageThread();// ���������߳�
		mthread.setName("MysqlConnPoolManageThread");
		mthread.setDaemon(true);
		mthread.start();
	}

	public void init(Properties args) throws Exception {
		String str;
		properties.setProperty("useUnicode", "TRUE");
		properties.setProperty("characterEncoding", "UTF-8");// Converters.getDefaultEncodingName());
		properties.setProperty("useServerPrepStmts", args.getProperty("useServerPrepStmts"));
		properties.setProperty("zeroDateTimeBehavior", "convertToNull");
		properties.setProperty("useSSL", "true");
		String coding = (String) args.get("characterEncoding");
		coding = "UTF-8";
		if (coding != null)
			properties.setProperty("characterEncoding", coding);
		driver = (String) args.get("JDBCDriver");
		if (driver == null)
			throw new IllegalArgumentException("no driver");
		url = (String) args.get("JDBCConnectionURL");
		if (url == null)
			throw new IllegalArgumentException("no url");
		user = (String) args.get("User");
		if (user == null)
			throw new IllegalArgumentException("no user");
		properties.setProperty("user", user);
		password = (String) args.get("Password");

//		if (!password.equals("1"))
//			password = new String(Base64.decode(password.getBytes("utf-8")),
//					"utf-8");

		if (password == null)
			throw new IllegalArgumentException("no password");

		properties.setProperty("password", password);
		if ((str = (String) args.get("ConnectionPoolSize")) != null)
			size = Integer.parseInt(str);
		if (size == 0)
			throw new IllegalArgumentException("no size");
		if ((str = (String) args.get("ConnectionPoolMax")) != null)
			maxsize = Integer.parseInt(str);
		if (maxsize == 0)
			throw new IllegalArgumentException("no maxsize");
		if ((str = (String) args.get("ConnectionUseCount")) != null)
			usecount = Integer.parseInt(str);
		if (usecount == 0)
			throw new IllegalArgumentException("no usecount");
		if ((str = (String) args.get("ConnectionTimeout")) != null)
			timeout = Integer.parseInt(str);
		if (timeout == 0)
			throw new IllegalArgumentException("no timeout");
		timeout *= 60000;// ����ʱʱ��ת���ɺ��뵥λ
		if ((str = (String) args.get("ThreadWorkTime")) != null)
			threadworktime = Integer.parseInt(str);
		threadworktime *= 60000;// ���߳�����ʱ��ת���ɺ��뵥λ
		if ((str = (String) args.get("ConnectionCheck")) != null) {
			if (str.equals("false"))
				check = false;
		}
		if ((str = (String) args.get("ConnectionDebug")) != null)
			debug = Integer.parseInt(str);
		// ����jdbc��������
		// java.sql.Driver d = (java.sql.Driver)
		Class.forName(driver);
		// .newInstance();
		initPool();
	}

	/* properties */
	public String getInfo() {
		return "<ConnectionPool driver=" + driver + ", url=" + url
				+ ", length=" + length + " > ";
	}

	public boolean isActive() {
		return active;
	}

	public int length() {
		return length;
	}

	public String getDriver() {
		return driver;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public int getMaxsize() {
		return maxsize;
	}

	/* method */
	/* ��ʼ�����ӳصķ��� */
	private void initPool() throws SQLException {
		pool = new PackConnection[maxsize];
		synchronized (pool) {
			Connection con;
			for (int i = 0; i < size; i++) {
				properties.setProperty("characterEncoding", "UTF-8");
				con = DriverManager.getConnection(url, properties);
				if (length == 0) {
					DatabaseMetaData md = con.getMetaData();
					// Get the maximum number of simultaneous connections as
					// reported by the JDBC driver
					int mn = md.getMaxConnections();
					// ������ݿ�ɵõ������������mn�����õ����������maxsizeС���������������Ϊmn
					if (mn > 0 && mn < maxsize)
						maxsize = mn;
					if (mn > 0 && mn < size)
						size = mn;
				}
				pool[i] = new PackConnection(new DBConnect(con, this));
				length++;
			}
		}
		if (debug > 0)
			trace("initPool ok!");
	}

	/* ��ʾ���ӳ�ÿ�����ӵķ��� */
	public String[] showPool() {
		synchronized (pool) {
			String[] strarr = new String[length];
			int index = 0;
			for (int i = 0; i < pool.length; i++) {
				if (pool[i] == null)
					continue;
				strarr[index++] = i + ":" + pool[i].getInfo();
			}
			return strarr;
		}
	}

	/* �����µ����ӵ�ָ������ֵ�ķ��� */
	private boolean addConnection(int index) {
		if (length >= maxsize)
			return false;
		// ���ø÷����ķ������Ѿ�ͬ����pool�������û��ʹ��ͬ��
		try {
			Connection con = DriverManager.getConnection(url, properties);
			pool[index] = new PackConnection(new DBConnect(con, this));
			length++;
			if (debug > 1)
				trace(index + " addConnection ok!");
			return true;
		} catch (SQLException e) {
		}
		if (debug > 0)
			trace(index + " addConnection err!");
		return false;
	}

	/* ��������ֵ�����ӳ����Ƴ����ӵķ��� */
	private void removeConnection(int index) {
		pool[index] = null;
		length--;
		if (debug > 1)
			trace(index + " removeConnection ok!");
	}

	/* �����ӳ��в���ָ�����ӵķ��� */
	private int find(Connection con) {
		synchronized (pool) {
			for (int i = 0; i < pool.length; i++)
				if (pool[i] != null && con == pool[i].con)
					return i;
		}
		return -1;
	}

	/* �����ӳ��еõ����ӵķ������������������ʧ�ܣ��򷵻ؿ� */
	public Connection getConnection() {
		if (debug > 2)
			trace("getConnection!");
		// livetime = System.currentTimeMillis();
		int index = -1;
		synchronized (pool) {
			PackConnection pc;
			for (int i = 0; i < pool.length; i++) {
				if ((pc = pool[i]) == null) {
					// �õ���һ����λ��
					if (index < 0)
						index = i;
					continue;
				}
				if (!pc.idle)
					continue;
				if (!check)
					return pc.getConnection();
				if (pc.isActive()) {
					// ������ӿ��ã����ú󣬷��ظ�����
					return pc.getConnection();
				} else {
					// ������Ӳ����ã��Ƴ�
					removeConnection(i);
					// ���ø�λ��
					if (index < 0)
						index = i;
				}
			}
			if (index < 0) {
				// if (debug > 0)
				trace("connection pool is full!");
				return null;
			}
			if (addConnection(index))
				return pool[index].getConnection();
			trace("connect return null");
			return null;
		}
	}

	/* �������ӵķ��� */
	public void close(Connection con) {
		if (debug > 2)
			trace("closeConnection!");
		int index = find(con);
		if (index < 0) {
			trace("nofind return");
			return;
		}
		PackConnection pc = pool[index];
		if (usecount > 0 && pc.count >= usecount) {
			// �������ʹ�õĴ���������������������ر�����
			if (debug > 1)
				trace(index + " usecount full!");
			removeConnection(index);
			pc.close();
		} else {
			// ����������Ϊ����
			if (debug > 1)
				trace("set  idel: " + index);
			pc.idle = true;
		}
	}

	/* �������ӳصķ��� */
	public void settle() {
		long currenttime = System.currentTimeMillis();
		synchronized (pool) {
			PackConnection pc;
			for (int i = 0; i < pool.length; i++) {
				if ((pc = pool[i]) == null)
					continue;
				if (pc.idle) {
					// ������ӿ���
					if (pc.isActive()) {
						// ������ӿ���
						// ���С����С�ĳش�С�������
						// if (length <= size)
						// continue;
						// �����ʱ���Ƴ����ر�����
						if (currenttime - pc.lasttime > timeout) {
							if (debug > 1)
								trace(i + " timeout!");
							removeConnection(i);
							pc.close();
						}
					} else {
						// ������Ӳ����ã��Ƴ�
						removeConnection(i);
						// ���С����С�ĳش�С�������µ�����
						if (length < size)
							addConnection(i);
					}
				} else {
					// �����������ʹ��
					if (currenttime - pc.lasttime > timeout) {
						// �����ʱ���ر����Ӳ��Ƴ�
						if (debug > 0)
							trace(i + " timeout! no callback!");
						removeConnection(i);
						pc.close();
						// ���С����С�ĳش�С�������µ�����
						if (length < size)
							addConnection(i);
					}
				}
			}
		}
		if (debug > 0)
			trace("settle! time=" + currenttime);
	}

	/* �������ӳصķ��������������Ӷ��ر� */
	public void cleanup() {
		active = false;
		if (mthread != null)
			mthread.interrupt();
		mthread = null;
		synchronized (pool) {
			for (int i = 0; i < pool.length; i++) {
				if (pool[i] == null)
					continue;
				pool[i].close();
				pool[i] = null;
			}
		}
		trace("cp is cleanup!");
	}

	private void trace(String s) {
		System.out.println(s);
	}

	// inner class
	class PackConnection {
		DBConnect con = null;// The JDBC Connection

		boolean idle = true;// true if this connection is currently in use

		long lasttime = 0;// The last time(in milliseconds)that this

		// connection was used
		int count = 0;// The number of times this connection has been used

		PackConnection(DBConnect con) {
			this.con = con;
			lasttime = System.currentTimeMillis();
		}

		String getInfo() {
			return "Connection=" + con + ",idle=" + idle + ",lasttime="
					+ lasttime + ",count=" + count;
		}

		boolean isActive() {
			try {
				if (!con.isClosed())
					return true;
			} catch (SQLException e) {
			}
			return false;
		}

		DBConnect getConnection() {
			idle = false;
			lasttime = System.currentTimeMillis();
			count++;
			return con;
		}

		void close() {
			try {
				con.con.close();
				if (debug > 1)
					trace("Connection is closed!");
			} catch (SQLException e) {
			}
		}
	}

	class DBConnect implements Connection {

		public Connection con;

		public ConnectionPool pool;

		public DBConnect(Connection con, ConnectionPool pool) {
			this.con = con;
			this.pool = pool;
		}

		@Override
		public void clearWarnings() throws SQLException {
			con.clearWarnings();
		}

		@Override
		public void close() throws SQLException {
			this.pool.close(this);
		}

		@Override
		public void commit() throws SQLException {
			con.commit();
		}

		@Override
		public Array createArrayOf(String typeName, Object[] elements)
				throws SQLException {
			return con.createArrayOf(typeName, elements);
		}

		@Override
		public Blob createBlob() throws SQLException {
			return con.createBlob();
		}

		@Override
		public Clob createClob() throws SQLException {
			return con.createClob();
		}

		@Override
		public NClob createNClob() throws SQLException {
			return con.createNClob();
		}

		@Override
		public SQLXML createSQLXML() throws SQLException {
			return con.createSQLXML();
		}

		@Override
		public Statement createStatement() throws SQLException {
			return con.createStatement();
		}

		@Override
		public Statement createStatement(int resultSetType,
				int resultSetConcurrency) throws SQLException {
			return con.createStatement(resultSetType, resultSetConcurrency);
		}

		@Override
		public Statement createStatement(int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			return con.createStatement(resultSetType, resultSetConcurrency,
					resultSetHoldability);
		}

		@Override
		public Struct createStruct(String typeName, Object[] attributes)
				throws SQLException {
			return con.createStruct(typeName, attributes);
		}

		@Override
		public boolean getAutoCommit() throws SQLException {
			return con.getAutoCommit();
		}

		@Override
		public String getCatalog() throws SQLException {
			return con.getCatalog();
		}

		@Override
		public Properties getClientInfo() throws SQLException {
			return con.getClientInfo();
		}

		@Override
		public String getClientInfo(String name) throws SQLException {
			return con.getClientInfo(name);
		}

		@Override
		public int getHoldability() throws SQLException {
			return con.getHoldability();
		}

		@Override
		public DatabaseMetaData getMetaData() throws SQLException {
			return con.getMetaData();
		}

		@Override
		public int getTransactionIsolation() throws SQLException {
			return con.getTransactionIsolation();
		}

		@Override
		public Map<String, Class<?>> getTypeMap() throws SQLException {
			return con.getTypeMap();
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			return con.getWarnings();
		}

		@Override
		public boolean isClosed() throws SQLException {
			return con.isClosed();
		}

		@Override
		public boolean isReadOnly() throws SQLException {
			return con.isReadOnly();
		}

		@Override
		public boolean isValid(int timeout) throws SQLException {
			return con.isValid(timeout);
		}

		@Override
		public String nativeSQL(String sql) throws SQLException {
			return con.nativeSQL(sql);
		}

		@Override
		public CallableStatement prepareCall(String sql) throws SQLException {
			return con.prepareCall(sql);
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency) throws SQLException {
			return con.prepareCall(sql, resultSetType, resultSetConcurrency);
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			return con.prepareCall(sql, resultSetType, resultSetConcurrency,
					resultSetHoldability);
		}

		@Override
		public PreparedStatement prepareStatement(String sql)
				throws SQLException {
			return con.prepareStatement(sql);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int autoGeneratedKeys) throws SQLException {
			return con.prepareStatement(sql, autoGeneratedKeys);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int[] columnIndexes) throws SQLException {
			return con.prepareStatement(sql, columnIndexes);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				String[] columnNames) throws SQLException {
			return con.prepareStatement(sql, columnNames);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int resultSetType, int resultSetConcurrency)
				throws SQLException {
			return con.prepareStatement(sql, resultSetType,
					resultSetConcurrency);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int resultSetType, int resultSetConcurrency,
				int resultSetHoldability) throws SQLException {
			return con.prepareStatement(sql, resultSetType,
					resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public void releaseSavepoint(Savepoint savepoint) throws SQLException {
			con.releaseSavepoint(savepoint);
		}

		@Override
		public void rollback() throws SQLException {
			con.rollback();
		}

		@Override
		public void rollback(Savepoint savepoint) throws SQLException {
			con.rollback(savepoint);
		}

		@Override
		public void setAutoCommit(boolean autoCommit) throws SQLException {
			con.setAutoCommit(autoCommit);
		}

		@Override
		public void setCatalog(String catalog) throws SQLException {
			con.setCatalog(catalog);
		}

		@Override
		public void setClientInfo(Properties properties)
				throws SQLClientInfoException {
			con.setClientInfo(properties);
		}

		@Override
		public void setClientInfo(String name, String value)
				throws SQLClientInfoException {
			con.setClientInfo(name, value);
		}

		@Override
		public void setHoldability(int holdability) throws SQLException {
			con.setHoldability(holdability);
		}

		@Override
		public void setReadOnly(boolean readOnly) throws SQLException {
			con.setReadOnly(readOnly);
		}

		@Override
		public Savepoint setSavepoint() throws SQLException {
			return con.setSavepoint();
		}

		@Override
		public Savepoint setSavepoint(String name) throws SQLException {
			return con.setSavepoint(name);
		}

		@Override
		public void setTransactionIsolation(int level) throws SQLException {
			con.setTransactionIsolation(level);
		}

		@Override
		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
			con.setTypeMap(map);
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return con.isWrapperFor(iface);
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return con.unwrap(iface);
		}

		public void abort(Executor arg0) throws SQLException {
		}

		public int getNetworkTimeout() throws SQLException {
			return 0;
		}

		public String getSchema() throws SQLException {
			return null;
		}

		public void setNetworkTimeout(Executor arg0, int arg1)
				throws SQLException {
		}

		public void setSchema(String arg0) throws SQLException {
		}
	}

	/* �����߳� */
	class ManageThread extends Thread {
		@Override
		public void run() {
			while (active) {
				try {
					Thread.sleep(threadworktime);
				} catch (InterruptedException e) {
				}
				settle();
			}
		}
	}
}
