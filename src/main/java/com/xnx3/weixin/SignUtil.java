package com.xnx3.weixin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import com.xnx3.Lang;
import com.xnx3.MD5Util;
import com.xnx3.weixin.vo.WebShareVO;

/**
 * 签名生成相关
 * @author 管雷鸣
 *
 */
public class SignUtil {
	
	/**
	 * 生成微信支付的签名
	 * @param paraMap 签名字段、值列表
	 * @param key 商户Key
	 * @return 签名
	 */
	public static String generateSign(Map<String, String> paraMap, String key){
		//调用逻辑传入参数按照字段名的 ASCII 码从小到大排序（字典序）
        String stringA = formatUrlMap(paraMap, false, false);
        String sign = MD5Util.MD5(stringA+"&key="+key).toUpperCase();
        return sign;
	}
	
	/**
	 * 微信H5分享给好友、朋友圈 等用到。这里返回的vo只是赋值 timestamp、 nonceStr、signature 三个
	 * @param jsapi_ticket 
	 * @param url
	 * @return
	 */
	public static WebShareVO generateSign(String jsapi_ticket, String url){
		WebShareVO vo = new WebShareVO();
		vo.setNonceStr(Lang.uuid());
		
		String string1;
		String signature = "";
		
		//注意这里参数名必须全部小写，且必须有序
		string1 = "jsapi_ticket=" + jsapi_ticket +
		  "&noncestr=" + vo.getNonceStr() +
		  "&timestamp=" + vo.getTimestamp() +
		  "&url=" + url;
		
		try{
		    MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes("UTF-8"));
		    signature = byteToHex(crypt.digest());
		}catch (NoSuchAlgorithmException e){
		    e.printStackTrace();
		}catch (UnsupportedEncodingException e){
		    e.printStackTrace();
		}
		
		vo.setSignature(signature);
		return vo;
	}
	
	private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
	
    /** 
     *  
     * 方法用途: 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序），并且生成url参数串<br> 
     * 实现步骤: <br> 
     *  
     * @param paraMap   要排序的Map对象 
     * @param urlEncode   是否需要URLENCODE 
     * @param keyToLower    是否需要将Key转换为全小写 
     *            true:key转化成小写，false:不转化 
     * @return 生成的url参数串
     */  
    public static String formatUrlMap(Map<String, String> paraMap, boolean urlEncode, boolean keyToLower){  
        String buff = "";  
        Map<String, String> tmpMap = paraMap;  
        try {  
            List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(tmpMap.entrySet());  
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）  
            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {  
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)  {  
                    return (o1.getKey()).toString().compareTo(o2.getKey());  
                }  
            });  
            // 构造URL 键值对的格式  
            StringBuilder buf = new StringBuilder();  
            for (Map.Entry<String, String> item : infoIds)  {
                
            	if (item.getKey() != null && item.getKey().length() > 0)  
                {  
                    String key = item.getKey();  
                    String val = item.getValue();  
                    if (urlEncode)  
                    {  
                        val = URLEncoder.encode(val, "utf-8");  
                    }  
                    if (keyToLower)  
                    {  
                        buf.append(key.toLowerCase() + "=" + val);  
                    } else  
                    {  
                        buf.append(key + "=" + val);  
                    }  
                    buf.append("&");  
                }  
   
            }  
            buff = buf.toString();  
            if (buff.isEmpty() == false)  
            {  
                buff = buff.substring(0, buff.length() - 1);  
            }  
        } catch (Exception e)  
        {  
           return null;  
        }  
        return buff;  
    }
	
}
