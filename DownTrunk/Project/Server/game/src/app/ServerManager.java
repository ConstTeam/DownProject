package app;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

import codec.AppMessageCodecFactory;
import db.ConnectionPool;
import handler.gm.QueryAccountHttpHandler;
import message.game.GameServiceConfig;
import message.hall.HallServiceConfig;
import net.DBConnect;
import net.IService;
import net.NBDataAccessBroker;
import net.NIONetServer;
import net.NetServer;
import net.ServiceManager;
import platform.alipay.AliPayHttpHandler;
import platform.gm.GmDisbandRoomHttpHandler;
import platform.gm.GmLogoutHttpHandler;
import redis.RedisProxy;
import util.XMLFactory;
import util.XMLNode;

public class ServerManager {

	private static final Logger logger = LoggerFactory.getLogger(Logger.class);

	private static final String DB_CONFIG = "db.cfg";

	private static final String LOG_DB_CONFIG = "dblog.cfg";
	
	private static final String REDIS_CONFIG = "redis.cfg";

	/** �������б� */
	public static HashMap<String, NetServer> serverMap = new HashMap<String, NetServer>();
	/** �������б� */
	public static HashMap<String, HttpServer> httpServerMap = new HashMap<String, HttpServer>();
	/** WebSocket�����б� */
	public static HashMap<String, WebSocketServer> webserverMap = new HashMap<String, WebSocketServer>();

	/** ���ݿ����ӳ� */
	public static DBConnect gameDBConnect;
	
	public static DBConnect logDBConnect;
	
	public static InetAddress serverAddr;

	/** �Է����ר��������ʹ��� */
	private static NBDataAccessBroker dataAccessor;
	/** ʵ������ */
	private static ServerManager instance = new ServerManager();

	private ServerManager() {
	}

	public static ServerManager getInstance() {
		return instance;
	}

	/**
	 * ��ʼ��
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		/*
		 * ��ʼ����ֹ�ַ�
		 */
		ServerStaticInfo.initBanName();

		serverAddr = InetAddress.getLocalHost();

