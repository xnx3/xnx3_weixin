package com.xnx3.weixin.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xnx3.weixin.bean.MessageReceive;

/**
 * 微信公众号自动回复
 * @author 管雷鸣
 *
 */
public interface AutoReply {
	
	/**
	 * 微信公众号，用户发送内容到微信公众号后，程序接收到用户的回复，自动进行处理
	 * @param request 微信服务器请求过来的 request
	 * @param response 要给微信服务器的响应 response
	 * @param messageReceive 微信服务器发送过来的信息，封装成 {@link MessageReceive}
	 */
	public void reply(HttpServletRequest request, HttpServletResponse response, MessageReceive messageReceive);
	
}
