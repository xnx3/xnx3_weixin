package com.xnx3.weixin.weixinPay.response;

import com.xnx3.BaseVO;
import com.xnx3.DateUtil;
import com.xnx3.Lang;

/**
 * 支付的订单信息 。 小程序支付、微信网页支付 等，都实现此。
 * 使用时不要使用这个，请使用如 {@link JSAPIOrder}
 * @author 管雷鸣
 *
 */
public abstract class ParamsVO extends BaseVO{
	
	private String appId;	//公众号id
	private int timeStamp;	//时间戳
	private String nonceStr;	//随机字符串
	private String package_;	//订单详情扩展字符串，如 prepay_id=123456789
	private String signType;	//签名方式，签名类型，默认为MD5，支持HMAC-SHA256和MD5。注意此处需与统一下单的签名类型一致
	private String paySign;		//签名
	
	public ParamsVO() {
		timeStamp = DateUtil.timeForUnix10();
		nonceStr = Lang.uuid();
		signType = "MD5";
	}
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public int getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getNonceStr() {
		return nonceStr;
	}
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
	public String getPackage() {
		return package_;
	}
	/**
	 * 设置package，这里只需要传入prepay_id的值即可
	 * @param package_
	 */
	public void setPackage(String package_) {
		if(package_.indexOf("prepay_id=") > -1){
			this.package_ = package_;
		}else{
			this.package_ = "prepay_id="+package_;
		}
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getPaySign() {
		return paySign;
	}
	public void setPaySign(String paySign) {
		this.paySign = paySign;
	}
	
	
}
