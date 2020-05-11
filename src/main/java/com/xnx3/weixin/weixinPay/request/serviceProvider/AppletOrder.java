package com.xnx3.weixin.weixinPay.request.serviceProvider;

/**
 * 小程序 创建订单，订单信息。这里是服务商使用的
 * (其实这个也是用的JSAPI)
 * <br/> 参数说明： https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1
 * @author 管雷鸣
 *
 */
public class AppletOrder extends com.xnx3.weixin.weixinPay.request.AppletOrder{
	public static final String TYPE = "Applet";	//小程序
	public static final String TRADE_TYPE = "JSAPI";	//支付类型
	
	private String subOpenid;	//openid和sub_openid可以选传其中之一，如果选择传sub_openid,则必须传sub_appid。如果获取用户openid是用的服务商的服务号获取的，那么就传入openid；如果获取用户openid使用的是商户认证的小程序获取的，那么就传入这个子的sub_openid
	
	public AppletOrder(String openid, int money, String notifyUrl) {
		super(openid, money, notifyUrl);
		super.tradeType = TRADE_TYPE;
		super.type = TYPE;
	}

	public String getSubOpenid() {
		return subOpenid;
	}
	
	/**
	 * openid和sub_openid可以选传其中之一，如果选择传sub_openid,则必须传sub_appid。
	 * <br/>如果获取用户openid是用的服务商的服务号获取的，那么就传入openid；
	 * <br/>如果获取用户openid使用的是商户认证的小程序获取的，那么就传入这个子的sub_openid
	 * <br/>执行此方法后，会自动设置openid为空
	 * @param subOpenid 用户子标识，参见 https://pay.weixin.qq.com/wiki/doc/api/jsapi_sl.php?chapter=9_1
	 */
	public void setSubOpenid(String subOpenid) {
		this.subOpenid = subOpenid;
		super.setOpenid("");
	}

	@Override
	public String toString() {
		return "AppletOrder [subOpenid=" + subOpenid + ", toString()=" + super.toString() + "]";
	}
	
}
