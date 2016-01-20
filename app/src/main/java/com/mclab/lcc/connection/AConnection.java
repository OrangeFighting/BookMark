package com.mclab.lcc.connection;

import java.util.Map;

import android.content.Context;

public abstract class AConnection {

	private static Context s_cxt = null;
	/**
	 * 表示连接等待时间
	 */
	protected static int s_iRequestTimeOut = 15000; 
	/**
	 * 
	 * @return 设置的连接等待时间
	 */
	public static int getRequestTimeOut() {
		return s_iRequestTimeOut;
	}
	/**
	 * 设置连接等待时间
	 * @param s_iRequestTimeOut 超时时间，毫秒
	 */
	public static void setRequestTimeOut(int s_iRequestTimeOut) {
		AConnection.s_iRequestTimeOut = s_iRequestTimeOut;
	}
	/**
	 * 实现信息的传输，指定目的地址，文本参数列表后，返回对应server的信息。
	 * @param method 调用的远程服务器的方法
	 * @param param 该远程方法的文本参数
	 * @return 服务器返回的响应文本
	 */
	public abstract String sendMessage(Map<String,String>[]param)throws Exception;
	public static void SetContext(Context context)
	{
		s_cxt = context;
	}
}
