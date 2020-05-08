package com.xnx3.weixin.weixinPay.request;

/**
 * JSAPI 创建订单，订单信息
 * @author 管雷鸣
 *
 */
public class JSAPIOrder extends Order{
	public static final String TYPE = "JSAPI";		//JSAPI 类型
	public static final String TRADE_TYPE = "JSAPI";	//支付类型
	
	public JSAPIOrder(String openid, int money, String notifyUrl) {
		super(openid, money, notifyUrl);
		super.tradeType = TRADE_TYPE;
		super.type = TYPE;
	}
	
}
