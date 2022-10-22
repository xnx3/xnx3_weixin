package com.xnx3.weixin.weixinPay.v2;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import com.xnx3.BaseVO;
import com.xnx3.DateUtil;
import com.xnx3.Lang;
import com.xnx3.Log;
import com.xnx3.weixin.HttpsUtil;
import com.xnx3.weixin.SignUtil;
import com.xnx3.weixin.XmlUtil;

/**
 * 微信 Native 支付
 * https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_1
 * @author 管雷鸣
 */
public class NativePay extends Pay{
	
	public NativePay() {
		this.trade_type = "NATIVE";
	}
	
	public static void main(String[] args) {
		String openid = "oa04fwGxDJsbIzzfwp4VPEBNGMdc";		//微信openid
		int money = 1;		//要支付的金额，单位是分，这里1便是支付1分
		String no = "NO"+DateUtil.timeForUnix10();
		
		new NativePay().createOrder(openid, "测试支付", money, no, "127.0.0.1");
		
//		AppletParamsVO vo = WeixinAppletPayUtil.createPayOrder(openid, money, no);
//		System.out.println(vo.toString());
	}
	
	/**
	 * 在微信支付中创建订单并拿到小程序中调起微信支付所需要的参数
	 * @param openid 微信要进行支付的用户的openid
	 * @param money 支付的金额，单位是分，这里1便是支付1分
	 * @param no 自己项目中订单的订单号，当支付成功后，微信回调时会将这个订单号传回来，然后我们系统根据这个来得到当前支付信息是哪个订单支付成功了
	 * @return <ul>
	 * 				<li>result=BaseVO.SUCCESS 是支付成功，返回支付用的 timeStamp、nonceStr、Package.....等参数</li>
	 * 				<li>result=BaseVO.FAILURE 失败，理论上应该不会这样，基本都是开发阶段会碰到，可以直接通过 info 来看失败原因，显示给用户</li>
	 * 			</ul>
	 * @author 管雷鸣
	 */
//	public static void createPayOrder(String openid, int money, String no) {
//		AppletParamsVO vo;
//		
//		AppletOrder appletOrder = new AppletOrder(openid, money, notifyUrl);
//		appletOrder.setOutTradeNo(no);
//		createOrder(appletOrder);
//	}
	

	/**
	 * 创建订单，在微信支付那边创建对应的订单，返回调起支付的链接
	 * @param openid 微信要进行支付的用户的openid，传入如 
	 * @param body 商品名字，传入如 “测试商品”
	 * @param money 支付的金额，单位是分，这里1便是支付1分
	 * @param no 自己项目中订单的订单号，当支付成功后，微信回调时会将这个订单号传回来，然后我们系统根据这个来得到当前支付信息是哪个订单支付成功了
	 * @param spbill_create_ip 用户终端的IP，进行支付的用户的ip
	 * @return 如果成功，info返回的就是调起支付的url，如 weixin://wxpay/bizpayurl?pr=mCpiJQ8zz  这个url发到微信里，可以直接点击就能进行支付。 可以用js将这个url转为一个二维码，实现用户用微信扫码即可调起支付
	 */
	public BaseVO createOrder(String openid, String body, int money, String no, String spbill_create_ip){
        //创建hashmap(用户获得签名)
        SortedMap<String, String> paraMap = new TreeMap<String, String>();
        //设置随机字符串
        String nonceStr = Lang.uuid();
        //设置商户订单号
//	    String outTradeNo = StringUtil.getRandom09AZ(2)+StringUtil.intTo36(DateUtil.timeForUnix10())+StringUtil.getRandom09AZ(2);
        
        //设置请求参数(公众号、小程序ID)
        paraMap.put("appid", this.gongzhonghao_appid);
        //设置请求参数(商户号)
        paraMap.put("mch_id", this.mch_id);
        //设置请求参数(随机字符串)
        paraMap.put("nonce_str", nonceStr);
        //设置请求参数(商品描述)
        paraMap.put("body", body);
        //设置请求参数(商户订单号)
        paraMap.put("out_trade_no", no);
        //设置请求参数(总金额)
        paraMap.put("total_fee", money+"");
        //设置请求参数(终端IP)
        paraMap.put("spbill_create_ip", spbill_create_ip);
        //设置请求参数(通知地址)
        paraMap.put("notify_url", super.notify_url);
        //设置请求参数(交易类型)
        paraMap.put("trade_type", this.trade_type);
        paraMap.put("openid", openid);
        String sign = SignUtil.generateSign(paraMap, this.mch_key);
        //将参数 编写XML格式
        StringBuffer paramBuffer = new StringBuffer();
        paramBuffer.append("<xml>");
        paramBuffer.append("<appid>"+this.gongzhonghao_appid+"</appid>");
        paramBuffer.append("<mch_id>"+this.mch_id+"</mch_id>");
        paramBuffer.append("<nonce_str>"+paraMap.get("nonce_str")+"</nonce_str>");
        paramBuffer.append("<sign>"+sign+"</sign>");
        paramBuffer.append("<body>"+body+"</body>");
        paramBuffer.append("<out_trade_no>"+paraMap.get("out_trade_no")+"</out_trade_no>");
        paramBuffer.append("<total_fee>"+paraMap.get("total_fee")+"</total_fee>");
        paramBuffer.append("<spbill_create_ip>"+paraMap.get("spbill_create_ip")+"</spbill_create_ip>");
        paramBuffer.append("<notify_url>"+super.notify_url+"</notify_url>");
        paramBuffer.append("<trade_type>"+paraMap.get("trade_type")+"</trade_type>");
        paramBuffer.append("<openid>"+openid+"</openid>");
        paramBuffer.append("</xml>");
        
        //发送请求(POST)(获得数据包ID)(这有个注意的地方 如果不转码成ISO8859-1则会告诉你body不是UTF8编码 就算你改成UTF8编码也一样不好使 所以修改成ISO8859-1)
        try {
        	String response = HttpsUtil.post(super.UNIFIED_ORDER, new String(paramBuffer.toString().getBytes(), "ISO8859-1"));
			Map<String,String> map = XmlUtil.stringToMap(response);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				//debug("---- "+entry.getKey()+" : "+entry.getValue());
			}
			String return_code = map.get("return_code");	//返回状态码
			
			if (return_code.equals("FAIL")) {
				String return_msg = map.get("return_msg");
				if(return_msg != null && return_msg.indexOf("签名错误") > -1){
					String stringA = SignUtil.formatUrlMap(paraMap, false, false);
					Log.error("签名错误，签名："+sign+", 签名字符串: "+stringA+"&key="+this.mch_key);
					Log.error("可以通过官方签名校验： https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=20_1   验证签名是否对应起来。如果签名没问题，那可能是微信商户的key，重新生成一个新的key使用就好了。");
				}
				Log.error("微信支付，创建订单失败, return_code = FAIL , response: "+response);
				return BaseVO.failure(response);
			}else if(return_code.equalsIgnoreCase("SUCCESS")) {
				//成功
				return BaseVO.success(map.get("code_url"));
			}else {
				//异常
				Log.error("未能判断是成功还是失败！响应："+response);
				return BaseVO.failure("未能判断是成功还是失败！响应："+response);
			}
        } catch (Exception e) {
			e.printStackTrace();
			return BaseVO.failure(e.getMessage());
		}
	}
	
	
}
