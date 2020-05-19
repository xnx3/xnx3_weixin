package com.xnx3.weixin.vo;

import com.xnx3.BaseVO;

/**
 * 微信小程序获取手机号，获取到手机号返回数据
 * @author 管雷鸣
 *
 */
public class PhoneVO extends BaseVO{
	private String phone;	//手机号，如 17076000000
	private String countryCode;	//国家码，如 86
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	@Override
	public String toString() {
		return "PhoneVO [phone=" + phone + ", countryCode=" + countryCode + "]";
	}
}
