package com.xnx3.weixin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.xnx3.BaseVO;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import com.xnx3.Lang;
import com.xnx3.StringUtil;
import com.xnx3.weixin.weixinPay.PayCallBackParamsVO;
import com.xnx3.weixin.weixinPay.request.AppletOrder;
import com.xnx3.weixin.weixinPay.request.JSAPIOrder;
import com.xnx3.weixin.weixinPay.request.Order;
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
	
	private String appid;	//公众号、小程序等appid
	private String mch_id;	//商户号
	private String key;		//商户key,在微信商户平台-帐户设置-安全设置-API安全-API密钥-设置API密钥这个里面设置的KEY
	private String sub_mch_id = null;	//子商户号。如果这里设置了，那么上面的 mch_id 便是服务商的商户号
	
	/**
	 * 创建微信支付工具
	 * @param appid 小程序appid、微信公众号appid 等
	 * @param mch_id 商户号，传入如 1591496141
	 * @param key 商户key，在微信商户平台-帐户设置-安全设置-API安全-API密钥-设置API密钥这个里面设置的KEY
	 */
	public WeiXinPayUtil(String appid, String mch_id, String key) {
		this.appid = appid;
		this.mch_id = mch_id;
		this.key = key;
	}
	
	/**
	 * 开启服务商模式
	 * <br/>也就是设置子商户号。如果这里设置了，那么new创建这个类的时候，设置的 mch_id 便是服务商的商户号
	 * @param sub_mch_id 子商户号，传入如 1591496140 
	 */
	public void openServiceProviderMode(String sub_mch_id) {
		this.sub_mch_id = sub_mch_id;
	}

	/**
	 * 创建订单，在微信支付那边创建对应的订单
	 * @param bean {@link PayOrderBean} 必须赋值里面所有的参数
	 * @param createOrderInterface 订单创建成功后，要执行的操作
	 */
	public BaseVO createOrder(Order order){
		debug("微信 统一下单 接口调用");
    
        //创建hashmap(用户获得签名)
        SortedMap<String, String> paraMap = new TreeMap<String, String>();
        //设置随机字符串
        String nonceStr = Lang.uuid();
        //设置商户订单号
//	    String outTradeNo = StringUtil.getRandom09AZ(2)+StringUtil.intTo36(DateUtil.timeForUnix10())+StringUtil.getRandom09AZ(2);
        
        //设置请求参数(公众号、小程序ID)
        paraMap.put("appid", this.appid);
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
            paraMap.put("openid", order.getOpenid());
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
        paramBuffer.append("<openid>"+order.getOpenid()+"</openid>");
        paramBuffer.append("</xml>");
        
        debug("paramBuffer:"+paramBuffer.toString());
        //发送请求(POST)(获得数据包ID)(这有个注意的地方 如果不转码成ISO8859-1则会告诉你body不是UTF8编码 就算你改成UTF8编码也一样不好使 所以修改成ISO8859-1)
        try {
        	String response = HttpsUtil.post(UNIFIED_ORDER, new String(paramBuffer.toString().getBytes(), "ISO8859-1"));
			Map<String,String> map = doXMLParse(response);
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
							return new JSAPIParamsVO(this.appid, map.get("prepay_id")).generateSign(this.key);
				        }else if(order.getType().equals(AppletOrder.TYPE)){
				        	//小程序
				        	return new AppletParamsVO(this.appid, map.get("prepay_id")).generateSign(this.key);
				        }else{
				        	//其他。。。
				        	return BaseVO.failure("目前只有JSAPI、小程序支付，其他的还没加，联系微信 xnx3com 让他来增加吧");
				        }
					}else{
						return BaseVO.failure("微信支付，业务结果失败："+response);
					}
				}else if (return_code.equals("FAIL")) {
					return BaseVO.failure("微信支付，创建订单失败："+response);
				}
			}
			return BaseVO.failure("创建订单失败，weixin response:"+response);
        } catch (Exception e) {
			e.printStackTrace();
			return BaseVO.failure(e.getMessage());
		}
	}

    
    /**
     * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
     * @param strxml
     * @return
     */
    private Map<String,String> doXMLParse(String strxml) throws Exception {
        if(null == strxml || "".equals(strxml)) {
            return null;
        }
        
        Map<String,String> map = new HashMap<String,String>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document d = builder.parse(StringUtil.stringToInputStream(strxml, "UTF-8"));
			if(d.hasChildNodes()){
				Node node = d.getFirstChild();	//获取XML节点
				NodeList nodelist = node.getChildNodes();
				for (int i = 0; i < nodelist.getLength(); i++) {
					Node item = nodelist.item(i);
					map.put(item.getNodeName(), item.getTextContent());
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return map;
    }
    
    /**
     * 微信支付成功后，微信服务器异步请求我方服务器的通知
     * @param notityXml 微信服务器发送到我方服务器的xml数据信息
     * @return {@link BaseVO}
     */
    public PayCallBackParamsVO payCallback(String notityXml){
    	PayCallBackParamsVO vo = new PayCallBackParamsVO();
    	
    	//解析成Map
    	Map<String,String> map = null;
        try {
			map = doXMLParse(notityXml);
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
	
	public static void debug(String text){
		System.out.println(text);
	}
}
