package com.yph.shwork.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
	
	
	/**
	 * 获取异常的详细信息
	 * @param throwable
	 * @return
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			throwable.printStackTrace(pw);
			String result = sw.toString();
			return result;
		} finally {
			pw.close();
			sw.flush();
			try {
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
