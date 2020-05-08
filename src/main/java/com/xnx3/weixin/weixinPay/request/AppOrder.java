package com.xnx3.weixin.weixinPay.request;

/**
 * APP 创建订单，订单信息
 * 此APP创建订单尚未验证！！！！
 * @author 管雷鸣
 * @deprecated
 */
public class AppOrder extends Order{
	public static final String TYPE = "APP";		//APP 类型
	public static final String TRADE_TYPE = "APP";	//支付类型
	
	public AppOrder(String openid, int money, String notifyUrl) {
		super(openid, money, notifyUrl);
		super.tradeType = TRADE_TYPE;
		super.type = TYPE;
	}
	
}
