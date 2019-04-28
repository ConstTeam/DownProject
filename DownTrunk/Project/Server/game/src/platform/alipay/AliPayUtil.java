package platform.alipay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import app.ServerStaticInfo;
import config.model.recharge.RechargeModel;
import db.RechargeDao;
import module.RechargeInfo;
import util.TimeFormat;
import util.Tools;

public class AliPayUtil {

	public static final String PAY_KEY = "ca6577dff6d647ac882dfb405ceda21e";
	public static final String PAY_SECRETKEY = "1b8da6c9b7544856955fcff6bf920f84";

	public static final String RETURN_URL = "http://103.231.166.31:9004/ali_payment.php";

	public static final String NOTIFY_URL = "http://103.231.166.31:9004/ali_payment.php";

	public static String scanPay(int playerId, RechargeModel rechargeModel, String ip, String payWay, String field1) {

		Map<String, Object> paramMap = new HashMap<String, Object>();
		double orderPrice = rechargeModel.Price - (getPriceDiscount(playerId) * 0.01);
		if (ServerStaticInfo.IS_DEBUG) {
			int random = Tools.random(100, 120);
			orderPrice = random * 0.01; // ������ʱ��
		}
		String price = Tools.decodeToUTF8(String.valueOf(orderPrice));
		paramMap.put("orderPrice", price); // ������� , ��λ:Ԫ

		payWay = Tools.decodeToUTF8(payWay);
		paramMap.put("payWayCode", payWay);// ֧����ʽ���� ֧����: ALIPAY ΢��:WEIXIN

		String orderNo = String.format("%d%s", playerId, TimeFormat.getTSS());
		paramMap.put("orderNo", orderNo); // �������

		Date orderDate = new Date();// ��������
		String orderDateStr = new SimpleDateFormat("yyyyMMdd").format(orderDate);// ��������
		paramMap.put("orderDate", orderDateStr);

		Date orderTime = new Date();// ����ʱ��
		String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime);// ����ʱ��
		paramMap.put("orderTime", orderTimeStr);

		paramMap.put("payKey", PAY_KEY);

		String productName = Tools.decodeToUTF8(rechargeModel.DisplayName);
		paramMap.put("productName", productName); // ��Ʒ����

		paramMap.put("orderIp", ip); // �µ�IP

		paramMap.put("orderPeriod", 30); // ������Ч�ڣ��֣�

		paramMap.put("returnUrl", RETURN_URL); // ҳ��֪ͨ����url

		paramMap.put("notifyUrl", NOTIFY_URL); // ��̨��Ϣ֪ͨUrl

		String remark = Tools.decodeToUTF8(String.format("%d,%s", playerId, rechargeModel.ID)); // ֧����ע
		paramMap.put("remark", remark);

		// ��չ�ֶ�,ѡ��,ԭֵ����
		paramMap.put("field1", field1); // ֧����Դ��WAP����PC
		// ǩ��
		String sign = MerchantApiUtil.getSign(paramMap, PAY_SECRETKEY);
		paramMap.put("sign", sign);

		String paramStr = MerchantApiUtil.getParamStr(paramMap);
		
		RechargeInfo rechargeInfo = new RechargeInfo(playerId, orderNo, rechargeModel.ID, price, rechargeModel.Num, payWay, payWay);
		RechargeDao.addPreRechargeInfo(rechargeInfo);
		
		return paramStr;
	}
	
	public static int getPriceDiscount(int playerId) {
		int discount = 0;
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		long timeInMillis = now.getTimeInMillis();
		long base = (timeInMillis / 100000) % playerId;
		discount = (int) (base % 50);
		return discount;
	}

}
