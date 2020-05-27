package com.xnx3.weixin.weixinPay.response;

import java.util.SortedMap;
import java.util.TreeMap;

import com.xnx3.weixin.SignUtil;

/**
 * APP调起支付，需要的一些参数
 * 如果 getResult() - AppParamsVO.FAILURE == 0， 那么 getInfo() 返回失败原因
 * @author 管雷鸣
 *
 */
public class AppParamsVO extends ParamsVO{

	/**
	 * 生成签名，并返回新的 {@link JSAPIParamsVO} 对象
	 * @param key 商户key，签名用的key
	 * @return 增加了签名的{@link JSAPIParamsVO}
	 */
	public AppParamsVO generateSign(String key){
		SortedMap<String, String> paraMap = new TreeMap<String, String>();
        //设置请求参数(公众号、小程序ID)
        paraMap.put("appId", super.getAppId());
        //设置请求参数(商户号)
        paraMap.put("timeStamp", super.getTimeStamp()+"");
        //设置请求参数(随机字符串)
        paraMap.put("nonceStr", super.getNonceStr());
        //设置请求参数(商品描述)
        paraMap.put("package", super.getPackage());
        //设置请求参数(商户订单号)
        paraMap.put("signType", super.getSignType());
        String sign = SignUtil.generateSign(paraMap, key);
        
        //将签名加入此vo
        super.setPaySign(sign);
		return this;
	}

	
}
