package app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WebSocketTestMain {

	public static void main(String[] args) {
		
		try {
			//建立udp的服务
	        DatagramSocket datagramSocket = new DatagramSocket();
	        //准备数据，把数据封装到数据包中。
	        String data = "Hello Wolrd!";
	        //创建了一个数据包
	        InetAddress localHost = InetAddress.getByName("192.168.1.35");
	        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length, localHost, 9902);
	        //调用udp的服务发送数据包
	        datagramSocket.send(packet);
	        //关闭资源 ---实际上就是释放占用的端口号
	        datagramSocket.close();

//			Thread t = new Thread(() -> {
//				try {
//					WebSocketServer.startGameWebService();
//				} catch (Exception e) {
//					ErrorPrint.print(e);
//					System.exit(1);
//				}
//			});
//			t.setDaemon(false);
//			t.setName("netty-websocket");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
