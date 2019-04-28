package platform.alipay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.ServerStaticInfo;
import config.ConfigData;
import config.model.recharge.RechargeModel;
import db.RechargeDao;
import module.RechargeInfo;
import platform.util.HttpHandlerMethod;
import sys.PayManager;
import util.ErrorPrint;


public class AliPayHttpHandler extends HttpServlet implements HttpHandler {
		
	private static final Logger logger = LoggerFactory.getLogger(AliPayHttpHandler.class);
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void handle(HttpExchange he) throws IOException {
		logger.info("AlipayPayHttpHandler from " + he.getRemoteAddress().toString());
		
		String info = "failure";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			HttpHandlerMethod.parseGetParameters(he, HttpHandlerMethod.NORMAL_WAY);
			
			@SuppressWarnings("unchecked")
			Map<String, Object> attributes = (Map<String, Object>) he.getAttribute("parameters");
			Iterator<Entry<String, Object>> iter =  attributes.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, Object> entry = iter.next();
				String key = entry.getKey();
				Object value = entry.getValue();
				logger.info("key:"+key);
				logger.info("value:"+value);
				if (!key.equals("sign")) {
					paramMap.put(key, value);
				}
			}

			String sign = MerchantApiUtil.getSign(paramMap, AliPayUtil.PAY_SECRETKEY);
			String signMsg = (String) attributes.get("sign");
			signMsg = signMsg.toUpperCase();
			if (!MerchantApiUtil.isRightSign(paramMap, AliPayUtil.PAY_SECRETKEY, signMsg)) {
				logger.error("alipayƽ̨��ֵ��������֤ǩ��ʧ�ܡ�orderSign��{}, sign��{}", (String)attributes.get("sign"), sign);
				return;
			}
			String cpOrder = (String)attributes.get("orderNo"); // ������͸�� ���ܺ���
			@SuppressWarnings("unused")
			String trxNo = (String)attributes.get("trxNo"); // ������͸�� ���ܺ���
			String remark = (String)attributes.get("remark"); // ͸������
			String stateCode = (String)attributes.get("tradeStatus"); // ״̬�� 2Ϊ����ɹ�
			String payWayCode = (String)attributes.get("payWayCode");
			
			double price = Double.parseDouble(attributes.get("orderPrice").toString()); // ʵ�����
			if (!stateCode.equalsIgnoreCase("SUCCESS")) {
				logger.error("alipayƽ̨��ֵ������orderId��{}, tradeStatus��{}", cpOrder, stateCode);
				return;
			}
			
			// ������ʽ ����_��Ʒid_���id
			String[] splitString = remark.split(",");
			String payCode = splitString[1];
			int playerId = Integer.parseInt(splitString[0]);
			logger.info("alipayƽ̨�����ش��ɹ�! ��Ʒid:"+payCode+"����ң�"+playerId);
			
			RechargeModel rechargeModel = ConfigData.rechargeModels.get(String.valueOf(payCode));
			if (rechargeModel == null) {
				info = "success";
				logger.error("alipayƽ̨��ֵ��������ʧ�ܣ�payCode����order_id��" + cpOrder);
				return;
			}
			RechargeInfo rechargeInfo = new RechargeInfo(playerId, cpOrder, payCode, price + "", rechargeModel.Num, payWayCode, payWayCode);
			if (!ServerStaticInfo.IS_DEBUG && rechargeModel.Price > (price + 0.5)){
				info = "success";
				logger.error("alipayƽ̨��ֵ���󶩵�����ʧ�ܣ�����:" + rechargeInfo.getOrderId() + ", �����۸�:" + rechargeInfo.getPrice());
				RechargeDao.addErrorRechargeInfo(rechargeInfo);
				return;
			}
			if (!RechargeDao.addRechargeInfo(rechargeInfo)) {
				logger.error("alipayƽ̨��ֵ��������ʧ�ܣ�����δ�ɹ��������ݿ⣡order_id��" + cpOrder);
				RechargeDao.addErrorRechargeInfo(rechargeInfo);
				info = "success";
			} else {
				PayManager.getInstance().addRechargeInfo(rechargeInfo);
				logger.info("alipayƽ̨��ֵ�������ճɹ���order_id:" + cpOrder);
				info = "success";
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			logger.info("alipayƽ̨��ֵ������Ϣ��"+info);
			he.setAttribute("parameters", null);
			he.setAttribute("get", null);
			he.sendResponseHeaders(200, info.getBytes("UTF-8").length);
			he.getResponseBody().write(info.getBytes("UTF-8"));
			he.getRequestBody().close();
			he.getResponseBody().close();
			he.close();
			logger.info("AlipayPayHttpHandler Over from " + he.getRemoteAddress().toString());
		}
	}
	
	
}
