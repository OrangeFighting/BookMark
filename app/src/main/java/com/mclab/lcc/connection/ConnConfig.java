package com.mclab.lcc.connection;



import android.content.Context;

public class ConnConfig {

	public static final String IP = "127.0.0.1";
	public static final String PORT = "8080";
	public static final String SERVLET_PAGE = "index.jsp";
	
	private static AConnection s_connInstance = null;
	private static Context s_ctx = null;
	
	public static void initConfig(Context contex)
	{
		s_ctx = contex;
	}
	public static void setContext(Context context)
	{
		s_ctx = context;
		AConnection.SetContext(context);
	}
	public static AConnection getInstance()
	{
		if(s_connInstance==null)
		{
			AConnection.SetContext(s_ctx);
			AConnection.setRequestTimeOut(15000);
			MyHTTPConnection.setHostIP(IP);
			MyHTTPConnection.setPort(PORT);
			MyHTTPConnection.setServletPage(SERVLET_PAGE);
			
			s_connInstance = MyHTTPConnection.getInstance();
		}
		return s_connInstance;
	}
}
