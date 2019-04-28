package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ThresholdingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import net.ISession;
import net.ServerAddress;
import util.ErrorPrint;

/**
 *
 */
public class ServerStaticInfo {

	private static final Logger logger = LoggerFactory.getLogger(ThresholdingOutputStream.class);
	
	/** ��������ַ--����������� */
	public static final String HALL_INTERNET = "hallInter";
	/** ������Ϸ�����ַ--����������� */
	public static final String GAME_INTERNET = "gameInter";
	/** �Ƕ�������ַ--����������ӿ� */
	public static final String HALL_MANAGE_PORT = "dcManage";
	/** �Ƕ�������ַ--����������ӿ� */
	public static final String GAME_MANAGE_PORT = "gameManage";

	public static final String DATA_PORT = "dataInter";

	public static final String DATA_MANAGE_PORT = "dataManage";
	
	public static final String GM_PORT = "gmInter";

	public static final String GM_MANAGE_PORT = "gmManage";
	
	public static final String CHAIN_PORT = "chainInter";

	public static boolean IS_DEBUG = false;
	
	public static boolean IS_LOCAL = false;
	
	/** ����������״̬ */
	public static boolean opened = false;
	/** ��ֹ�ַ� */
	public static String[] banNameList = new String[0];
	
	public static String serverType;

	/** ��ַ�б� */
	private static HashMap<String, InetSocketAddress> addrs = new HashMap<>();
	
	/** ������Id */
	private static String serverId;
	/** ����IP */
	private static String publicIp;
	/** ���������� */
	public static String serverName;
	/** ƽ̨���� */
	private static String platform;
	/** ��ǰ�汾�� */
	public static String version;
	/** ��ʼid */
	public static int initRoleId;
	
	public static Calendar clearTime;
	
	public static Calendar zeroClearTime;
	
	public static String startServerTime;
	
	public static int size = 1000;
	
	public static String getPlatform() {
		return platform;
	}

	public static String getServerId() {
		return serverId;
	}

	public static void setServerId(String serverId) {
		ServerStaticInfo.serverId = serverId;
	}

	public static String getServerType() {
		return serverType;
	}

	public static void setServerType(String serverType) {
		ServerStaticInfo.serverType = serverType;
	}

	/**
	 * ����ƽ̨����
	 */
	public static void loadPlatfrom() {
		try {
			Properties props = new Properties();
			Reader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(System.getProperty("etcPath") + "platform.inf"), "gbk"));
			props.load(in);
			
			platform = props.getProperty("platform");
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}
	
	public static void loadConfigPath() {
		// �������ñ�·��
		ConfigData.setConfigPath(System.getProperty("configPath"));
	}
	
	/**
	 * ���ص�ǰ�汾��Ϣ
	 */
	public static void loadVersion() {
		try {
			version = FileUtils.readFileToString(new File(System.getProperty("etcPath") + "version.txt"), "gbk");
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}
	
	public static void loadParam() {
		
		if (System.getProperty("debug") != null) {
			String debug = System.getProperty("debug");
			if (debug.equalsIgnoreCase("true")) {
				ServerStaticInfo.IS_DEBUG = true;
				logger.info("OPEN Debug Model;");
			}
		}
		
		if (System.getProperty("local") != null) {
			IS_LOCAL = System.getProperty("local").equalsIgnoreCase("true");
		}
		if (System.getProperty("serverId") != null) {
			ServerStaticInfo.setServerId(System.getProperty("serverId"));
		} else {
			ServerStaticInfo.setServerId(ServerStaticInfo.getServerType() + "_0");
		}
	
		if (System.getProperty("size") != null) {
			String size = System.getProperty("size");
			ServerStaticInfo.size = Integer.parseInt(size);
		}
	}

	public static void loadLocalServer() {
		if (ServerStaticInfo.IS_LOCAL) {
			String ip = ServerManager.serverAddr.getHostAddress().toString();
			ip = ip.substring(ip.lastIndexOf(".") + 1, ip.length());
			ServerStaticInfo.setServerId(ServerStaticInfo.getServerType() + "_" + ip);
		}
	}

	/**
	 * ��ʼ����ֹ�ַ�
	 */
	public static boolean initBanName() {
		String path = System.getProperty("etcPath") + File.separator + "banname.map";
		String txtStr = null;
		try {
			txtStr = FileUtils.readFileToString(new File(path), "gbk");

			banNameList = txtStr.split("\r\n");

			List<String> banList = new ArrayList<String>();
			for (int i = 0; i < banNameList.length; i++) {
				String ban = banNameList[i];
				if (ban == null) {
					continue;
				}
				if ("".equals(ban.trim())) {
					continue;
				}
				banList.add(ban.trim());
			}
			banNameList = new String[banList.size()];
			banNameList = banList.toArray(banNameList);
			return true;
		} catch (IOException e) {
			ErrorPrint.print(e);
			return false;
		}
	}
	
	/** ��ȡָ���ַ����еĽ�ֹ�ַ� */
	public static String getBanName(String str) {
		if (banNameList != null)
			for (int i = 0; i < banNameList.length; i++) {
				String s = banNameList[i];
				if (str.indexOf(s) >= 0)
					return s;
			}
		return null;
	}
	
	/**
	 * ������ӵ�ַ��Ϣ
	 * @param key
	 * @param addr
	 */
	public static void addAddress(String key, InetSocketAddress addr) {
		addrs.put(key, addr);
	}
	public static InetSocketAddress getAddress(String key) {
		return addrs.get(key);
	}
	
	public static ISession getConn(String key) {
		try {
			InetSocketAddress address = getAddress(key);
			ServerAddress sa = new ServerAddress();
			sa.setAddress(address.getAddress());
			sa.setPort(address.getPort());
			
			ISession session = ServerManager.getDataAccessor().createConnect(sa);
			return session;
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		return null;
	}
	
	public static ISession getConn(String addr, int port) {
		try {
			InetSocketAddress address = new InetSocketAddress(addr, port);
			ServerAddress sa = new ServerAddress();
			sa.setAddress(address.getAddress());
			sa.setPort(port);
			
			ISession session = ServerManager.getDataAccessor().createConnect(sa);
			return session;
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		return null;
	}

	public static String getPublicIp() {
		return publicIp;
	}

	public static void setPublicIp(String publicIp) {
		ServerStaticInfo.publicIp = publicIp;
	}
}
