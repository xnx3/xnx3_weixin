package com.xnx3.weixin;

import java.util.HashMap;
import java.util.Map;

import com.xnx3.BaseVO;
import com.xnx3.net.HttpResponse;
import com.xnx3.net.HttpsUtil;
import net.sf.json.JSONObject;

/**
 * 模板消息
 * @author 管雷鸣
 * <p>官方网址</p>
 * <p>https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html#1</p>
 * <p>https://mp.weixin.qq.com/advanced/tmplmsg?action=faq&token=1663114&lang=zh_CN</p>
 *
 */
public class TemplateMessageUtil {
	static HttpsUtil https;
	static{
		https = new HttpsUtil();
	}
	
	public static void main(String[] args) {
		String token = "33_OcuF70QVeNIoozqpAaFPlhdRq42K_HGxa4i1L-eax7NOoUTErAgMGJeu0L9qT1iGlNCB5qsd-3_nlyANnDaQm7y2lH5eskaE8WW1v--sbURNoTDDQ2NGJduaBzVUwOQwijaLgmi6gZvd-dyNROJeAHADAV";
		String openid = "oa04fwGxDJsbIzzfwp4VPEBNGMd";
		String templateId = "BBN1yKFB3lZPqGAikXoOrBV80we32SIEVb_T0hmn4J";
		String url = "http://www.leimingyun.com";	//如果不想点击后跳转， url 可以设置为 null
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("first", "测试的");
		dataMap.put("tradeDateTime", "02月18日 01时05分");
		dataMap.put("orderType", "询价订单");
		dataMap.put("customerInfo", "管雷鸣");
		BaseVO vo = TemplateMessageUtil.sendMessage(token, openid, templateId, url, dataMap);
		System.out.println(vo);
	}
	

	/**
	 * 发送小程序的模板消息
	 * @param accessToken 通过 appId 跟 appSecret 得到的 access_token
	 * @param appletAppId 小程序的appid
	 * @param appletPagePath 跳转到小程序的页面路径，如 pages/index/index
	 * @param openid 接收的用户openid，如 oa04fwGxDJsbIzzfwp4VPEBNGM21
	 * @param templateId 模板id，如 3YV61HeNU6k59PfswwGs8zDCQxLUr2CaHre5GWyHu61
	 * @param url 点击消息跳转到的url，如果没有跳转，则传入 null
	 * @param dataMap 模板消息的信息。如 
	 * 	<pre>
	 * 		Map&lt;String, String&gt; dataMap = new HashMap&lt;String, String&gt;();
	 *		dataMap.put("first", "测试的");
	 * 		dataMap.put("tradeDateTime", "我是时间");
	 * 	</pre>
	 * @return {@link BaseVO} 如果 result() 为 BaseVO.SUCCESS ，那么是成功。 如果是 BaseVO.FAILURE ，可以用 getInfo() 获得失败原因 
	 */
	public static BaseVO sendAppletMessage(String accessToken, String appletAppId, String appletPagePath, String openid, String templateId, String url, Map<String, String> dataMap){
		JSONObject json = new JSONObject();
		json.put("touser", openid);	//护理美容
		json.put("template_id", templateId);
		if(url != null && url.length() > 0){
			json.put("url", url);
		}
		
		JSONObject appletJson = new JSONObject();
		appletJson.put("appid", appletAppId);
		appletJson.put("pagepath", appletPagePath);
		json.put("miniprogram", appletJson);
		
		JSONObject dataJson = new JSONObject();
		for(Map.Entry<String, String> entry : dataMap.entrySet()){
			JSONObject item = new JSONObject();
			item.put("value", entry.getValue());
			dataJson.put(entry.getKey(), item);
		}
		json.put("data", dataJson);
		
		return sendMessage(accessToken, json.toString());
	}
	
	/**
	 * 发送微信公众号服务号的模板消息
	 * @param accessToken 通过 appId 跟 appSecret 得到的 access_token
	 * @param openid 接收的用户openid，如 oa04fwGxDJsbIzzfwp4VPEBNGM21
	 * @param templateId 模板id，如 3YV61HeNU6k59PfswwGs8zDCQxLUr2CaHre5GWyHu61
	 * @param url 点击消息跳转到的url，如果没有跳转，则传入 null
	 * @param dataMap 模板消息的信息。如 
	 * 	<pre>
	 * 		Map&lt;String, String&gt; dataMap = new HashMap&lt;String, String&gt;();
	 *		dataMap.put("first", "测试的");
	 * 		dataMap.put("tradeDateTime", "我是时间");
	 * 	</pre>
	 * @return {@link BaseVO} 如果 result() 为 BaseVO.SUCCESS ，那么是成功。 如果是 BaseVO.FAILURE ，可以用 getInfo() 获得失败原因 
	 */
	public static BaseVO sendMessage(String accessToken, String openid, String templateId, String url, Map<String, String> dataMap){
		JSONObject json = new JSONObject();
		json.put("touser", openid);	//护理美容
		json.put("template_id", templateId);
		if(url != null && url.length() > 0){
			json.put("url", url);
		}
		
		JSONObject dataJson = new JSONObject();
		for(Map.Entry<String, String> entry : dataMap.entrySet()){
			JSONObject item = new JSONObject();
			item.put("value", entry.getValue());
			dataJson.put(entry.getKey(), item);
		}
		json.put("data", dataJson);
		
		return sendMessage(accessToken, json.toString());
	}
	
	/**
	 * 向关注者发送一条小程序模版消息
	 * @param accessToken 通过 appId 跟 appSecret 得到的 access_token
	 * @param jsonString 发送的消息的内容，json格式字符串。 传入如   {"touser":"OPENID","template_id":"ngqIpbwh8bUfcSsECmogfXcV14J0tQlEpBO27izEYtY","url":"http://www.leimingyun.com","topcolor":"#FF0000","data":{"User":{"value":"黄先生"},"Date":{"value":"06月07日 19时24分"},"CardNumber":{"value":"0426"},"Type":{"value":"消费"},"Money":{"value":"人民币260.00元"},"DeadTime":{"value":"06月07日19时24分"},"Left":{"value":"6504.09"}}}
	 * @return {@link BaseVO} 如果 result() 为 BaseVO.SUCCESS ，那么是成功。 如果是 BaseVO.FAILURE ，可以用 getInfo() 获得失败原因
	 */
	public static BaseVO sendMessage(String accessToken, String jsonString){
		HttpResponse hr;
		try {
			hr = https.send("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+accessToken, jsonString, new HashMap<String, String>());
			if(hr.getCode() - 200 == 0){
				//响应200，拿到数据了，将数据格式化
				JSONObject resultJson = JSONObject.fromObject(hr.getContent());
				Object errcode = resultJson.get("errcode");
				if(errcode != null && errcode.toString().equals("0")){
					Object msgid = resultJson.get("msgid");
					System.out.println(msgid);
					if(msgid != null){
						//成功
						return BaseVO.success(msgid.toString());
					}
				}
				
				return BaseVO.failure(hr.getContent());
			}else{
				return BaseVO.failure("weixin server response http code:"+hr.getCode()+"，"+hr.getContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return BaseVO.failure(e.getMessage());
		}
	}
}
