package net;

import java.io.IOException;

public interface DataAccess {

	/** 操作成功的返回码常量 */
	public static final int OK = 200;

	/** 访问指定的地址,指定模块的数据 */
	IByteBuffer access(ServerAddress url, int cmd, IByteBuffer data)
			throws DataAccessException;
	
	IByteBuffer access(ISession session, IByteBuffer data)
			throws DataAccessException;
	/** 通过已经获得的连接，访问指定模块，获得数据 */
	IByteBuffer access(ISession session, int cmd, IByteBuffer data)
			throws DataAccessException;

	/** 异步访问指定的地址,指定模块的数据，response为空时发送非阻塞消息 */
	void access(ServerAddress url, int cmd, IByteBuffer data, Response response)
			throws DataAccessException;

	/** 通过已经获得的连接，异步访问指定模块，获得数据，response为空时发送非阻塞消息 */
	void access(ISession session, int cmd, IByteBuffer data, Response response)
			throws DataAccessException;

	/**
	 * 创建一个新的连接
	 * 
	 * @param url
	 * @return
	 */
	public ISession createConnect(ServerAddress url);

	/** 得到一个远程连接 */
	public ISession getConnect(ServerAddress address) throws IOException,
			DataAccessException;

}
