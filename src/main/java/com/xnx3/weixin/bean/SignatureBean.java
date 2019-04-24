package com.xnx3.weixin.bean;

/**
 * JS SDK 签名获取的值，包含signature、noncestr、timestamp等
 * @author 管雷鸣
 *
 */
public class SignatureBean {
	private String signature;	// js sdk 签名
	private String noncestr;	//	随机字符串
	private int timestamp;		//时间戳
	private String url;			//当前网页的URL，不包含#及其后面部分
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getNoncestr() {
		return noncestr;
	}
	public void setNoncestr(String noncestr) {
		this.noncestr = noncestr;
	}
	public int getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String toString() {
		return "SignatureBean [signature=" + signature + ", noncestr=" + noncestr + ", timestamp=" + timestamp
				+ ", url=" + url + "]";
	}
	
	
}
