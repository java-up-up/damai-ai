package org.javaup.ai.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 字符串工具类
 * @author: 阿星不是程序员
 **/
@Slf4j
public class StringUtil {

	/**
	 * 判断字符串不为空
	 * @param str 字符串
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return (str != null && !str.isEmpty() && !str.trim().isEmpty() && !"null".equalsIgnoreCase(str.trim())
				&& !"undefined".equalsIgnoreCase(str.trim()) && !"NULL".equalsIgnoreCase(str.trim()));

	}

	/**
	 * 判断字符串为空
	 * @param str	字符串
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return !StringUtil.isNotEmpty(str);
	}
	
	/**
	 * 将流转换为字符串
	 * @param is 文件流
	 * @return
	 */
	public static String inputStreamConvertString(InputStream is){
		ByteArrayOutputStream baos = null;
		String result = null;
		try {
			if(is != null) {
				baos = new ByteArrayOutputStream();
				int i;
				while ((i = is.read()) != -1) {
					baos.write(i);
				}
				result = baos.toString();
			}
		}catch(IOException e) {
			throw new RuntimeException("流转换为字符串失败！");
		}finally {
			if(baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					log.error("关闭流失败！");
				}
			}
		}
		return result;
	}
	
	/**
	 * 将URL参数转成map
	 * */
	public static Map<String, String> convertQueryStringToMap(String queryString) {
		Map<String, String> resultMap = new HashMap<>(256);
		String[] params = queryString.split("&");
		for (String param : params) {
			String[] keyValue = param.split("=");
			if (keyValue.length == 2) {
				try {
					String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
					String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
					resultMap.put(key, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return resultMap;
	}
	
	/**
	 * 获取字符串地前N个字符
	 * */
	public static String getFirstN(String input, int length) {
		if (input == null || length <= 0) {
			return "";
		}
		if (input.length() <= length) {
			return input;
		}
		return input.substring(0, length);
	}
	
	/**
	 * 获取字符串地后N个字符
	 * */
	public static String getLastN(String input, int length) {
		if (input == null || length <= 0) {
			return ""; 
		}
		if (input.length() <= length) {
			return input; 
		}
		return input.substring(input.length() - length);
	}
}
