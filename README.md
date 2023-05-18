微信常用工具类

# 快速入门使用
### 1. maven导入

````
<dependency>
    <groupId>com.xnx3.weixin</groupId>
    <artifactId>xnx3-weixin</artifactId>
    <version>1.7</version>
</dependency>
````

### 2. 写一个类测试

````
public static void main(String[] args) {
	WeiXinUtil weixin = new WeiXinUtil("你的 appId", "你的 appSecret");
	System.out.println("获取 access_token ："+weixin.getAccessToken());
}
````

# 微信支付
### 微信内网页H5支付
````
String appid = "wx07f3db3a6bbedf11";	//微信公众号appid
String mch_id = "1589606111";			//微信商户平台的商户号
String key = "e9caa063361f4c6cb0821a0131086111";	//微信商户平台的key，在微信商户平台-帐户设置-安全设置-API安全-API密钥-设置API密钥这个里面设置的KEY
String openid = "oa04fwGxDJsbIzzfwp4VPEBNGMdc";		//支付用户的openid
int money = 1;		//支付的金额，单位是分，这里1便是支付1分
String notifyUrl = "http://www.xxx.com/weixin/payCallback.json";	//支付成功后，微信异步回调通知咱的服务器url

WeiXinPayUtil util = new WeiXinPayUtil(appid, mch_id, key);	////创建微信支付 util，只创建一次即可，可多次调用 util.createOrder(....) 进行创建订单支付
JSAPIParamsVO vo = (JSAPIParamsVO) util.createOrder(new com.xnx3.weixin.weixinPay.request.JSAPIOrder(openid, money, notifyUrl));	////JSAPI 方式调起支付，比如微信网页版，就是这种支付方式。注意，如果是小程序支付，需要传入 AppletOrder ， 用 AppletParamsVO 接收
if(vo.getResult() - JSAPIParamsVO.SUCCESS == 0){
	//成功，打印出支付用的 timeStamp、nonceStr、Package.....等参数
	System.out.println(vo);
}else{
	//失败，通过 getInfo() 获取到失败原因，显示给用户
	System.out.println(vo.getInfo());
}
````
执行结果：
````
JSAPIParamsVO [getAppId()=wxb38da40ed2b11111, getTimeStamp()=1590549279, getNonceStr()=bc4eb247ac11404c9357624d3b741811, getPackage()=prepay_id=wx2711143897416175212ff0911607611111, getSignType()=MD5, getPaySign()=6BCF15DB946028390D1F89086DA01111, getResult()=1, getInfo()=成功]
````
可以直接将执行的结果返回的参数，填入 https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6  中调起支付，进行支付操作

### 微信小程序支付
[/else/AppletPay.md](/else/AppletPay.md)


# 开发文档
#### com.xnx3.weixin.WeiXinUtil	微信网页开发
- getAccessToken()	获取当前可用的 access_token (会7200秒自动刷新一次)
- getUserInfo(String openId)		通过openId，获取用户的信息
- getOauth2Url(String redirectUri,String scope,String state)	获取网页授权的URL跳转地址
- getOauth2SimpleUrl(String redirectUri)	获取网页授权的URL跳转地址，弹出授权页面，可通过openid拿到昵称、性别、所在地。并且，即使在未关注的情况下，只要用户授权，也能获取其信息
- getOauth2ExpertUrl(String redirectUri)	获取网页授权的URL跳转地址，不会出现授权页面，只能拿到用户openid
- getOauth2OpenId(String code)	获取网页授权，获取用户的openid
- getOauth2UserInfo(String code)	网页授权获取用户的个人信息
- receiveMessage(HttpServletRequest request)	接收xml格式消息，用户通过微信公众号发送消息，有服务器接收。这里将微信服务器推送来的消息进行格式化为 {@link MessageReceive}对象
- receiveMessage(String messageContent)	接收xml格式消息，用户通过微信公众号发送消息，有服务器接收。这里将微信服务器推送来的消息进行格式化为 {@link MessageReceive}对象
- autoReplyText()	微信服务器接收消息或者事件后，推送到我们的服务器。我们服务器会自动处理并给微信服务器返回一个响应：微信公众号会自动给这个用户发送一条文字消息
- joinVerify()	微信公众号开发，需首先填入与微信服务器交互的我方URL地址， 填写的URL需要正确响应微信发送的Token验证。这里便是接入时的验证的作用
- getJsapiTicket()	获取 JS SDK 的 ticket，可拿来直接使用，拿来的就是有效的
- refreshJsapiTicket()	刷新重新获取 jsapi_ticket ，其实直接使用 getJsapiTicket() 获取可用的 ticket 即可。
- getJsSignature()	JS-SDK 生成 signature 签名，可以在页面中直接使用，如分享到朋友圈等

#### com.xnx3.weixin.WeiXinAppletUtil	微信小程序
- loginByCode(String code)	根据code ，获取 openid、session_key 、 unionid