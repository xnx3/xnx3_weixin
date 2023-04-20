package com.xnx3.weixin;

import net.sf.json.JSONObject;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.xnx3.BaseVO;
import com.xnx3.DateUtil;
import com.xnx3.media.ImageUtil;
//import com.xnx3.net.HttpResponse;
//import com.xnx3.net.HttpsUtil;
import com.xnx3.weixin.bean.AccessToken;
import com.xnx3.weixin.vo.Jscode2sessionResultVO;
import com.xnx3.weixin.vo.PhoneVO;

import cn.zvo.http.Http;
import cn.zvo.http.Response;

/**
 * 微信小程序
 * @author 管雷鸣
 */
public class WeiXinAppletUtil implements java.io.Serializable{
	static Http http;
	
	//获取普通access_token的url
	public static String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";	
	//access_token获取后使用的时长，单位为秒，官方给出的access_token获取后最大有效时间是7200秒，一个access_token的有效期最大只能是7200秒之内有效，超出后就要重新获取。这里设定获取到access_token后最大持续5000秒，超过后便再次获取新的access_token
	public static int ACCESS_TOKEN_DELAY_TIME = 5000;	
	
	private String appId; 	//appId 小程序的 app_id
	private String appSecret;	//appSecret 小程序的 app_secret
	
	private AccessToken accessToken;	//持久化access_token数据
	
	public boolean debug = true;	//调试日志是否打印
	
