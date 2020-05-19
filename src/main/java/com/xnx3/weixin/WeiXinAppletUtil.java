package com.xnx3.weixin;

import net.sf.json.JSONObject;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.xnx3.BaseVO;
import com.xnx3.DateUtil;
import com.xnx3.net.HttpResponse;
import com.xnx3.net.HttpsUtil;
import com.xnx3.weixin.bean.AccessToken;
import com.xnx3.weixin.vo.Jscode2sessionResultVO;
import com.xnx3.weixin.vo.PhoneVO;

/**
 * 微信小程序
 * @author 管雷鸣
 */
public class WeiXinAppletUtil implements java.io.Serializable{
	//获取普通access_token的url
	public static String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";	
	//access_token获取后使用的时长，单位为秒，官方给出的access_token获取后最大有效时间是7200秒，一个access_token的有效期最大只能是7200秒之内有效，超出后就要重新获取。这里设定获取到access_token后最大持续5000秒，超过后便再次获取新的access_token
	public static int ACCESS_TOKEN_DELAY_TIME = 5000;	
	
	private String appId; 	//appId 小程序的 app_id
	private String appSecret;	//appSecret 小程序的 app_secret
	
	private AccessToken accessToken;	//持久化access_token数据
	
	public boolean debug = true;	//调试日志是否打印
	
	/**
	 * 初始化
	 * @param appId 小程序的 app_id
	 * @param appSecret 小程序的 app_secret
	 */
	public WeiXinAppletUtil(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}
	
	/**
	 * https://api.weixin.qq.com/sns/jscode2session 根据code ，获取 openid、session_key 、 unionid
	 * @param code 微信小程序端，通过js获取到的code，也就是 wx.login 获取到的 code 
	 * @return {@link Jscode2sessionResultVO} 微信jscode2session接口所返回的结果。
	 * 	<ul>
	 * 		<li>result : {@link Jscode2sessionResultVO}.FAILURE 调用失败，通过 .getInfo() 返回失败结果</li>
	 * 		<li>result : {@link Jscode2sessionResultVO}.SUCCESS 调用成功。即可正常获得 openid、session_key 等</li>
	 * 	</ul>
	 * @deprecated 请使用 {@link #loginByCode(String)}
	 */
	public Jscode2sessionResultVO jscode2session(String code){
		Jscode2sessionResultVO vo = new Jscode2sessionResultVO();
		
		HttpsUtil https = new HttpsUtil();
		HttpResponse hr = https.get("https://api.weixin.qq.com/sns/jscode2session?appid="+appId+"&secret="+appSecret+"&js_code="+code+"&grant_type=authorization_code");
		if(hr.getCode() - 200 == 0 && hr.getContent() != null && hr.getContent().indexOf("session_key") > -1){
			JSONObject json = JSONObject.fromObject(hr.getContent());
			vo.setOpenid(json.get("openid") == null? "":json.getString("openid"));
			vo.setUnionid(json.get("unionid") == null ? "":json.getString("unionid"));
			vo.setSessionKey(json.get("session_key") == null? "":json.getString("session_key"));
		}else{
			vo.setBaseVO(BaseVO.FAILURE, hr.getContent());
		}
		return vo;
	}
	
	/**
	 * https://api.weixin.qq.com/sns/jscode2session 根据code ，获取 openid、session_key 、 unionid
	 * <br/>这个跟 {@link #jscode2session(String)} 一样，只不过这个loginByCode 更接近其意思
	 * @param code 微信小程序端，通过js获取到的code，也就是 wx.login 获取到的 code 
	 * @return {@link Jscode2sessionResultVO} 微信jscode2session接口所返回的结果。
	 * 	<ul>
	 * 		<li>result : {@link Jscode2sessionResultVO}.FAILURE 调用失败，通过 .getInfo() 返回失败结果</li>
	 * 		<li>result : {@link Jscode2sessionResultVO}.SUCCESS 调用成功。即可正常获得 openid、session_key 等</li>
	 * 	</ul>
	 */
	public Jscode2sessionResultVO loginByCode(String code){
		return jscode2session(code);
	}
	
