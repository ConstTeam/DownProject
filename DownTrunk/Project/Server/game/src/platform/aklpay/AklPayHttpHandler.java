package platform.aklpay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

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
import util.Tools;


public class AklPayHttpHandler extends HttpServlet implements HttpHandler {
		
	private static final Logger logger = Logger.getLogger(AklPayHttpHandler.class);
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void handle(HttpExchange he) throws IOException {
		logger.info("AklPayHttpHandler from " + he.getRemoteAddress().toString());
		
		String info = "failure";
		ArrayList<String> keyList = new ArrayList<>();
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
				keyList.add(key);
			}
			String string = (String) he.getAttribute("get");
			string = string.substring(0, string.lastIndexOf("signMsg"));
			StringBuffer sb = new StringBuffer();
			sb.append(string);
			sb.append("pkey=");
			sb.append(AklPayPlatformUtil.SECRETKEY);
			@SuppressWarnings("unused")
			String appId = (String)attributes.get("partnerID"); // �̻�Id
			String cpOrder = (String)attributes.get("orderID"); // ������͸�� ���ܺ���
			String remark = (String)attributes.get("remark"); // ͸������
			@SuppressWarnings("unused")
			String stateCode = (String)attributes.get("stateCode"); // ״̬�� 2Ϊ����ɹ�
			int payfee = Integer.parseInt(attributes.get("payAmount").toString()); // ʵ�����
			double price = payfee * 0.01;
			
			String signMsg = (String)attributes.get("signMsg");
			logger.info("aklPayǩ��ǰ���ݣ�"+sb.toString());
			signMsg = signMsg.toLowerCase();
			String mySign = Tools.md5(sb.toString()).toLowerCase();
			if(!signMsg.trim().equals(mySign)){
				logger.error("aklPayƽ̨��֤ǩ��ʧ�ܣ�sign��"+signMsg+"��mySign��"+mySign);
				return;
			}
			// ������ʽ ����_��Ʒid_���id
			String[] splitString = remark.split(",");
			String payCode = splitString[1];
			int playerId = Integer.parseInt(splitString[0]);
			logger.info("aklPayƽ̨�����ش��ɹ�! ��Ʒid:"+payCode+"����ң�"+playerId);
			
			RechargeModel rechargeModel = ConfigData.rechargeModels.get(String.valueOf(payCode));
			if (rechargeModel == null) {
				info = "success";
				logger.error("aklPayƽ̨��ֵ��������ʧ�ܣ�payCode����order_id��" + cpOrder);
				return;
			}
			RechargeInfo rechargeInfo = new RechargeInfo(playerId, cpOrder, payCode, price + "", rechargeModel.Num, "aklpay", "aklpay");
			if (!ServerStaticInfo.IS_DEBUG && rechargeModel.Price > price){
				info = "success";
				logger.error("aklPayƽ̨��ֵ���󶩵�����ʧ�ܣ�����:" + rechargeInfo.getOrderId() + ", �����۸�:" + rechargeInfo.getPrice());
				RechargeDao.addErrorRechargeInfo(rechargeInfo);
				return;
			}
			if (!RechargeDao.addRechargeInfo(rechargeInfo)) {
				logger.error("aklPayƽ̨��ֵ��������ʧ�ܣ�����δ�ɹ��������ݿ⣡order_id��" + cpOrder);
				RechargeDao.addErrorRechargeInfo(rechargeInfo);
			} else {
				PayManager.getInstance().addRechargeInfo(rechargeInfo);
				logger.info("aklPayƽ̨��ֵ�������ճɹ���order_id:" + cpOrder);
				info = "success";
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			logger.info("aklPayƽ̨��ֵ������Ϣ��"+info);
			he.setAttribute("parameters", null);
			he.setAttribute("get", null);
			he.sendResponseHeaders(200, info.getBytes("UTF-8").length);
			he.getResponseBody().write(info.getBytes("UTF-8"));
			he.getRequestBody().close();
			he.getResponseBody().close();
			he.close();
			logger.info("AklPayHttpHandler Over from " + he.getRemoteAddress().toString());
		}
	}
	
	
}
