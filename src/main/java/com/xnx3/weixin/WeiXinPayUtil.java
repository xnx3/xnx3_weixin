package com.xnx3.weixin;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import com.xnx3.BaseVO;
import com.xnx3.Lang;
import com.xnx3.weixin.weixinPay.PayCallBackParamsVO;
import com.xnx3.weixin.weixinPay.request.AppletOrder;
import com.xnx3.weixin.weixinPay.request.JSAPIOrder;
import com.xnx3.weixin.weixinPay.request.Order;
import com.xnx3.weixin.weixinPay.response.AppParamsVO;
import com.xnx3.weixin.weixinPay.response.AppletParamsVO;
import com.xnx3.weixin.weixinPay.response.JSAPIParamsVO;

/**
 * 微信支付
 * @author 管雷鸣
 *
 */
public class WeiXinPayUtil implements java.io.Serializable{
	//统一下单.商户在小程序中先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付交易后调起支付。
	public static final String UNIFIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	
	/**
	 * 公众号、小程序等appid
	 * @deprecated 使用 {@link #setOfficialAccounts_appid(String)} 、 {@link #setApplet_appid(String)} 代替
	 */
	private String appid;
	private String mch_id;	//商户号
	private String key;		//商户key,在微信商户平台-帐户设置-安全设置-API安全-API密钥-设置API密钥这个里面设置的KEY
	private String sub_mch_id = null;	//子商户号。如果这里设置了，那么上面的 mch_id 便是服务商的商户号
	private String sub_applet_appid = null;	//商户自己的小程序appid。如果这里设置了，那么上面的 appid 便是服务商的微信服务号appid
	
	private String officialAccounts_appid;	//微信公众号的appid
	private String applet_appid;			//微信小程序的appid
	private String serviceProvider_officialAccounts_appid;	//服务商的微信公众号appid
	
	
	/**
	 * 微信公众号的appid
	 */
	public String getOfficialAccounts_appid() {
		return officialAccounts_appid;
	}
	/**
	 * 微信公众号的appid
	 * @param officialAccounts_appid 微信公众号的appid
	 */
	public void setOfficialAccounts_appid(String officialAccounts_appid) {
		this.officialAccounts_appid = officialAccounts_appid;
	}
	/**
	 * 微信小程序的appid
	 */
	public String getApplet_appid() {
		return applet_appid;
	}
	/**
	 * 微信小程序的appid
	 * @param applet_appid 微信小程序的appid
	 */
	public void setApplet_appid(String applet_appid) {
		this.applet_appid = applet_appid;
	}
	
	/**
	 * 服务商的微信公众号appid
	 */
	public String getServiceProvider_officialAccounts_appid() {
		return serviceProvider_officialAccounts_appid;
	}
	/**
	 * 服务商的微信公众号appid
	 * @param serviceProvider_officialAccounts_appid 服务商的微信公众号appid
	 */
	public void setServiceProvider_officialAccounts_appid(String serviceProvider_officialAccounts_appid) {
		this.serviceProvider_officialAccounts_appid = serviceProvider_officialAccounts_appid;
	}
	/**
	 * 创建微信支付工具
	 * @param appid 小程序appid、微信公众号appid 等，已废弃
	 * @param mch_id 商户号，传入如 1591496141
	 * @param key 商户key，在微信商户平台-帐户设置-安全设置-API安全-API密钥-设置API密钥这个里面设置的KEY
	 */
	public WeiXinPayUtil(String appid, String mch_id, String key) {
		this.appid = appid;
		this.mch_id = mch_id;
		this.key = key;
	}
	
	/**
	 * 开启服务商模式，小程序、公众号等都是使用服务商的
	 * <p>也就是设置子商户号。如果这里设置了，那么new创建这个类的时候，设置的 mch_id 便是服务商的商户号</p>
	 * @param sub_mch_id 子商户号，传入如 1591496140 
	 */
	public void openServiceProviderMode(String sub_mch_id) {
		this.sub_mch_id = sub_mch_id;
	}
	
