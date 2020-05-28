# 微信服务商模式调起子商户的支付。
这里以小程序支付为例
````
String appid = "wx07f3db3a6bbedf11";	//微信小程序appid
String mch_id = "1589606111";			//服务商的mch_id
String key = "e9caa063361f4c6cb0821a0131086111";	//微信服务商的商户平台key，在微信商户平台-帐户设置-安全设置-API安全-API密钥-设置API密钥这个里面设置的KEY
String openid = "oa04fwGxDJsbIzzfwp4VPEBNGMdc";		//支付用户的openid
int money = 1;		//支付的金额，单位是分，这里1便是支付1分
String notifyUrl = "http://www.xxx.com/weixin/payCallback.json";	//支付成功后，微信异步回调通知咱的服务器url

WeiXinPayUtil util = new WeiXinPayUtil(appid, mch_id, key);	////创建微信支付 util，只创建一次即可，可多次调用 util.createOrder(....) 进行创建订单支付
util.openServiceProviderMode("sub_mch_id");//子商户的mch_id
util.setServiceProviderSubAppletAppid("sub_applet_appid");	//子商户自己认证的小程序appid
AppletParamsVO vo = (AppletParamsVO) util.createOrder(new com.xnx3.weixin.weixinPay.request.serviceProvider.AppletOrder(openid, money, notifyUrl));	////JSAPI 方式调起支付，比如微信网页版，就是这种支付方式。注意，如果是小程序支付，需要传入 AppletOrder ， 用 AppletParamsVO 接收
if(vo.getResult() - AppletParamsVO.SUCCESS == 0){
	//成功，打印出支付用的 timeStamp、nonceStr、Package.....等参数
	System.out.println(vo);
}else{
	//失败，通过 getInfo() 获取到失败原因，显示给用户
	System.out.println(vo.getInfo());
}
````
执行结果：
````
AppletParamsVO [getAppId()=wxb38da40ed2b11111, getTimeStamp()=1590549279, getNonceStr()=bc4eb247ac11404c9357624d3b741811, getPackage()=prepay_id=wx2711143897416175212ff0911607611111, getSignType()=MD5, getPaySign()=6BCF15DB946028390D1F89086DA01111, getResult()=1, getInfo()=成功]
````
可以直接将执行的结果返回的参数，填入 https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6  中调起支付，进行支付操作
