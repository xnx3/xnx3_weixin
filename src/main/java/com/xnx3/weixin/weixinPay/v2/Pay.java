package com.xnx3.weixin.weixinPay.v2;

/**
 * JSAPIPay、NativePay、AppPay 等支付的父类
 * @author 管雷鸣
 */
public class Pay {
	public static String UNIFIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	
	/**
	 * 微信支付的商户号，格式如：1581125900 ，登录 https://pay.weixin.qq.com/ 取得
	 */
	public String mch_id = "";
	/**
	 * 微信支付的API密钥，32位字符串。登录 https://pay.weixin.qq.com/  在 [账户中心] - [API安全] - [设置API密钥]
	 */
	public String mch_key = "";
	
	/**
	 * 微信公众号的appid，格式如 wx07f3db3a6bbexxxx
	 */
	public String gongzhonghao_appid = "";
	/**
	 * 微信公众号的 appsecret ，格式如 b067bdd3935962ff8262f51fe5054xxx
	 */
	public String gongzhonghao_appsecret = "";
	
	/**
	 * 微信支付成功后，微信服务器会将支付结果异步通知我们的服务器。这里是异步通知的url。传入如 http://xxx.zvo.cn/weixin.do
	 * 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。 公网域名必须为https，如果是走专线接入，使用专线NAT IP或者私有回调域名可使用http
	 */
	public String notify_url = "";	//支付成功的通知地址
	
	/**
	 * 支付类型
	 * JSAPI -JSAPI支付
	 * NATIVE -Native支付
	 * APP -APP支付
	 */
	String trade_type = "JSAPI"; 
	

	/**
	 * 支付回调中，通知微信服务器，执行成功，不要再给我发这个订单支付成功的消息了
	 * @return <pre>
	 * 				<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>
	 *			</pre>
	 */
	public static String callbackExecuteSuccess() {
		return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
	}
}
