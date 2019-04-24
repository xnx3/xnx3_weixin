package com.xnx3.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import net.sf.json.JSONObject;
import com.xnx3.DateUtil;
import com.xnx3.Lang;
import com.xnx3.StringUtil;
import com.xnx3.net.HttpResponse;
import com.xnx3.net.HttpUtil;
import com.xnx3.weixin.bean.AccessToken;
import com.xnx3.weixin.bean.JsapiTicket;
import com.xnx3.weixin.bean.MessageReceive;
import com.xnx3.weixin.bean.MessageReply;
import com.xnx3.weixin.bean.SignatureBean;
import com.xnx3.weixin.bean.UserInfo;

/**
 * 微信基本操作-不涉及小程序。微信小程序使用 {@link XiaoChengXuUtil}
 * @author 管雷鸣
 */
public class WeiXinUtil {
	public static String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";	//获取普通access_token的url
	public static String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";	//获取用户个人信息的url
	public static String OAUTH2_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";	//网页授权跳转的url
	public static String OAUTH2_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";	//网页授权，获取access_token
	public static String OAUTH2_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";	//网页授权，获取用户信息
	public static String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";	//获取jsapi_ticket的接口。jsapi_ticket是公众号用于调用微信JS接口的临时票据，效果同access_token，也是7200秒刷新一次
	
	public static int ACCESS_TOKEN_DELAY_TIME = 5000;	//access_token获取后使用的时长，单位为秒，官方给出的access_token获取后最大有效时间是7200秒，一个access_token的有效期最大只能是7200秒之内有效，超出后就要重新获取。这里设定获取到access_token后最大持续5000秒，超过后便再次获取新的access_token
	public static int JSAPI_TICKET_DELAY_TIME = 5000;	//jsapi_ticket获取后使用的时长，单位为秒
	
	
	public boolean debug = true;	//调试日志是否打印
	private AccessToken accessToken;	//持久化access_token数据
	private JsapiTicket jsapiTicket;	//持久化的 JsapiTicket 数据，用于JS SDK
	private String appId;	//AppID(应用ID)
	private String appSecret;	//AppSecret(应用密钥)
	private String token;	//用户于微信公众平台双方拟定的令牌Token
	
