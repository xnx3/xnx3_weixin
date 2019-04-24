package com.xnx3.weixin.bean;

/**
 * 生成签名之前必须先了解一下jsapi_ticket，jsapi_ticket是公众号用于调用微信JS接口的临时票据。正常情况下，jsapi_ticket的有效期为7200秒，通过access_token来获取。由于获取jsapi_ticket的api调用次数非常有限，频繁刷新jsapi_ticket会导致api调用受限，影响自身业务，开发者必须在自己的服务全局缓存jsapi_ticket 。
 * @author 管雷鸣
 *
 */
public class JsapiTicket {
	//获取到的凭证
	private String ticket;
	//凭证有效时间，单位：秒
	private int expires_in;
	//当前access_token获取的时间，Linux时间戳
	private int gainTime;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public int getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}

	public int getGainTime() {
		return gainTime;
	}

	public void setGainTime(int gainTime) {
		this.gainTime = gainTime;
	}

	@Override
	public String toString() {
		return "JsapiTicket [ticket=" + ticket + ", expires_in=" + expires_in + ", gainTime=" + gainTime + "]";
	}	
	
	
}