	/**
	 * 获取用户手机号
	 * @param sessionKey 用户通过小程序code登录成功后获得的sessionKey
	 * @param encryptedData 小程序通过 open-type="getPhoneNumber" 按钮获取的手机号加密信息
	 * @param iv 小程序通过 open-type="getPhoneNumber" 按钮获取的
	 * @return 手机号。如果 {@link PhoneVO}.getResult 为 PhoneVO.SUCCESS ，那么便是获取成功，可以通过 {@link PhoneVO#getPhone()}获取手机号
	 */
	public PhoneVO getPhone(String sessionKey, String encryptedData, String iv){
		PhoneVO vo = new PhoneVO();
		
    	//被加密的数据
		byte[] dataByte = Base64.getDecoder().decode(encryptedData);
    	//加密秘钥
    	byte[] keyByte = Base64.getDecoder().decode(sessionKey);
    	// 偏移量
    	byte[] ivByte = Base64.getDecoder().decode(iv);
    	try {
    		//如果密钥不足16位，那么就补足.  这个if 中的内容很重要
    		int base = 16;
    		if (keyByte.length % base != 0) {
				int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
				byte[] temp = new byte[groups * base];
				Arrays.fill(temp, (byte) 0);
				System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
				keyByte = temp;
    		}
			// 初始化
			if (Security.getProvider(org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME) == null){
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			}

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
			AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
			parameters.init(new IvParameterSpec(ivByte));
			cipher.init(Cipher.DECRYPT_MODE, spec, parameters);// 初始化
			byte[] resultByte = cipher.doFinal(dataByte);
			if (null != resultByte && resultByte.length > 0) {
				String result = new String(resultByte, "UTF-8");
				JSONObject json = JSONObject.fromObject(result); 
				if(json.get("phoneNumber") != null){
					vo.setPhone(json.getString("phoneNumber"));
				}
				if(json.get("countryCode") != null){
					vo.setCountryCode(json.getString("countryCode"));
				}
				vo.setResult(PhoneVO.SUCCESS);
				return vo;
			}else{
				vo.setBaseVO(PhoneVO.FAILURE, "weixin server response is null");
	    		return vo;
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    		vo.setBaseVO(PhoneVO.FAILURE, e.getMessage());
    		return vo;
    	}
    }
		    
	
	/**
	 * 获取最新的普通access_token
	 * @return AccessToken 若返回null，则获取access_token失败
	 */
	public AccessToken getAccessToken(){
		boolean refreshToken = false;	//需重新刷新获取token，默认是不需要
		
		if(accessToken == null){
			accessToken = new AccessToken();
			refreshToken = true;
		}
		
		//是否过时，需要重新获取token
		if(DateUtil.timeForUnix10()>accessToken.getGainTime()+ACCESS_TOKEN_DELAY_TIME){
			refreshToken = true;
		}
		
		//避免一次可能网络中断，连续获取三次，减小误差
		boolean success = !refreshToken;
		int i = 0;
		for (; i < 3 && !success ; i++) {
			success = refreshAccessToken();
		}
		
		if(!success){
			debug("连续获取"+i+"次access_token，均失败！" );
			return null;
		}else{
			return accessToken;
		}
	}

	/**
	 * 刷新重新获取access_token
	 * @return 获取成功|失败
	 */
	private boolean refreshAccessToken(){
		com.xnx3.net.HttpUtil httpUtil = new com.xnx3.net.HttpUtil();
		HttpResponse httpResponse = httpUtil.get(ACCESS_TOKEN_URL.replace("APPID", this.appId).replace("APPSECRET", this.appSecret));
		JSONObject json = JSONObject.fromObject(httpResponse.getContent());
		if(json.get("errcode") == null){
			//没有出错，获取access_token成功
			accessToken.setAccess_token(json.getString("access_token"));
			accessToken.setExpires_in(json.getInt("expires_in"));
			accessToken.setGainTime(DateUtil.timeForUnix10());
			return true;
		}else{
			debug("获取access_token失败！返回值："+httpResponse.getContent());
			return false;
		}
	}
	/**
	 * 调试日志打印
	 * @param message 日志内容
	 */
	private void debug(String message){
		if(debug){
			System.out.println("WeiXinUtil:"+message);
		}
	}
	
	
//	/**
//	 * 向关注者发送一条小程序模版消息
//	 * @param openid 接受用户的微信 openid
//	 * @param content 向用户发送的消息的内容
//	 */
//	public void sendTextMessage(String openid, String content){
//		JSONObject json = new JSONObject();
//		json.put("touser", "oMXL_4_0q-cuteDgVLPLe8mzT7rE");
//		json.put("msgtype", "text");
//		
//		JSONObject mqTemplateMsg = new JSONObject();
//		mqTemplateMsg.put("appid", this.appId);
//		mqTemplateMsg.put("template_id", this.appId);
//		
//		JSONObject textJson = new JSONObject();
//		textJson.put("content", content);
//		json.put("text", textJson);
//		System.out.println(json);
//		HttpUtil http = new HttpUtil();
//		JSONObject resultJson = http.doPost("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="+com.xnx3.wangmarket.weixin.Global.getWeiXinXiaoChengXuUtil().getAccessToken().getAccess_token(), json);
//		System.out.println("result: "+resultJson);
//	}
}
