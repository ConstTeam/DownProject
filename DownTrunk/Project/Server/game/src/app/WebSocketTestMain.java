package app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WebSocketTestMain {

	public static void main(String[] args) {
		
		try {
			//����udp�ķ���
	        DatagramSocket datagramSocket = new DatagramSocket();
	        //׼�����ݣ������ݷ�װ�����ݰ��С�
	        String data = "Hello Wolrd!";
	        //������һ�����ݰ�
	        InetAddress localHost = InetAddress.getByName("192.168.1.35");
	        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length, localHost, 9902);
	        //����udp�ķ��������ݰ�
	        datagramSocket.send(packet);
	        //�ر���Դ ---ʵ���Ͼ����ͷ�ռ�õĶ˿ں�
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
