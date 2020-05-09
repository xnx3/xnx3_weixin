package com.xnx3.weixin.weixinPay;

import java.util.Map;

import com.xnx3.BaseVO;
import com.xnx3.Lang;

/**
 * 微信支付成功后，异步回调，微信服务器返回的数据。 这里是已经验证完签名无误之后，需要java进行处理的
 * @author 管雷鸣
 *
 */
public class PayCallBackParamsVO extends BaseVO{
	private Map<String, String> params;	//微信服务器发送过来的信息
	private String outTradeNo; //订单号
	private int money;	//实际支付的金额，单位是分
	
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
		
		if(params.get("total_fee") != null){
			this.money = Lang.stringToInt(params.get("total_fee").toString(), 0);
		}
		if(params.get("out_trade_no") != null){
			this.outTradeNo = params.get("out_trade_no").toString();
		}
	}
	public String getOutTradeNo() {
		return outTradeNo;
	}
	public int getMoney() {
		return money;
	}
	@Override
	public String toString() {
		return "PayCallBackParamsBean [params=" + params + ", outTradeNo=" + outTradeNo + ", money=" + money
				+ ", toString()=" + super.toString() + "]";
	}
}
