package app;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Calendar;

import db.AccountDao;
import module.Account;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;
import net.ServerAddress;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import util.ErrorPrint;
public class TestMain {
	
	public static void main(String[] args) {
		
		try {
			ServerManager.getInstance().dataAccessInit();
			ServerManager.getInstance().dbInit();
			if (!ServerManager.getInstance().redisInit()) {
				System.exit(1);
			}

			
			String ip = "127.0.0.1";
			int port = 7860;
			String platform = "NONE";
			String channel = "test";
			String deviceInfo = "os,Windows 10  (10.0.0) 64bit|dm,All Series (ASUS)|dn,MORESHINE-PC|dt,Desktop|sms,8063|gdn,Intel(R) HD Graphics 4600|gms,2127|pf,WindowsPlayer|sl,Chinese|idfa,-1|idfv,-1";
			String deviceId = "174f4d8caaa80c4b8af47316500b22c6613f80e1";
			String sessionId = "";
			String guide = "";
			ServerAddress sa = new ServerAddress();
			InetSocketAddress address = new InetSocketAddress(ip, port);
			sa.setAddress(address.getAddress());
			sa.setPort(port);
			
			for (int i = 1; i <= 200; i++) {
				String platformId = String.valueOf("Test" + i);
				Thread thread = new Thread(() -> {
					try {
						ISession conn = ServerStaticInfo.getConn(ip, port);
						IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
						data.writeByte(1);
						data.writeByte(1);
						data.writeUTF(platform);
						data.writeUTF(channel);
						data.writeUTF(deviceInfo);
						data.writeUTF(String.valueOf(Calendar.getInstance().getTimeInMillis()));
						data.writeUTF(deviceId);
						data.writeUTF(platformId);
						data.writeUTF(sessionId);
						data.writeUTF(guide);
						conn.send(data);
						
						Thread.sleep(2000);
						
						int playerId = 0;
						Connection connect = ServerManager.gameDBConnect.getDBConnect();
						try {
							Account account = AccountDao.getAccount(connect, platformId, platform, channel);
							if (account == null) {
								return;
							}
							playerId = account.playerId;
						} catch (Exception e) {
							ErrorPrint.print(e);
						}
						
						data.clear();
						data.writeByte(1);
						data.writeByte(2);
						data.writeInt(-1);
						conn.send(data);
						
						for (int j = 1; j <= 100; j++) {
							Thread.sleep(1000);
							PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
							if (playerInfo == null) {
								continue;
							}
							if (playerInfo.getRoomId() == 0) {
								continue;
							}
							ISession gameConn = ServerStaticInfo.getConn(ip, 7861);
							data.clear();
							data.writeByte(101);
							data.writeByte(1);
							data.writeInt(playerId);
							gameConn.send(data);
							System.out.println(platformId + "成功进入房间。");
							break;
						}
					} catch (Exception e) {
						ErrorPrint.print(e);
					}
				});
				thread.start();
				Thread.sleep(100);
			}
			System.out.println("载入完成。");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
