package com.xnx3.weixin.vo;

import com.xnx3.BaseVO;
import com.xnx3.DateUtil;

/**
 * H5分享的vo
 * @author 管雷鸣
 *
 */
public class WebShareVO extends BaseVO{
	private String appId;		//公众号的唯一标识
	private Integer timestamp; 	//生成签名的时间戳
	private String nonceStr;	//生成签名的随机串
	private String signature;	//签名
	
	public WebShareVO() {
		this.timestamp = DateUtil.timeForUnix10(); 
	}
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public Integer getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Integer timestamp) {
		this.timestamp = timestamp;
	}
	public String getNonceStr() {
		return nonceStr;
	}
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	@Override
	public String toString() {
		return "H5ShareVO [appId=" + appId + ", timestamp=" + timestamp + ", nonceStr=" + nonceStr + ", signature="
				+ signature + "]";
	}
}
