package app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.IByteBuffer;
import net.NetConnection;
import net.NetConnectionListener;
import net.NetServerListener;
import net.ServiceManager;
import sys.HallServerOnlineManager;

/**
 * 服务器对客户端监听Handler
 * 
 */
public class HallClientHandler implements NetServerListener, NetConnectionListener {

	private static final Logger logger = LoggerFactory.getLogger(HallClientHandler.class);

	/**
	 * 会话空闲超时
	 */
	private static final long SESSION_IDLE = 150 * 1000;

	/** 服务管理器 */
	public ServiceManager serversManager;

	/** 每个cpu执行的线程数 */
	private final int POOL_SIZE = ServerStaticInfo.size;

	/** 线程池 */
	public ExecutorService excutorService;

	/** 连接数 */
	protected long connectionCount = 0;

	public HallClientHandler() {
		excutorService = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() * POOL_SIZE, 60L,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	public HallClientHandler(int size) {
		excutorService = Executors.newFixedThreadPool(size);
	}

	@Override
	public void connectionClosed(NetConnection netconnection) {
		netconnection.close();
		HallServerOnlineManager.getInstance().playerLogout(netconnection);
		logger.info("连接关闭-- " + netconnection.getHost() + ":" + netconnection.getPort());
	}

	@Override
	public void connectionOpened(NetConnection netconnection) {
		connectionCount++;
		netconnection.setServiceManager(serversManager);
		netconnection.addListener(this);
		netconnection.setIdle(SESSION_IDLE);
		logger.info("连接开启--");
	}

	/** 有消息进入时 */
	public void messageArrived(NetConnection netconnection, IByteBuffer message) {
		if (!excutorService.isShutdown()) {
			excutorService.execute(netconnection);
		}
	}

	public ServiceManager getServersManager() {
		return serversManager;
	}

	public void setServersManager(ServiceManager serversManager) {
		this.serversManager = serversManager;
	}

	/** 服务启动 */
	public void serverOpened() {

	}

	/** 服务关闭 */
	public void serverClosed() {
		this.serversManager.destory();
	}

}
