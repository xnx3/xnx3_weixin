package com.xnx3.weixin;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import com.xnx3.BaseVO;
import com.xnx3.DateUtil;
import com.xnx3.Lang;
import com.xnx3.weixin.WeiXinPayUtil;
import com.xnx3.weixin.XmlUtil;
import com.xnx3.weixin.weixinPay.PayCallBackParamsVO;
import com.xnx3.weixin.weixinPay.request.AppletOrder;
import com.xnx3.weixin.weixinPay.request.JSAPIOrder;
import com.xnx3.weixin.weixinPay.request.Order;
import com.xnx3.weixin.weixinPay.response.AppletParamsVO;
import com.xnx3.weixin.weixinPay.response.JSAPIParamsVO;

import net.sf.json.JSONObject;

/**
 * 微信小程序支付
 * @author 管雷鸣
 */
public class WeixinAppletPayUtil {
	public static WeiXinPayUtil weiXinPayUtil;	//创建微信支付 util，只创建一次即可，可多次调用 util.createOrder(....) 进行创建订单支付
	public static String notifyUrl = "http://www.xxx.com/weixin/payCallback.json";	//支付成功后，微信异步回调通知咱的服务器url
	public static String gongzhonghaoAppId;	//微信小程序所关联的微信公众号的appid
	public static String mch_id = "1615645795";
	public static String xiaochengxuappid= "wx78c46179e4ddee65";
	public static String openid = "oa04fwGxDJsbIzzfwp4VPEBNGMdc";		//微信openid
	public static String key = "liaochengshangchengxiaochengxu32";
	public static String parent_mch_id = "1589606251";
	public static String fuwushang_weixingongzhonghoa_id = "wx07f3db3a6bbedfbe";
	
	
	static {
		gongzhonghaoAppId = "wxe69c524f007c2df1";
		
		
		if(gongzhonghaoAppId != null && gongzhonghaoAppId.length() > 0 && mch_id != null && mch_id.length() > 0 && key != null && key.length() > 0) {
			weiXinPayUtil = new WeiXinPayUtil(gongzhonghaoAppId, mch_id, key);
			System.out.println("已开启微信小程序的微信支付配置");
		}else {
			System.out.println("未开启微信小程序的微信支付配置");
		}
		
		notifyUrl = "http//xxxx";
	}
	
