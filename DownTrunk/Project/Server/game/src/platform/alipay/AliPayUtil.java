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
			orderPrice = random * 0.01; // 测试临时用
		}
		String price = Tools.decodeToUTF8(String.valueOf(orderPrice));
		paramMap.put("orderPrice", price); // 订单金额 , 单位:元

		payWay = Tools.decodeToUTF8(payWay);
		paramMap.put("payWayCode", payWay);// 支付方式编码 支付宝: ALIPAY 微信:WEIXIN

		String orderNo = String.format("%d%s", playerId, TimeFormat.getTSS());
		paramMap.put("orderNo", orderNo); // 订单编号

		Date orderDate = new Date();// 订单日期
		String orderDateStr = new SimpleDateFormat("yyyyMMdd").format(orderDate);// 订单日期
		paramMap.put("orderDate", orderDateStr);

		Date orderTime = new Date();// 订单时间
		String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime);// 订单时间
		paramMap.put("orderTime", orderTimeStr);

		paramMap.put("payKey", PAY_KEY);

		String productName = Tools.decodeToUTF8(rechargeModel.DisplayName);
		paramMap.put("productName", productName); // 商品名称

		paramMap.put("orderIp", ip); // 下单IP

		paramMap.put("orderPeriod", 30); // 订单有效期（分）

		paramMap.put("returnUrl", RETURN_URL); // 页面通知返回url

		paramMap.put("notifyUrl", NOTIFY_URL); // 后台消息通知Url

		String remark = Tools.decodeToUTF8(String.format("%d,%s", playerId, rechargeModel.ID)); // 支付备注
		paramMap.put("remark", remark);

		// 扩展字段,选填,原值返回
		paramMap.put("field1", field1); // 支付来源：WAP或者PC
		// 签名
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
