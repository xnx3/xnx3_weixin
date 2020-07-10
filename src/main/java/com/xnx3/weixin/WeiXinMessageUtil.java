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
import com.xnx3.Lang;
import com.xnx3.weixin.bean.MessageReceive;
import com.xnx3.weixin.bean.MessageReply;

/**
 * 微信公众号对话沟通，用户发送消息给公众号，公众号将消息推送到服务器，服务器进行接收，处理，再回复等
 * @author 管雷鸣
 */
public class WeiXinMessageUtil implements java.io.Serializable{
	public WeiXinUtil weixinUtil;	//操作的微信公众号
	public boolean debug = true;	//调试日志是否打印
	private String token;	//用户于微信公众平台双方拟定的令牌Token
	
	/**
	 * 微信基本操作-不涉及小程序。微信小程序使用 {@link XiaoChengXuUtil}
	 * @param weixinUtil 要操作的微信公众号
	 * @param token 用户于微信公众平台双方拟定的令牌Token
	 */
	public WeiXinMessageUtil(WeiXinUtil weixinUtil, String token) {
		this.weixinUtil = weixinUtil;
		this.token = token;
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
	
	
}
