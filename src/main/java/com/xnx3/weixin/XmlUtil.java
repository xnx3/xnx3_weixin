package com.xnx3.weixin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.xnx3.StringUtil;

/**
 * XML简单工具类
 * @author 管雷鸣
 *
 */
public class XmlUtil {

    /**
     * 解析xml,返回第一级元素键值对。
     * @param strxml xml的字符串形式
     * @return Map形式
     */
    public static Map<String,String> stringToMap(String strxml){
        if(null == strxml || "".equals(strxml)) {
            return null;
        }
        
        Map<String,String> map = new HashMap<String,String>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document d = builder.parse(StringUtil.stringToInputStream(strxml, "UTF-8"));
			if(d.hasChildNodes()){
				Node node = d.getFirstChild();	//获取XML节点
				NodeList nodelist = node.getChildNodes();
				for (int i = 0; i < nodelist.getLength(); i++) {
					Node item = nodelist.item(i);
					map.put(item.getNodeName(), item.getTextContent());
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return map;
    }
    
    public static void main(String[] args) throws Exception {
		String xml = "<xml><appid><![CDATA[wx07f3db3a6bbedfbe]]></appid><bank_type><![CDATA[OTHERS]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[Y]]></is_subscribe><mch_id><![CDATA[1589606251]]></mch_id><nonce_str><![CDATA[64ff9105387847a4b2bd0af830aeb4f4]]></nonce_str><openid><![CDATA[oa04fwGxDJsbIzzfwp4VPEBNGMdc]]></openid><out_trade_no><![CDATA[i9qa5lk3]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[3B31E24B24F887E999D7AC8763213C60]]></sign><sub_appid><![CDATA[wx7cce7d82c7cf245a]]></sub_appid><sub_is_subscribe><![CDATA[N]]></sub_is_subscribe><sub_mch_id><![CDATA[1591496141]]></sub_mch_id><sub_openid><![CDATA[oMXL_4_0q-cuteDgVLPLe8mzT7rE]]></sub_openid><time_end><![CDATA[20200511141930]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[JSAPI]]></trade_type><transaction_id><![CDATA[4200000542202005113511447596]]></transaction_id></xml>";
    	Map<String, String> map = stringToMap(xml);
    	for(Map.Entry<String, String> entry : map.entrySet()){
    	    String mapKey = entry.getKey();
    	    String mapValue = entry.getValue();
    	    System.out.println(mapKey+":"+mapValue);
    	}
	}
}