	/**
	 * 微信基本操作-不涉及小程序。微信小程序使用 {@link XiaoChengXuUtil}
	 * @param appId AppID(应用ID)
	 * @param appSecret AppSecret(应用密钥)
	 * @param token 用户于微信公众平台双方拟定的令牌Token。可为空。
	 */
	public WeiXinUtil(String appId, String appSecret, String token) {
		this.appId = appId;
		this.appSecret = appSecret;
		this.token = token;
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
	 * 通过openId，获取用户的信息
	 * @param openId 普通用户的标识，对当前公众号唯一
	 * @return UserInfo	
	 * 			<ul>		
	 * 				<li>若返回null，则获取失败</li>
	 * 				<li>若不为null，先判断其subscribe，若为true，已关注，则可以取到其他的信息</li>
	 * 			</ul>
	 */
	public UserInfo getUserInfo(String openId){
		HttpUtil httpUtil = new HttpUtil();
		UserInfo userInfo = null;
		HttpResponse httpResponse = httpUtil.get(USER_INFO_URL.replace("ACCESS_TOKEN", getAccessToken().getAccess_token()).replace("OPENID", openId));
		JSONObject json = JSONObject.fromObject(httpResponse.getContent());
		if(json.get("subscribe") != null){
			userInfo = new UserInfo();
			userInfo.setSubscribe(json.getString("subscribe").equals("1"));
			if(userInfo.isSubscribe()){
				userInfo.setCity(json.getString("city"));
				userInfo.setCountry(json.getString("country"));
				userInfo.setHeadImgUrl(json.getString("headimgurl"));
				userInfo.setLanguage(json.getString("language"));
				userInfo.setNickname(json.getString("nickname"));
				userInfo.setOpenid(json.getString("openid"));
				userInfo.setProvince(json.getString("province"));
				userInfo.setSex(json.getInt("sex"));
				userInfo.setSubscribeTime(json.getInt("subscribe_time"));
				userInfo.setUnionid(json.getString("unionid"));
				userInfo.setRemark(json.getString("remark"));
				userInfo.setGroupid(json.getInt("groupid"));
				userInfo.setSubscribeScene(json.getString("subscribe_scene"));
				userInfo.setQr_scene(json.getString("qr_scene"));
				userInfo.setQrSceneStr(json.getString("qr_scene_str"));
			}
		}else{
			debug("获取用户信息失败！用户openid:"+openId+"，微信回执："+httpResponse.getContent());
		}
		
		return userInfo;
	}
	
	/**
	 * 刷新重新获取access_token
	 * @return 获取成功|失败
	 */
	private boolean refreshAccessToken(){
		HttpUtil httpUtil = new HttpUtil();
		HttpResponse httpResponse = httpUtil.get(ACCESS_TOKEN_URL.replace("APPID", this.appId).replace("APPSECRET", this.appSecret));
		JSONObject json = JSONObject.fromObject(httpResponse.getContent());
		if(json.get("errcode") == null){
			//没有出错，获取access_token成功
			accessToken.setAccess_token(json.getString("access_token"));
			accessToken.setExpires_in(json.getInt("expires_in"));
			return true;
		}else{
			debug("获取access_token失败！返回值："+httpResponse.getContent());
			return false;
		}
	}
	
	/**
	 * 获取网页授权的URL跳转地址
	 * @param redirectUri 授权后重定向的回调链接地址，无需URL转码，原始url
	 * @param scope 应用授权作用域，snsapi_base （不弹出授权页面，直接跳转，只能获取用户openid），snsapi_userinfo （弹出授权页面，可通过openid拿到昵称、性别、所在地。并且，即使在未关注的情况下，只要用户授权，也能获取其信息）
	 * @param state 重定向后会带上state参数，开发者可以填写a-zA-Z0-9的参数值，最多128字节
	 * @return url地址
	 */
	public String getOauth2Url(String redirectUri,String scope,String state){
		return OAUTH2_URL.replace("APPID", this.appId).replace("REDIRECT_URI", StringUtil.stringToUrl(redirectUri)).replace("SCOPE", scope).replace("STATE", state);
	}
	
	/**
	 * 获取网页授权的URL跳转地址，弹出授权页面，可通过openid拿到昵称、性别、所在地。并且，即使在未关注的情况下，只要用户授权，也能获取其信息
	 * @param redirectUri 授权后重定向的回调链接地址，无需URL转码，原始url
	 * @return url地址
	 */
	public String getOauth2SimpleUrl(String redirectUri){
		return getOauth2Url(redirectUri, "snsapi_userinfo", "STATE");
	}
	
	/**
	 * 获取网页授权的URL跳转地址，不会出现授权页面，只能拿到用户openid
	 * @param redirectUri 授权后重定向的回调链接地址，无需URL转码，原始url
	 * @return url地址
	 */
	public String getOauth2ExpertUrl(String redirectUri){
		return getOauth2Url(redirectUri, "snsapi_base", "STATE");
	}
	
	/**
	 * 获取网页授权，获取用户的openid
	 * @param code 如果用户同意授权，页面将跳转至 redirect_uri&#47;&#63;code=CODE&#38;state=STATE，授权成功会get方式传过来
	 * @return 用户openid 若为null，则获取失败
	 */
	public String getOauth2OpenId(String code){
		HttpUtil httpUtil = new HttpUtil();
		HttpResponse httpResponse = httpUtil.get(OAUTH2_ACCESS_TOKEN_URL.replace("APPID", this.appId).replace("SECRET", this.appSecret).replace("CODE", code));
		JSONObject json = JSONObject.fromObject(httpResponse.getContent());
		if(json.get("errcode") == null){
			//没有出错，获取网页access_token成功
			return json.getString("openid");
		}else{
			debug("获取网页授权access_token失败！返回值："+httpResponse.getContent());
		}
		
		return null;
	}
	
	/**
	 * 网页授权获取用户的个人信息
	 * @param code 如果用户同意授权，页面将跳转至 redirect_uri&#47;&#63;code=CODE&#38;state=STATE，授权成功会get方式传过来
	 * @return	结果：
	 * 		<ul>	
	 * 			<li>若成功，返回{@link UserInfo} (无 subscribeTime 项)</li>
	 * 			<li>若失败，返回null</li>
	 * 		</ul>
	 */
	public UserInfo getOauth2UserInfo(String code){
		HttpUtil httpUtil = new HttpUtil();
		HttpResponse httpResponse = httpUtil.get(OAUTH2_ACCESS_TOKEN_URL.replace("APPID", this.appId).replace("SECRET", this.appSecret).replace("CODE", code));
		JSONObject json = JSONObject.fromObject(httpResponse.getContent());
		if(json.get("errcode") == null){
			//没有出错，获取网页access_token成功
			HttpResponse res = httpUtil.get(OAUTH2_USER_INFO_URL.replace("ACCESS_TOKEN", json.getString("access_token")).replace("OPENID", json.getString("openid")));
			JSONObject j = JSONObject.fromObject(res.getContent());
			if(j.get("errcode") == null){
				UserInfo userInfo = new UserInfo();
				userInfo.setCity(j.getString("city"));
				userInfo.setOpenid(j.getString("openid"));
				userInfo.setNickname(j.getString("nickname"));
				userInfo.setSex(j.getInt("sex"));
				userInfo.setProvince(j.getString("province"));
				userInfo.setCountry(j.getString("country"));
				userInfo.setHeadImgUrl(j.getString("headimgurl"));
				userInfo.setLanguage("zh_CN");
				return userInfo;
			}else{
				debug("获取网页授权用户信息失败！返回值："+res.getContent());
			}
		}else{
			debug("获取网页授权access_token失败！返回值："+httpResponse.getContent());
		}
		
		return null;
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
	 * 接收xml格式消息，用户通过微信公众号发送消息，有服务器接收。这里将微信服务器推送来的消息进行格式化为 {@link MessageReceive}对象
	 * <p>通常此会存在于一个Servlet中，用于接收微信服务器推送来的消息。</p>
	 * @param request 这里便是微信服务器接收到消息后，将消息POST提交过来的请求，会自动从request中取微信post的消息内容
	 * @return	返回 {@link MessageReceive}
	 * @throws DocumentException 异常
	 */
	public MessageReceive receiveMessage(HttpServletRequest request) throws DocumentException{
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String messageContent = jb.toString();
		
		return receiveMessage(messageContent);
	}
	
	
	/**
	 * 接收xml格式消息，用户通过微信公众号发送消息，有服务器接收。这里将微信服务器推送来的消息进行格式化为 {@link MessageReceive}对象
	 * <p>通常此会存在于一个Servlet中，用于接收微信服务器推送来的消息。例如SpringMVC中可以这样写：</p>
	 * <pre>
	 * 	MessageReceive message = new WeiXinUtil(......).receiveMessage(request);
	 * </pre>
	 * @param messageContent 这里便是微信服务器接收到消息后，将消息POST提交过来消息内容
	 * @return	返回 {@link MessageReceive}
	 * @throws DocumentException 异常
	 */
	public MessageReceive receiveMessage(String messageContent) throws DocumentException{
		MessageReceive mr = new MessageReceive();
		
		if(messageContent == null || messageContent.length() == 0){
			//为空，那么直接返回mr，当然，mr中的各项都是空的
			return mr;
		}
		
		mr.setReceiveBody(messageContent);
		
		Document doc = DocumentHelper.parseText(messageContent); 
		Element e = doc.getRootElement();   
		
		if(e.element("CreateTime") != null){
			mr.setCreateTime(Lang.stringToInt(e.element("CreateTime").getText(), 0));
		}
		if(e.element("FromUserName") != null){
			mr.setFromUserName(e.element("FromUserName").getText());
		}
		if(e.element("MsgType") != null){
			mr.setMsgType(e.element("MsgType").getText());
		}
		if(e.element("ToUserName") != null){
			mr.setToUserName(e.element("ToUserName").getText());
		}
		if(e.element("MsgId") != null){
			mr.setMsgId(e.element("MsgId").getText());
		}
		if(e.element("Content") != null){
			mr.setContent(e.element("Content").getText());
		}
		if(e.element("Description") != null){
			mr.setDescription(e.element("Description").getText());
		}
		if(e.element("Format") != null){
			mr.setFormat(e.element("Format").getText());
		}
		if(e.element("MediaId") != null){
			mr.setMediaId(e.element("MediaId").getText());
		}
		if(e.element("PicUrl") != null){
			mr.setPicUrl(e.element("PicUrl").getText());
		}
		if(e.element("ThumbMediaId") != null){
			mr.setThumbMediaId(e.element("ThumbMediaId").getText());
		}
		if(e.element("Title") != null){
			mr.setTitle(e.element("Title").getText());
		}
		if(e.element("Url") != null){
			mr.setUrl(e.element("Url").getText());
		}
		if(e.element("Event") != null){
			mr.setEvent(e.element("Event").getText());
		}
		
		if(e.element("EventKey") != null){
			mr.setEventKey(e.element("EventKey").getText());
		}
		if(e.element("Ticket") != null){
			mr.setTicket(e.element("Ticket").getText());
		}
		
		return mr;
	}
	
	/**
	 * 微信服务器接收消息或者事件后，推送到我们的服务器。我们服务器会自动处理并给微信服务器返回一个响应：微信公众号会自动给这个用户发送一条文字消息
	 * <p>相当于：</p>
	 * <pre>
	 * 	MessageReply messageReply = new MessageReply(messageReceive.getFromUserName(), messageReceive.getToUserName());
	 *	messageReply.replyText(response, content);
	 * </pre>
	 * @param response {@link HttpServletResponse}响应，输出返回值给微信服务器。
	 * @param messageReceive 使用{@link #receiveMessage(HttpServletRequest)}方法获取到的 {@link MessageReceive}。这里面可以拿到是要回复给哪个用户。
	 * @param content 微信公众号自动给触发此响应的用户发送的文字消息，这里便是文字消息的内容
	 */
	public void autoReplyText(HttpServletResponse response, MessageReceive messageReceive, String content){
		MessageReply messageReply = new MessageReply(messageReceive.getFromUserName(), messageReceive.getToUserName());
		messageReply.replyText(response, content);
	}
	
	
	/**
	 * 微信公众号开发，需首先填入与微信服务器交互的我方URL地址， 填写的URL需要正确响应微信发送的Token验证。这里便是接入时的验证的作用
	 * <p>使用时，如 SpringMVC 中：</p>
	 * <pre>
	 * 	&#64;RequestMapping(&quot;weixin&quot;)
	 *	public void verify(HttpServletRequest request, HttpServletResponse response){
	 *		WeiXinUtil.joinVerify(request, response);
	 *	}
	 * </pre>
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 */
	public void joinVerify(HttpServletRequest request, HttpServletResponse response){
		response.setContentType("text/html");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		
		String reSignature = null;
		try {
			String[] str = { token, timestamp, nonce };
			Arrays.sort(str);
			String bigStr = str[0] + str[1] + str[2];
			reSignature = new SHA1().getDigestOfString(bigStr.getBytes()).toLowerCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != reSignature && reSignature.equals(signature)) {
			//请求来自微信
			out.print(echostr);
		} else {
			out.print("error request! the request is not from weixin server");
		}
		out.flush();
		out.close();
	}
	
	
	/**
	 * 获取 JS SDK 的 ticket
	 * @return 当前可用的 ticket
	 */
	public JsapiTicket getJsapiTicket(){
		boolean refreshToken = false;	//需重新刷新获取token，默认是不需要
		
		if(jsapiTicket == null){
			jsapiTicket = new JsapiTicket();
			refreshToken = true;
		}
		
		//是否过时，需要重新获取token
		if(DateUtil.timeForUnix10()>jsapiTicket.getGainTime()+JSAPI_TICKET_DELAY_TIME){
			refreshToken = true;
		}
		
		//避免一次可能网络中断，连续获取三次，减小误差
		boolean success = !refreshToken;
		int i = 0;
		for (; i < 3 && !success ; i++) {
			success = refreshJsapiTicket();
		}
		
		if(!success){
			debug("连续获取"+i+"次 jsapi_ticket ，均失败！" );
			return null;
		}else{
			return jsapiTicket;
		}
	}
	
	/**
	 * 刷新重新获取 jsapi_ticket ，其实直接使用 getJsapiTicket() 获取可用的 ticket 即可。
	 * @return 获取成功|失败
	 */
	private boolean refreshJsapiTicket(){
		HttpUtil httpUtil = new HttpUtil();
		HttpResponse httpResponse = httpUtil.get(JSAPI_TICKET_URL.replace("ACCESS_TOKEN", getAccessToken().getAccess_token()));
		JSONObject json = JSONObject.fromObject(httpResponse.getContent());
		if(json.get("errcode") != null && json.getInt("errcode") == 0){
			//没有出错，获取成功
			jsapiTicket.setExpires_in(json.getInt("expires_in"));
			jsapiTicket.setGainTime(DateUtil.timeForUnix10());
			jsapiTicket.setTicket(json.getString("ticket"));
			return true;
		}else{
			debug("获取access_token失败！返回值："+httpResponse.getContent());
			return false;
		}
	}
	
	/**
	 * JS-SDK 生成 signature 签名,可以在页面中直接使用，如分享到朋友圈等
	 * <b>注意事项:</b>
	 * <p>1.签名的noncestr和timestamp必须与wx.config中的nonceStr和timestamp相同。</p>
	 * <p>2.签名用的url必须是调用JS接口页面的完整URL。</p>
	 * <p>3.出于安全考虑，开发者必须在服务器端实现签名的逻辑。</p>
	 * @param url 当前网页的URL，不包含#及其后面部分,如 http://mp.weixin.qq.com?params=value
	 * @return signature 签名，已处理好，拿来即可直接使用
	 */
	public SignatureBean getJsSignature(String url){
		SignatureBean bean = new SignatureBean();
		bean.setNoncestr(StringUtil.getRandomAZ(8));
		bean.setTimestamp(DateUtil.timeForUnix10());
		bean.setUrl(url);
		
		String str = "jsapi_ticket="+getJsapiTicket().getTicket()+"&noncestr="+bean.getNoncestr()+"&timestamp="+bean.getTimestamp()+"&url="+url;
		String signstr = new SHA1().getDigestOfString(str.getBytes()).toLowerCase();
		bean.setSignature(signstr);
		
		return bean;
	}
	
}
