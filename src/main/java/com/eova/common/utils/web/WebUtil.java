package com.eova.common.utils.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.kit.LogKit;

public class WebUtil {
	
	/**
	 * 从请求流中快速读取文件内容，例如JSON，TXT，XML
	 * 
	 * @param in
	 * @param charSet
	 * @return
	 */
	public static String readData(HttpServletRequest request, String charSet) {
		InputStream in = null;
		StringBuilder sb = new StringBuilder();
		try {
			in = request.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, charSet));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogKit.error(e.getMessage(), e);
				}
			}
		}
	}

	public static String readData(HttpServletRequest request) {
		return readData(request, "UTF-8");
	}

	/**
     * 获取用户端请求的真实IP地址
     *
     * @param request
     * @return
     */
    public static String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.indexOf(",") > -1) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