	/**
	 * 设置服务商模式下，商户(用户)自己认证的小程序appid。
	 * <p> {@link #openServiceProviderMode(String)} 设置了这个之后，此处才有效</p>
	 * @param sub_applet_appid 子商户自己的小程序appid
	 */
	public void setServiceProviderSubAppletAppid(String sub_applet_appid){
		this.sub_applet_appid = sub_applet_appid;
	}
	

	/**
	 * 创建订单，在微信支付那边创建对应的订单
	 * @param order {@link Order} 创建订单的一些参数，比如，如果是 微信H5支付，传入 {@link JSAPIOrder} ,如果是小程序支付，传入 {@link AppletOrder}
	 */
	public BaseVO createOrder(Order order){
		debug("微信 统一下单 接口调用");
		
		String appid;	//支付对应的appid
		String subOpenid = null;	//是否使用的是子openid，服务商模式会有子openid，如果不为null，则使用的是子openid
		
		//判断是否是服务商模式，如果是服务商模式，有可能会使用sub_openid
		String className = order.getClass().getName();
		if(className.indexOf("com.xnx3.weixin.weixinPay.request.serviceProvider.") > -1){
			//是服务商模式，那么appid为服务商的微信服务号appid
			appid = this.serviceProvider_officialAccounts_appid;
			
			if(className.equals("com.xnx3.weixin.weixinPay.request.serviceProvider.AppletOrder")){
				com.xnx3.weixin.weixinPay.request.serviceProvider.AppletOrder spao = (com.xnx3.weixin.weixinPay.request.serviceProvider.AppletOrder) order;
				if(spao.getSubOpenid() != null && spao.getSubOpenid().length() > 0){
					//使用子openid
					subOpenid = spao.getSubOpenid();
				}
			}else if (className.equals("com.xnx3.weixin.weixinPay.request.serviceProvider.JSAPIOrder")) {
				com.xnx3.weixin.weixinPay.request.serviceProvider.JSAPIOrder spjo = (com.xnx3.weixin.weixinPay.request.serviceProvider.JSAPIOrder) order;
				if(spjo.getSubOpenid() != null && spjo.getSubOpenid().length() > 0){
					//使用子openid
					subOpenid = spjo.getSubOpenid();
				}
			}
		}else{
			//不是服务商模式
			
			if(order.getType().equals(JSAPIOrder.TYPE)){
	        	//JSAPI，那么使用公众号的appid
				appid = this.officialAccounts_appid;
	        }else if(order.getType().equals(AppletOrder.TYPE)){
	        	//小程序，使用小程序的appid
	        	appid = this.applet_appid;
	        }else{
	        	//其他。。。
	        	return voTransform(order, BaseVO.failure("目前只有JSAPI、小程序支付，其他的还没加，联系微信 xnx3com 让他来增加吧"));
	        }
		}
		
		
        //创建hashmap(用户获得签名)
        SortedMap<String, String> paraMap = new TreeMap<String, String>();
        //设置随机字符串
        String nonceStr = Lang.uuid();
        //设置商户订单号
//	    String outTradeNo = StringUtil.getRandom09AZ(2)+StringUtil.intTo36(DateUtil.timeForUnix10())+StringUtil.getRandom09AZ(2);
        
        //设置请求参数(公众号、小程序ID)
        paraMap.put("appid", appid);
        //设置请求参数(商户号)
        paraMap.put("mch_id", this.mch_id);
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
        if(this.sub_mch_id != null && this.sub_mch_id.length() > 0){
        	paraMap.put("sub_mch_id", this.sub_mch_id);
        }
        //设置请求参数(交易类型)
        paraMap.put("trade_type", order.getTradeType());
        if(order.getTradeType().equals(JSAPIOrder.TRADE_TYPE) || order.getTradeType().equals(AppletOrder.TRADE_TYPE)){
        	//JSAPI、小程序支付 需要有openid参与签名。(在接口文档中 该参数 是否必填项 但是一定要注意 如果交易类型设置成'JSAPI'则必须传入openid)
        	if(this.sub_mch_id != null && this.sub_mch_id.length() > 0){
        		//服务商模式，那有可能会用 sub_openid
        		if(subOpenid != null){
        			paraMap.put("sub_openid", subOpenid);
        		}else{
        			paraMap.put("openid", order.getOpenid());
        		}
        	}else{
        		paraMap.put("openid", order.getOpenid());
        	}
        }
        String sign = SignUtil.generateSign(paraMap, key);
        //将参数 编写XML格式
        StringBuffer paramBuffer = new StringBuffer();
        paramBuffer.append("<xml>");
        paramBuffer.append("<appid>"+appid+"</appid>");
        paramBuffer.append("<mch_id>"+this.mch_id+"</mch_id>");
        paramBuffer.append("<nonce_str>"+paraMap.get("nonce_str")+"</nonce_str>");
        paramBuffer.append("<sign>"+sign+"</sign>");
        paramBuffer.append("<body>"+order.getBody()+"</body>");
        paramBuffer.append("<out_trade_no>"+paraMap.get("out_trade_no")+"</out_trade_no>");
        paramBuffer.append("<total_fee>"+paraMap.get("total_fee")+"</total_fee>");
        if(this.sub_mch_id != null && this.sub_mch_id.length() > 0){
        	paramBuffer.append("<sub_mch_id>"+this.sub_mch_id+"</sub_mch_id>");
        }
        paramBuffer.append("<spbill_create_ip>"+paraMap.get("spbill_create_ip")+"</spbill_create_ip>");
        paramBuffer.append("<notify_url>"+paraMap.get("notify_url")+"</notify_url>");
        paramBuffer.append("<trade_type>"+paraMap.get("trade_type")+"</trade_type>");
        if(this.sub_mch_id != null && this.sub_mch_id.length() > 0){
    		//服务商模式，那有可能会用 sub_openid
    		if(subOpenid != null){
    			paramBuffer.append("<sub_openid>"+subOpenid+"</sub_openid>");
    		}else{
    			paramBuffer.append("<openid>"+order.getOpenid()+"</openid>");
    		}
    	}else{
    		paramBuffer.append("<openid>"+order.getOpenid()+"</openid>");
    	}
        paramBuffer.append("</xml>");
        
        debug("paramBuffer:"+paramBuffer.toString());
        //发送请求(POST)(获得数据包ID)(这有个注意的地方 如果不转码成ISO8859-1则会告诉你body不是UTF8编码 就算你改成UTF8编码也一样不好使 所以修改成ISO8859-1)
        try {
        	String response = HttpsUtil.post(UNIFIED_ORDER, new String(paramBuffer.toString().getBytes(), "ISO8859-1"));
			Map<String,String> map = XmlUtil.stringToMap(response);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				//debug("---- "+entry.getKey()+" : "+entry.getValue());
			}
			
			String return_code = map.get("return_code");	//返回状态码
			if(return_code != null){
				if(return_code.equals("SUCCESS")){
					String result_code = map.get("result_code"); //业务结果
					if(result_code != null && result_code.equals("SUCCESS")){
						//成功
						
						if(order.getType().equals(JSAPIOrder.TYPE)){
				        	//JSAPI
							return new JSAPIParamsVO(appid, map.get("prepay_id")).generateSign(this.key);
				        }else if(order.getType().equals(AppletOrder.TYPE)){
				        	//小程序
				        	String signAppid = appid;
				        	if(this.sub_mch_id != null && this.sub_mch_id.length() > 0 && this.sub_applet_appid != null && this.sub_applet_appid.length() > 0){
								signAppid = this.sub_applet_appid;
							}
				        	return new AppletParamsVO(signAppid, map.get("prepay_id")).generateSign(this.key);
				        }else{
				        	//其他。。。
				        	return voTransform(order, BaseVO.failure("目前只有JSAPI、小程序支付，其他的还没加，联系微信 xnx3com 让他来增加吧"));
				        }
					}else{
						debug("微信支付，业务结果失败 , response: "+response);
						return voTransform(order, BaseVO.failure("微信支付，业务结果失败："+response));
					}
				}else if (return_code.equals("FAIL")) {
					String return_msg = map.get("return_msg");
					if(return_msg != null && return_msg.indexOf("签名错误") > -1){
						String stringA = SignUtil.formatUrlMap(paraMap, false, false);
						debug("签名错误，签名："+sign+", 签名字符串: "+stringA+"&key="+key);
						debug("可以通过官方签名校验： https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=20_1   验证签名是否对应起来。如果签名没问题，那可能是微信商户的key，重新生成一个新的key使用就好了。");
					}
					debug("微信支付，创建订单失败, return_code = FAIL , response: "+response);
					return voTransform(order, BaseVO.failure("微信支付，创建订单失败："+return_msg));
				}
			}
			return voTransform(order, BaseVO.failure("创建订单失败，weixin response:"+response));
        } catch (Exception e) {
			e.printStackTrace();
			return voTransform(order, BaseVO.failure(e.getMessage()));
		}
	}
	

	
	/**
	 * 服务于创建订单返回的 vo，将 BaseVO  转化为jsapi、applet、app等vo
	 * @param order 创建订单时传入的 order
	 * @param vo {@link BaseVO}
	 * @return {@link JSAPIParamsVO}、 {@link AppletParamsVO}、 {@link AppParamsVO} 等
	 */
    private BaseVO voTransform(Order order, BaseVO vo){
    	//判断是否是服务商模式，如果是服务商模式，有可能会使用sub_openid
		String className = order.getClass().getName();
		if(className.equals("com.xnx3.weixin.weixinPay.request.serviceProvider.AppletOrder") || className.equals("com.xnx3.weixin.weixinPay.request.AppletOrder")){
			AppletParamsVO appletVO = new AppletParamsVO("", "");
			appletVO.setBaseVO(vo);
			return appletVO;
		}else if (className.equals("com.xnx3.weixin.weixinPay.request.serviceProvider.JSAPIOrder") || className.equals("com.xnx3.weixin.weixinPay.request.JSAPIOrder")) {
			JSAPIParamsVO jsapiVO = new JSAPIParamsVO("", "");
			jsapiVO.setBaseVO(vo);
			return jsapiVO;
		}else if (className.equals("com.xnx3.weixin.weixinPay.request.AppOrder")) {
			AppParamsVO appVO = new AppParamsVO();
			appVO.setBaseVO(vo);
			return appVO;
		}else{
			System.out.println("传入的 order : "+order);
			System.out.println("传入的 order className : "+className);
			System.out.println("请使用 com.xnx3.weixin.weixinPay.request 包下的 Order 类。比如你是 JSAPI 创建订单，那么你就传入 com.xnx3.weixin.weixinPay.request.JSAPIOrder 对象");
			return BaseVO.failure("您传入的是什么？order class:"+className);
		}
    }
    
    /**
     * 微信支付成功后，微信服务器异步请求我方服务器的通知
     * @param notityXml 微信服务器发送到我方服务器的xml数据信息
     * @return {@link BaseVO} 如果验证成功，也就是 result == SUCCESS ，那么info会返回  <xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml> 也就是吧info返回的响应，返回给微信服务器
     */
    public PayCallBackParamsVO payCallback(String notityXml){
    	PayCallBackParamsVO vo = new PayCallBackParamsVO();
    	
    	//解析成Map
    	Map<String,String> map = null;
        try {
			map = XmlUtil.stringToMap(notityXml);
		} catch (Exception e) {
			e.printStackTrace();
		}
        if(map == null){
        	vo.setBaseVO(BaseVO.FAILURE, "未接收到参数");
        	return vo;
        }
        
        //判断 支付是否成功
        if(map.get("result_code") != null && "SUCCESS".equals(map.get("result_code"))){
        	/*
        	 * 签名校验
        	 */
        	String signStr = map.get("sign");
        	map.remove("sign");
        	
//        	//调用逻辑传入参数按照字段名的 ASCII 码从小到大排序（字典序）
//            String stringA = formatUrlMap(map, false, false);
//            //第二步，在stringA最后拼接上key得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，再将得到的字符串所有字符转换为大写，得到sign值signValue。(签名)
//            String nSign = MD5Util.MD5(stringA+"&key="+shanghu_key).toUpperCase();
            String sign = SignUtil.generateSign(map, this.key);
            debug("sign:  "+sign+", param Sign:"+signStr);
        	if(sign.equalsIgnoreCase(signStr)){
        		debug("微信回调返回是否支付成功：是");
        		vo.setParams(map);
        		//通知微信服务器，已经成功处理，这个数据不要在请求了
        		vo.setInfo("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
            	return vo;
        	}else{
        		//签名对比出错，警报！可能有人利用接口攻击了
//        		debug("签名对比出错，警报！可能有人利用接口攻击了-->"+map.toString());
        		vo.setBaseVO(BaseVO.FAILURE, "签名对比出错");
        		return vo;
        	}
        }else{
        	vo.setBaseVO(BaseVO.FAILURE, notityXml);
        	return vo;
        }
    }
    
	/**
	 * 微信支付的回调
	 * @param request
	 * @param response
	 */
//	@RequestMapping(value="payCallback${url.suffix}", method = RequestMethod.POST)
//	@ResponseBody
//    public String payCallback(HttpServletRequest request,HttpServletResponse response) {
//		debug("微信回调接口方法 start");
//		debug("微信回调接口 操作逻辑 start");
//        String inputLine = "";
//        String notityXml = "";
//        try {
//            while((inputLine = request.getReader().readLine()) != null){
//                notityXml += inputLine;
//            }
//            //关闭流
//            request.getReader().close();
//            debug("微信回调内容信息："+notityXml);
//            //解析成Map
//            Map<String,String> map = doXMLParse(notityXml);
//            //判断 支付是否成功
//            if("SUCCESS".equals(map.get("result_code"))){
//            	
//            	/*
//            	 * 签名校验
//            	 */
//            	String sign = map.get("sign");
//            	map.remove("sign");
//            	
//            	
//            	//调用逻辑传入参数按照字段名的 ASCII 码从小到大排序（字典序）
//                String stringA = formatUrlMap(map, false, false);
//                //第二步，在stringA最后拼接上key得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，再将得到的字符串所有字符转换为大写，得到sign值signValue。(签名)
//                String nSign = MD5Util.MD5(stringA+"&key="+shanghu_key).toUpperCase();
//                debug("sign:  "+sign+", nSign:"+nSign);
//            	if(sign.equalsIgnoreCase(nSign)){
//            		debug("微信回调返回是否支付成功：是");
//                    //获得 返回的商户订单号
//                    String outTradeNo = map.get("out_trade_no");
//                    debug("微信回调返回商户订单号："+outTradeNo);
//                    debug("微信支付单号："+map.get("transaction_id"));
//                    
//                    //支付金额，单位为分
//                    String total_fee = map.get("total_fee");
//                    int totalFee = Lang.stringToInt(total_fee, 0);
//                    //将分转化为元
//                    totalFee = Math.round(totalFee/100);
//                    
//                    //通知微信服务器，已经成功处理
//                	return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
//            	}else{
//            		//签名对比出错，警报！可能有人利用接口攻击了
//            		debug("签名对比出错，警报！可能有人利用接口攻击了-->"+map.toString());
//            	}
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        return "failure";
//    }
	
    /**
     * 是否显示debug的打印信息
     */
    public static boolean debug = false;
	public static void debug(String text){
		if(debug){
			System.out.println(text);
		}
	}
}