	public static void main(String[] args) {
		String openid = "oa04fwGxDJsbIzzfwp4VPEBNGMdc";		//微信openid
		int money = 1;		//要支付的金额，单位是分，这里1便是支付1分
		String no = "NO"+DateUtil.timeForUnix10();
		
		WeixinAppletPayUtil.createPayOrder(openid, money, no);
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
	public static void createPayOrder(String openid, int money, String no) {
		AppletParamsVO vo;
		if(weiXinPayUtil == null) {
			vo = new AppletParamsVO(gongzhonghaoAppId, "");
			vo.setBaseVO(BaseVO.FAILURE, "请先在 application.properties 中配置相关参数。详情参考： https://gitee.com/leimingyun/wm_plugin_weixinapplet_pay");
//			return vo;
			return;
		}
		
		AppletOrder appletOrder = new AppletOrder(openid, money, notifyUrl);
		appletOrder.setOutTradeNo(no);
		
//		weiXinPayUtil.setApplet_appid(xiaochengxuappid);
//		weiXinPayUtil.setServiceProviderSubAppletAppid(xiaochengxuappid);
//		weiXinPayUtil.setServiceProvider_officialAccounts_appid("wx07f3db3a6bbedfbe");
//		serviceProviderCreateOrder(appletOrder);	////JSAPI 方式调起支付，比如微信网页版，就是这种支付方式。注意，如果是小程序支付，需要传入 AppletOrder ， 用 AppletParamsVO 接收
		createOrder(appletOrder);
		//		if(vo.getResult() - AppletParamsVO.SUCCESS == 0){
//			//成功，打印出支付用的 timeStamp、nonceStr、Package.....等参数
//			System.out.println(vo);
//		}else{
//			//失败，通过 getInfo() 获取到失败原因，显示给用户
//			System.out.println(vo.getInfo());
//		}
//		return vo;
	}
	

	/**
	 * 创建订单，(非服务商)，在微信支付那边创建对应的订单
	 * @param order {@link Order} 创建订单的一些参数，比如，如果是 微信H5支付，传入 {@link JSAPIOrder} ,如果是小程序支付，传入 {@link AppletOrder}
	 */
	public static void createOrder(Order order){
        //创建hashmap(用户获得签名)
        SortedMap<String, String> paraMap = new TreeMap<String, String>();
        //设置随机字符串
        String nonceStr = Lang.uuid();
        //设置商户订单号
//	    String outTradeNo = StringUtil.getRandom09AZ(2)+StringUtil.intTo36(DateUtil.timeForUnix10())+StringUtil.getRandom09AZ(2);
        
        //设置请求参数(公众号、小程序ID)
        paraMap.put("appid", xiaochengxuappid);
        //设置请求参数(商户号)
        paraMap.put("mch_id", mch_id);
        //设置请求参数(随机字符串)
        paraMap.put("nonce_str", nonceStr);
        //设置请求参数(商品描述)
        paraMap.put("body", order.getBody());
        //设置请求参数(商户订单号)
        paraMap.put("out_trade_no", order.getOutTradeNo());
        //设置请求参数(总金额)
        paraMap.put("total_fee", order.getTotalFee()+"");
        //设置请求参数(终端IP)
        paraMap.put("spbill_create_ip", order.getClientIp());
        //设置请求参数(通知地址)
        paraMap.put("notify_url", order.getNotifyUrl());
        //设置请求参数(交易类型)
        paraMap.put("trade_type", order.getTradeType());
        paraMap.put("openid", order.getOpenid());
        String sign = SignUtil.generateSign(paraMap, key);
        //将参数 编写XML格式
        StringBuffer paramBuffer = new StringBuffer();
        paramBuffer.append("<xml>");
        paramBuffer.append("<appid>"+xiaochengxuappid+"</appid>");
        paramBuffer.append("<mch_id>"+mch_id+"</mch_id>");
        paramBuffer.append("<nonce_str>"+paraMap.get("nonce_str")+"</nonce_str>");
        paramBuffer.append("<sign>"+sign+"</sign>");
        paramBuffer.append("<body>"+order.getBody()+"</body>");
        paramBuffer.append("<out_trade_no>"+paraMap.get("out_trade_no")+"</out_trade_no>");
        paramBuffer.append("<total_fee>"+paraMap.get("total_fee")+"</total_fee>");
        paramBuffer.append("<spbill_create_ip>"+paraMap.get("spbill_create_ip")+"</spbill_create_ip>");
        paramBuffer.append("<notify_url>"+paraMap.get("notify_url")+"</notify_url>");
        paramBuffer.append("<trade_type>"+paraMap.get("trade_type")+"</trade_type>");
        paramBuffer.append("<openid>"+order.getOpenid()+"</openid>");
        paramBuffer.append("</xml>");
        
        System.out.println("paramBuffer:"+paramBuffer.toString());
        //发送请求(POST)(获得数据包ID)(这有个注意的地方 如果不转码成ISO8859-1则会告诉你body不是UTF8编码 就算你改成UTF8编码也一样不好使 所以修改成ISO8859-1)
        try {
        	String response = HttpsUtil.post(WeiXinPayUtil.UNIFIED_ORDER, new String(paramBuffer.toString().getBytes(), "ISO8859-1"));
			Map<String,String> map = XmlUtil.stringToMap(response);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				//debug("---- "+entry.getKey()+" : "+entry.getValue());
			}
			
			System.out.println(response);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 服务商模式创建订单，在微信支付那边创建对应的订单
	 * @param order {@link Order} 创建订单的一些参数，比如，如果是 微信H5支付，传入 {@link JSAPIOrder} ,如果是小程序支付，传入 {@link AppletOrder}
	 */
	public static void serviceProviderCreateOrder(Order order){
		String subOpenid = openid;	//是否使用的是子openid，服务商模式会有子openid，如果不为null，则使用的是子openid
//		String mch_id = "";
		String serviceProvider_officialAccounts_appid = "wx07f3db3a6bbedfbe";
		
        //创建hashmap(用户获得签名)
        SortedMap<String, String> paraMap = new TreeMap<String, String>();
        //设置随机字符串
        String nonceStr = Lang.uuid();
        //设置商户订单号
//	    String outTradeNo = StringUtil.getRandom09AZ(2)+StringUtil.intTo36(DateUtil.timeForUnix10())+StringUtil.getRandom09AZ(2);
        
        //设置请求参数(公众号、小程序ID)
        paraMap.put("appid", fuwushang_weixingongzhonghoa_id);
        paraMap.put("sub_appid", xiaochengxuappid);
        //设置请求参数(商户号)
        paraMap.put("mch_id", parent_mch_id);
        //设置请求参数(随机字符串)
        paraMap.put("nonce_str", nonceStr);
        //设置请求参数(商品描述)
        paraMap.put("body", order.getBody());
        //设置请求参数(商户订单号)
        paraMap.put("out_trade_no", order.getOutTradeNo());
        //设置请求参数(总金额)
        paraMap.put("total_fee", order.getTotalFee()+"");
        //设置请求参数(终端IP)
        paraMap.put("spbill_create_ip", order.getClientIp());
        //设置请求参数(通知地址)
        paraMap.put("notify_url", order.getNotifyUrl());
//        if(this.sub_mch_id != null && this.sub_mch_id.length() > 0){
        	paraMap.put("sub_mch_id",mch_id);
//        }
        //设置请求参数(交易类型)
        paraMap.put("trade_type", order.getTradeType());
        paraMap.put("sub_openid", subOpenid);
        
        
        String sign = SignUtil.generateSign(paraMap, key);
        //将参数 编写XML格式
        StringBuffer paramBuffer = new StringBuffer();
        paramBuffer.append("<xml>");
        paramBuffer.append("<appid>"+fuwushang_weixingongzhonghoa_id+"</appid>");
        paramBuffer.append("<sub_appid>"+xiaochengxuappid+"</sub_appid>");
        paramBuffer.append("<mch_id>"+parent_mch_id+"</mch_id>");
        paramBuffer.append("<nonce_str>"+paraMap.get("nonce_str")+"</nonce_str>");
        paramBuffer.append("<sign>"+sign+"</sign>");
        paramBuffer.append("<body>"+order.getBody()+"</body>");
        paramBuffer.append("<out_trade_no>"+paraMap.get("out_trade_no")+"</out_trade_no>");
        paramBuffer.append("<total_fee>"+paraMap.get("total_fee")+"</total_fee>");
//        if(this.sub_mch_id != null && this.sub_mch_id.length() > 0){
        	paramBuffer.append("<sub_mch_id>"+ mch_id+"</sub_mch_id>");
//        }
        paramBuffer.append("<spbill_create_ip>"+paraMap.get("spbill_create_ip")+"</spbill_create_ip>");
        paramBuffer.append("<notify_url>"+paraMap.get("notify_url")+"</notify_url>");
        paramBuffer.append("<trade_type>"+paraMap.get("trade_type")+"</trade_type>");
//        if(this.sub_mch_id != null && this.sub_mch_id.length() > 0){
//    		//服务商模式，那有可能会用 sub_openid
//    		if(subOpenid != null){
    			paramBuffer.append("<sub_openid>"+subOpenid+"</sub_openid>");
//    		}else{
//    			paramBuffer.append("<openid>"+order.getOpenid()+"</openid>");
//    		}
//    	}else{
//    		paramBuffer.append("<openid>"+order.getOpenid()+"</openid>");
//    	}
        paramBuffer.append("</xml>");
        
        System.out.println("paramBuffer:"+paramBuffer.toString());
        //发送请求(POST)(获得数据包ID)(这有个注意的地方 如果不转码成ISO8859-1则会告诉你body不是UTF8编码 就算你改成UTF8编码也一样不好使 所以修改成ISO8859-1)
        try {
        	String response = HttpsUtil.post(WeiXinPayUtil.UNIFIED_ORDER, new String(paramBuffer.toString().getBytes(), "ISO8859-1"));
			Map<String,String> map = XmlUtil.stringToMap(response);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				//debug("---- "+entry.getKey()+" : "+entry.getValue());
			}
			
			String return_code = map.get("return_code");	//返回状态码
			if(return_code != null){
				if(return_code.equals("SUCCESS")){
					String result_code = map.get("result_code"); //业务结果
					System.out.println("result_code:"+result_code);
				}else if (return_code.equals("FAIL")) {
					String return_msg = map.get("return_msg");
					if(return_msg != null && return_msg.indexOf("签名错误") > -1){
						String stringA = SignUtil.formatUrlMap(paraMap, false, false);
//						debug("签名错误，签名："+sign+", 签名字符串: "+stringA+"&key="+key);
						System.out.println("签名错误，签名："+sign+", 签名字符串: "+stringA+"&key="+key);
//						debug("可以通过官方签名校验： https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=20_1   验证签名是否对应起来。如果签名没问题，那可能是微信商户的key，重新生成一个新的key使用就好了。");
					}
//					debug("微信支付，创建订单失败, return_code = FAIL , response: "+response);
					System.out.println("微信支付，创建订单失败, return_code = FAIL , response: "+response);
//					return voTransform(order, BaseVO.failure("微信支付，创建订单失败："+return_msg));
				}
			}
//			return voTransform(order, BaseVO.failure("创建订单失败，weixin response:"+response));
			System.out.println("创建订单失败，weixin response:"+response);
        } catch (Exception e) {
			e.printStackTrace();
//			return voTransform(order, BaseVO.failure(e.getMessage()));
		}
	}
	
	
	/**
	 * 微信支付成功的回调，自动验证签名，并检查是不是支付成功的回调，拿到支付成功通知的订单号no
	 * @return <ul>
	 * 				<li>result=BaseVO.SUCCESS 是支付成功，可以用 getInfo() 拿到支付成功的订单号，也就是createPayOrder方法中传入的no订单号</li>
	 * 				<li>result=BaseVO.FAILURE 失败，异常，理论上应该不会这样，开发阶段会碰到，可以直接通过 info 来看失败原因</li>
	 * 			</ul>
	 */
	public static BaseVO weixinpayCallback(HttpServletRequest request){
        String inputLine = "";
        String notityXml = "";
        try {
			while((inputLine = request.getReader().readLine()) != null){
			    notityXml += inputLine;
			}
			//关闭流
            request.getReader().close();
            System.out.println("微信回调内容信息："+notityXml);
		} catch (IOException e) {
			e.printStackTrace();
		}
        if(notityXml.length() > 0){
        	//有信息
        	
        	Map<String,String> map;
        	try {
				map = XmlUtil.stringToMap(notityXml);
			} catch (Exception e) {
				e.printStackTrace();
				return BaseVO.failure("failure");
			}
        	String no = map.get("out_trade_no");	//取到订单号
        	if(no == null){
        		return BaseVO.failure("out_trade_no not find");
        	}
        	
    		//回调验签、以及看其是否是支付成功
    		PayCallBackParamsVO paramVO = weiXinPayUtil.payCallback(notityXml);
    		System.out.println(JSONObject.fromObject(paramVO).toString());
    		if(paramVO.getResult() - PayCallBackParamsVO.FAILURE == 0){
    			return BaseVO.failure(paramVO.getInfo()); 
    		}
        	
    		//是支付成功，那么进行处理支付成功应该操作的事情
    		return BaseVO.success("succ");
        }
        
        return BaseVO.failure("failure");
	}
	
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
