package platform;


import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import module.RechargeInfo;
import util.ErrorPrint;

public class PayModel {

	private static final Logger logger = Logger.getLogger(PayModel.class);
	
	/** ����ֵ��Ϣ */
	private LinkedBlockingQueue<RechargeInfo> rechargeInfo;

	/** ���ճ�ֵ��Ϣ HashMap<OrderId, RechargeInfo> */
	private HashMap<String, RechargeInfo> rechargeInfos;

	private PayModel() {
		rechargeInfo = new LinkedBlockingQueue<>();
		rechargeInfos = new HashMap<>();
	}

	public static PayModel getInstance() {
		return new PayModel();
	}
	
	public RechargeInfo poll() {
		try {
			return rechargeInfo.take();
		} catch (InterruptedException e) {
			ErrorPrint.print(e);
		}
		return null;
	}
	
	public void redealRechargeInfo(RechargeInfo rinfo) {
		if (rinfo.getRetimes() >= 5) {
			logger.info("��ֵ�����ظ����볬��5�Σ�");
			return;
		}
		rinfo.addRetimes();
		rechargeInfo.add(rinfo);
		logger.info("��ֵ�������µ��룡");
	}
	
	/**
	 * ��ֵ��Ϣ����
	 * @param rinfo
	 */
	public void dealRechargeInfo(RechargeInfo rinfo) {
		rechargeInfo.add(rinfo);
		addRechargeInfo(rinfo);
		logger.info("��ֵ�������գ�");
	}

	/**
	 * ��ֵ��Ϣ����
	 * @param rinfo
	 */
	public void addRechargeInfo(RechargeInfo rinfo) {
		rechargeInfos.put(rinfo.getOrderId(), rinfo);
	}
	
	/**
	 * ��ѯ��ֵ��Ϣ
	 * @param orderId �������
	 * @return �ѳ�ֵ�ɹ�����1��δ��ֵ�ɹ�����0���޴˶�������-1
	 */
	public int getRechargeInfo(String orderId) {
		if (rechargeInfos.get(orderId) == null) {
			return -1;
		}
		if (rechargeInfos.get(orderId).isDeal()) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * �õ���ֵ���
	 * @param orderId �������
	 * @return 
	 */
	public String getRechargePrice(String orderId) {
		return rechargeInfos.get(orderId).getPrice();
	}
	
}