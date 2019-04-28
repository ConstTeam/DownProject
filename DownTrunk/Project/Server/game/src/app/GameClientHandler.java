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
import sys.GameServerOnlineManager;

/**
 * �������Կͻ��˼���Handler
 * 
 */
public class GameClientHandler implements NetServerListener, NetConnectionListener {

	private static final Logger logger = LoggerFactory.getLogger(GameClientHandler.class);

	/**
	 * �Ự���г�ʱ
	 */
	private static final long SESSION_IDLE = 150 * 1000;

	/** ��������� */
	public ServiceManager serversManager;

	/** ÿ��cpuִ�е��߳��� */
	private final int POOL_SIZE = ServerStaticInfo.size;

	/** �̳߳� */
	public ExecutorService excutorService;

	/** ������ */
	protected long connectionCount = 0;

	public GameClientHandler() {
		excutorService = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() * POOL_SIZE, 60L,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	public GameClientHandler(int size) {
		excutorService = Executors.newFixedThreadPool(size);
	}

	@Override
	public void connectionClosed(NetConnection netconnection) {
		netconnection.close();
		GameServerOnlineManager.getInstance().playerLogout(netconnection);
		logger.info("���ӹر�-- " + netconnection.getHost() + ":" + netconnection.getPort());
	}

	@Override
	public void connectionOpened(NetConnection netconnection) {
		connectionCount++;
		netconnection.setServiceManager(serversManager);
		netconnection.addListener(this);
		netconnection.setIdle(SESSION_IDLE);
		logger.info("���ӿ���--");
	}

	/** ����Ϣ����ʱ */
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

	/** �������� */
	public void serverOpened() {

	}

	/** ����ر� */
	public void serverClosed() {
		this.serversManager.destory();
	}

}
