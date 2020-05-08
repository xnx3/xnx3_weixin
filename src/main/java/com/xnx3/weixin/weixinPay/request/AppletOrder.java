package com.xnx3.weixin.weixinPay.request;

/**
 * 小程序 创建订单，订单信息
 * (其实这个也是用的JSAPI)
 * <br/> 参数说明： https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1
 * @author 管雷鸣
 *
 */
public class AppletOrder extends Order{
	public static final String TYPE = "Applet";	//小程序
	public static final String TRADE_TYPE = "JSAPI";	//支付类型
	
	public AppletOrder(String openid, int money, String notifyUrl) {
		super(openid, money, notifyUrl);
		super.tradeType = TRADE_TYPE;
		super.type = TYPE;
	}
	
}