	static{
		http = new Http();
	}
	
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
		
//		HttpsUtil https = new HttpsUtil();
		Http http = new Http();
		Response res;
		try {
			res = http.get("https://api.weixin.qq.com/sns/jscode2session?appid="+appId+"&secret="+appSecret+"&js_code="+code+"&grant_type=authorization_code");
			if(res.getCode() - 200 == 0 && res.getContent() != null && res.getContent().indexOf("session_key") > -1){
				JSONObject json = JSONObject.fromObject(res.getContent());
				vo.setOpenid(json.get("openid") == null? "":json.getString("openid"));
				vo.setUnionid(json.get("unionid") == null ? "":json.getString("unionid"));
				vo.setSessionKey(json.get("session_key") == null? "":json.getString("session_key"));
			}else{
				vo.setBaseVO(BaseVO.FAILURE, res.getContent());
			}
		} catch (IOException e) {
			e.printStackTrace();
			vo.setBaseVO(BaseVO.FAILURE, e.getMessage());
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
	 * 获取用户手机号
	 * @param sessionKey 用户通过小程序code登录成功后获得的sessionKey
	 * @param requestPayloadString request payload 请求发过来的JSON格式字符串
	 * @return 手机号。如果 {@link PhoneVO}.getResult 为 PhoneVO.SUCCESS ，那么便是获取成功，可以通过 {@link PhoneVO#getPhone()}获取手机号
	 */
	public PhoneVO getPhone(String sessionKey, String requestPayloadString){
		PhoneVO vo = new PhoneVO();
		if(requestPayloadString == null || requestPayloadString.length() == 0){
			vo.setBaseVO(PhoneVO.FAILURE, "requestPayloadString 无数据");
			return vo;
		}
		
		JSONObject json = JSONObject.fromObject(requestPayloadString);
		if(json.get("encryptedData") != null){
			return getPhone(sessionKey, json.getString("encryptedData"), json.getString("iv"));
		}else{
			vo.setBaseVO(PhoneVO.FAILURE, "encryptedData 不存在");
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
//		com.xnx3.net.HttpUtil httpUtil = new com.xnx3.net.HttpUtil();
		Http http = new Http();
		Response res;
		try {
			res = http.get(ACCESS_TOKEN_URL.replace("APPID", this.appId).replace("APPSECRET", this.appSecret));
			JSONObject json = JSONObject.fromObject(res.getContent());
			if(json.get("errcode") == null){
				//没有出错，获取access_token成功
				accessToken.setAccess_token(json.getString("access_token"));
				accessToken.setExpires_in(json.getInt("expires_in"));
				accessToken.setGainTime(DateUtil.timeForUnix10());
				return true;
			}else{
				debug("获取access_token失败！返回值："+res.getContent());
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			debug("获取access_token失败！异常："+e.getMessage());
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
	
	
	/**
	 * 向小程序使用者发送一条小程序模版的订阅消息
	 * <p>必须小程序端弹出过是否允许订阅xxx消息，用户点击允许，才能发给用户</p>
	 * <p>要是必须小程序端弹出的是否允许订阅xxx消息，只是允许一次，那只能成功给这个用户发送一次，第二次发送将报错。</p>
	 * @param template_id 模板消息id，传入如 Mg3rJhtFtV4cd2PKDCwSBNOVKbXsX1Mhdh4mBrMQ111
	 * @param openid 接受用户的微信 openid 
	 * @param dataMap 模板消息的信息。如 
	 * 	<pre>
	 * 		Map&lt;String, String&gt; dataMap = new HashMap&lt;String, String&gt;();
	 *		dataMap.put("first", "测试的");
	 * 		dataMap.put("tradeDateTime", "我是时间");
	 * 	</pre>
	 * 其中key便是 {{first.DATA}} 中的 first
	 * @return 
	 */
	public BaseVO sendSubscribeTemplateMessage(String template_id, String openid, Map<String, String> dataMap){
		JSONObject json = new JSONObject();
		json.put("touser", openid);
		json.put("template_id", template_id);
		
		JSONObject dataJson = new JSONObject();
		for(Map.Entry<String, String> entry : dataMap.entrySet()){
			JSONObject item = new JSONObject();
			dataJson.put(entry.getKey(), getMessageValue(entry.getValue()));
		}
		json.put("data", dataJson);
		
		Response res;
		try {
			res = http.post("https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token="+getAccessToken().getAccess_token(), json.toString(), new HashMap<String, String>());
			
//			res = http.send("https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token="+getAccessToken().getAccess_token(), json.toString(), new HashMap<String, String>(), new HashMap<String, String>());
			if(res.getCode() - 200 == 0){
				//响应200，拿到数据了，将数据格式化
				JSONObject resultJson = JSONObject.fromObject(res.getContent());
				Object errcode = resultJson.get("errcode");
				if(errcode != null && errcode.toString().equals("0")){
					Object msgid = resultJson.get("msgid");
					if(msgid != null){
						//成功
						return BaseVO.success(msgid.toString());
					}
				}
				
				return BaseVO.failure(res.getContent());
			}else{
				return BaseVO.failure("weixin server response http code:"+res.getCode()+"，"+res.getContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return BaseVO.failure(e.getMessage());
		}
		
	}
	private static JSONObject getMessageValue(String value) {
		JSONObject json = new JSONObject();
		json.put("value", value);
		return json;
	}
	

	/**
	 * 生成微信小程序的二维码，通过该接口生成的小程序码，永久有效
	 * <p>获取小程序码，适用于需要的码数量较少的业务场景。通过该接口生成的小程序码，永久有效，有数量限制</p>
	 * <p>使用示例：</p>
	 * <pre>
	 * WeiXinAppletUtil util = new WeiXinAppletUtil("wx451fb5710f14exxx", "a4537821cb3828fb9ae98c6dbxxxxxx");
	 * BufferedImage bufferImage = util.getWXCode("path/index/index?id=1"); //生成二维码
	 * ImageUtil.saveToLocalhost(bufferImage, "jpg", "/images/12.jpg"); //保存到本地
	 * </pre>
	 * <p>注意，与 wxacode.createQRCode 总共生成的码数量限制为 100,000，请谨慎调用</p>
	 * @param path 生成二维码的路径，传入如 path/index/index ,也可以带参数，如 path/index/index?id=1
	 * @return 生成的二维码的 {@link BufferedImage} jpg格式的二维码，比如可以使用 ImageUtil.saveToLocalhost(bufferImage, "jpg", "/images/12.jpg"); 将其保存到磁盘。
	 * 	<p>如果返回 null ，则是生成二维码失败</p>
	 */
	public BufferedImage getWXCode(String path) {
		JSONObject json = new JSONObject();
		json.put("path", path);
		
		try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.weixin.qq.com/wxa/getwxacode?access_token="+getAccessToken().getAccess_token()).openConnection();
            // 设置连接超时时间
            conn.setConnectTimeout(30000);
            // 设置读取超时时间
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Origin", "https://sirius.searates.com");// 主要参数
            conn.setRequestProperty("Referer", "https://sirius.searates.com/cn/port?A=ChIJP1j2OhRahjURNsllbOuKc3Y&D=567&G=16959&shipment=1&container=20st&weight=1&product=0&request=&weightcargo=1&");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");// 主要参数
            // 需要输出
            conn.setDoInput(true);
            // 需要输入
            conn.setDoOutput(true);
            // 设置是否使用缓存
            conn.setUseCaches(false);
            // 设置请求属性
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");	//application/x-www-form-urlencoded
            conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            
            DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(json.toString());
            dos.flush();
            dos.close();
            
            // 输出返回结果
            InputStream input = conn.getInputStream();
            return ImageUtil.inputStreamToBufferedImage(input);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
}
