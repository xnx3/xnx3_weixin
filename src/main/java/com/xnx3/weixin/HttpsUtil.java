package com.xnx3.weixin;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 微信使用的https接口请求
 * @author 管雷鸣
 *
 */
public class HttpsUtil {
	
	/**
	 * post请求，返回响应的内容
     * @param url 请求url
     * @param data post 请求发送的data信息。这里不是key-value的形式，而是直接提交字符串到url
     * @return 响应的内容
    */
    public static String post(String url, String data){
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            // 设置连接超时时间
            conn.setConnectTimeout(30000);
            // 设置读取超时时间
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");
            if(data != null && data.length() > 0) {
                conn.setRequestProperty("Origin", "https://sirius.searates.com");// 主要参数
                conn.setRequestProperty("Referer", "https://sirius.searates.com/cn/port?A=ChIJP1j2OhRahjURNsllbOuKc3Y&D=567&G=16959&shipment=1&container=20st&weight=1&product=0&request=&weightcargo=1&");
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");// 主要参数
            }
            // 需要输出
            conn.setDoInput(true);
            // 需要输入
            conn.setDoOutput(true);
            // 设置是否使用缓存
            conn.setUseCaches(false);
            // 设置请求属性
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");	//application/x-www-form-urlencoded
            conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            
            if(data != null && data.length() > 0) {
                // 建立输入流，向指向的URL传入参数
                DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(data);
                dos.flush();
                dos.close();
            }
            // 输出返回结果
            InputStream input = conn.getInputStream();
            int resLen =0;
            byte[] res = new byte[1024];
            StringBuilder sb=new StringBuilder();
            while((resLen=input.read(res))!=-1){
                sb.append(new String(res, 0, resLen));
            }
            return sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
