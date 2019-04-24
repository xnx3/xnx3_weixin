# weixin develop

# 开发文档
#### com.xnx3.weixin.WeiXinUtil
- getAccessToken()	获取当前可用的 access_token (7200秒刷新一次的)
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


#### com.xnx3.weixin.XiaoChengXuUtil
- jscode2session(String code)	根据code ，获取 openid、session_key 、 unionid