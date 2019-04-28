package platform;


import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import module.RechargeInfo;
import util.ErrorPrint;

public class PayModel {

	private static final Logger logger = Logger.getLogger(PayModel.class);
	
	/** 待充值信息 */
	private LinkedBlockingQueue<RechargeInfo> rechargeInfo;

	/** 当日充值信息 HashMap<OrderId, RechargeInfo> */
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
			logger.info("充值订单重复导入超过5次！");
			return;
		}
		rinfo.addRetimes();
		rechargeInfo.add(rinfo);
		logger.info("充值订单重新导入！");
	}
	
	/**
	 * 充值信息导入
	 * @param rinfo
	 */
	public void dealRechargeInfo(RechargeInfo rinfo) {
		rechargeInfo.add(rinfo);
		addRechargeInfo(rinfo);
		logger.info("充值订单接收！");
	}

	/**
	 * 充值信息导入
	 * @param rinfo
	 */
	public void addRechargeInfo(RechargeInfo rinfo) {
		rechargeInfos.put(rinfo.getOrderId(), rinfo);
	}
	
	/**
	 * 查询充值信息
	 * @param orderId 订单编号
	 * @return 已充值成功返回1，未充值成功返回0，无此订单返回-1
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
	 * 得到充值金额
	 * @param orderId 订单编号
	 * @return 
	 */
	public String getRechargePrice(String orderId) {
		return rechargeInfos.get(orderId).getPrice();
	}
	
}