		initInetAddress();
	}
	
	public void dataAccessInit() {

		/*
		 * ����������ʹ���
		 */
		dataAccessor = new NBDataAccessBroker();
	}

	public void dbInit() throws Exception {

		/*
		 * ��ʼ�����ӳ�
		 */
		String dbConfigPath = System.getProperty("etcPath") + DB_CONFIG;
		ConnectionPool dbpool = new ConnectionPool();
		dbpool.initialize(dbConfigPath);

		gameDBConnect = new DBConnect();
		gameDBConnect.setConnectPool(dbpool);
	}

	public void dbLogInit() throws Exception {

		/*
		 * ��ʼ�����ӳ�
		 */
		String dbConfigPath = System.getProperty("etcPath") + LOG_DB_CONFIG;
		ConnectionPool logDbpool = new ConnectionPool();
		logDbpool.initialize(dbConfigPath);

		logDBConnect = new DBConnect();
		logDBConnect.setConnectPool(logDbpool);
	}
	
	public boolean redisInit() throws Exception {

		/*
		 * ��ʼ�����ӳ�
		 */
		String redisConfigPath = System.getProperty("etcPath") + REDIS_CONFIG;
		return RedisProxy.getInstance().init(redisConfigPath);
	}
	
	/**
	 * ��ʼ����������ַ
	 * 
	 * @throws Exception
	 */
	protected void initInetAddress() throws Exception {
		String path = System.getProperty("etcPath") + "ip.xml";
		String serveletConfig = FileUtils.readFileToString(new File(path), "utf-8");
		XMLNode xmlnode = XMLFactory.getInstance().parseXML(serveletConfig);
		ArrayList<XMLNode> xmlNodes = xmlnode.getSubNodes();
		if (xmlNodes == null) {
			return;
		}

		for (XMLNode node : xmlNodes) {
			String serverName = node.getName();
			String serverId = node.getAttributeValue("Id");
			String ipStr = node.getAttributeValue("Ip");
			String internetPortStr = node.getAttributeValue("InternetPort");
			String managePortStr = node.getAttributeValue("ManagePort");

			InetAddress addr = InetAddress.getByName(ipStr);
			InetAddress hostAddr = InetAddress.getByName(serverAddr.getHostAddress());

			int internetPort = 0;
			int managePort = 0;
			if (!internetPortStr.equals("")) {
				internetPort = Integer.parseInt(internetPortStr);
			}
			if (!managePortStr.equals("")) {
				managePort = Integer.parseInt(managePortStr);
			}
			switch (serverName) {
			case "GameServer":
				if (serverId.equals(ServerStaticInfo.getServerId())) {
					ServerStaticInfo.serverName = node.getAttributeValue("Name");
					ServerStaticInfo.setPublicIp(node.getAttributeValue("PublicIp"));
					if (ServerStaticInfo.IS_LOCAL) {
						ServerStaticInfo.setPublicIp(ServerManager.serverAddr.getHostAddress().toString());
					}
					ServerStaticInfo.addAddress(ServerStaticInfo.GAME_INTERNET, new InetSocketAddress(addr, internetPort));
					ServerStaticInfo.addAddress(ServerStaticInfo.GAME_MANAGE_PORT, new InetSocketAddress(InetAddress.getByName("127.0.0.1"), managePort));
				}
				break;
			case "HallServer":
				if (serverId.equals(ServerStaticInfo.getServerId())) {
					ServerStaticInfo.serverName = node.getAttributeValue("Name");
					ServerStaticInfo.addAddress(ServerStaticInfo.HALL_INTERNET, new InetSocketAddress(addr, internetPort));
					ServerStaticInfo.addAddress(ServerStaticInfo.HALL_MANAGE_PORT, new InetSocketAddress(InetAddress.getByName("127.0.0.1"), managePort));
				}
				break;
			case "DataServer":
				ServerStaticInfo.addAddress(ServerStaticInfo.DATA_PORT, new InetSocketAddress(ipStr.equals("") ? hostAddr : addr, internetPort));
				ServerStaticInfo.addAddress(ServerStaticInfo.DATA_MANAGE_PORT, new InetSocketAddress(InetAddress.getByName("127.0.0.1"), managePort));
				break;
			case "GMServer":
				ServerStaticInfo.addAddress(ServerStaticInfo.GM_PORT, new InetSocketAddress(ipStr.equals("") ? hostAddr : addr, internetPort));
				ServerStaticInfo.addAddress(ServerStaticInfo.GM_MANAGE_PORT, new InetSocketAddress(InetAddress.getByName("127.0.0.1"), managePort));
				break;
			case "ChainServer":
				ServerStaticInfo.addAddress(ServerStaticInfo.CHAIN_PORT, new InetSocketAddress(ipStr.equals("") ? hostAddr : addr, internetPort));
				break;
			}
		}
	}

	/**
	 * �����ͻ��˼���
	 * 
	 * @return ����ɹ������ͻ��˼������򷵻�true
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public boolean startClientServer()
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		NIONetServer server = new NIONetServer();
		// server.setEncrypt(new AppEncrypt());
		server.setOrder(true);
		HallClientHandler clientHandler = new HallClientHandler();
		ServiceManager serviceManager = new ServiceManager();

		// ��ʼ�������ó���service
		HashMap<Integer, Class<?>> clientServices = HallServiceConfig.getInstance().getServices();
		Iterator<Entry<Integer, Class<?>>> iterator = clientServices.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Class<?>> next = iterator.next();
			int moduleId = next.getKey();
			IService service = (IService) next.getValue().newInstance();
			serviceManager.addService(moduleId, service);
		}
		clientHandler.setServersManager(serviceManager);

		server.addNetServerListener(clientHandler);
		// ���ö˿�
		server.setPort(ServerStaticInfo.getAddress(ServerStaticInfo.HALL_INTERNET).getPort());
		// ���ý�����
		server.setMessageCodecFactory(new AppMessageCodecFactory());

		if (server.start()) {
			serverMap.put("phone", server);
		} else {
			return false;
		}
		return true;
	}
	
	public boolean startGameClientServer()
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		NIONetServer server = new NIONetServer();
		// server.setEncrypt(new AppEncrypt());
		server.setOrder(true);
		GameClientHandler clientHandler = new GameClientHandler();
		ServiceManager serviceManager = new ServiceManager();

		// ��ʼ�������ó���service
		HashMap<Integer, Class<?>> clientServices = GameServiceConfig.getInstance().getServices();
		Iterator<Entry<Integer, Class<?>>> iterator = clientServices.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Class<?>> next = iterator.next();
			int moduleId = next.getKey();
			IService service = (IService) next.getValue().newInstance();
			serviceManager.addService(moduleId, service);
		}
		clientHandler.setServersManager(serviceManager);

		server.addNetServerListener(clientHandler);
		// ���ö˿�
		server.setPort(ServerStaticInfo.getAddress(ServerStaticInfo.GAME_INTERNET).getPort());
		// ���ý�����
		server.setMessageCodecFactory(new AppMessageCodecFactory());

		if (server.start()) {
			serverMap.put("phone", server);
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * ��������˿ڼ������߳�����1��
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 */
	public boolean startManageServer(String port)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		HttpServer server = null;
		server = HttpServer.create(ServerStaticInfo.getAddress(port), 0);

		server.createContext("/", new ManagementHttpHandler());
		server.setExecutor(null);
		server.start();

		return true;
	}

	/**
	 * GM�˿ڼ���
	 * 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public boolean startGMServer()
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		HttpServer server = null;
		if (ServerStaticInfo.getAddress(ServerStaticInfo.GM_PORT) == null) {
			return false;
		}
		server = HttpServer.create(ServerStaticInfo.getAddress(ServerStaticInfo.GM_PORT), 0);
		server.createContext("/gm/queryaccount", new QueryAccountHttpHandler());
		logger.info(server.getAddress().getHostString() + ":" + server.getAddress().getPort() + "/gm/*");
		
		server.setExecutor(null);
		server.start();
		
		httpServerMap.put("GM", server);
		return true;

	}

	public boolean startSDKServer()
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		HttpServer server = null;
		server = HttpServer.create(ServerStaticInfo.getAddress(ServerStaticInfo.DATA_PORT), 0);

		server.createContext("/ali_payment.php", new AliPayHttpHandler());
		logger.info(server.getAddress().getHostString() + ":" + server.getAddress().getPort() + "/ali_payment.php");
		
		server.createContext("/player_logout.php", new GmLogoutHttpHandler());
		logger.info(server.getAddress().getHostString() + ":" + server.getAddress().getPort() + "/player_logout.php");
		
		server.createContext("/disband_room.php", new GmDisbandRoomHttpHandler());
		logger.info(server.getAddress().getHostString() + ":" + server.getAddress().getPort() + "/disband_room.php");
		
		server.setExecutor(null);
		server.start();

		httpServerMap.put("SDK", server);
		return true;
	}
	
	public static void addWebSocketServer(String name, WebSocketServer webSocketServer) {
		webserverMap.put(name, webSocketServer);
	}

	/**
	 * �رշ�����
	 */
	public static void closeAll() {
		Iterator<Entry<String, NetServer>> iterator = serverMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, NetServer> entry = iterator.next();
			NetServer service = entry.getValue();
			service.stop();
		}
		
		Iterator<Entry<String, HttpServer>> httpIter = httpServerMap.entrySet().iterator();
		while (httpIter.hasNext()) {
			Entry<String, HttpServer> entry = httpIter.next();
			HttpServer httpServer = entry.getValue();
			 // ������Ϣδ�����꣬���ȴ�2���رա�
			httpServer.stop(2);
		}
	}

	public static NBDataAccessBroker getDataAccessor() {
		return dataAccessor;
	}
}
