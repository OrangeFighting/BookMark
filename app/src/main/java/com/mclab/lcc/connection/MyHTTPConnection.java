package com.mclab.lcc.connection;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class MyHTTPConnection extends AConnection{

	private static String s_strHostIP = "";
	private static String s_strPort = "8080";
	private static String s_strServletPage = "";
	private static final String CHARSET = "UTF-8";
	
	private static MyHTTPConnection s_mConnInstance = null;
	
	public static MyHTTPConnection getInstance() {
		if(s_mConnInstance==null)
		{
			s_mConnInstance = new MyHTTPConnection();
		}
		return s_mConnInstance;
	}
	public static void setHostIP(String ip)
	{
		s_strHostIP=ip;
	}
	public static void setPort(String port)
	{
		s_strPort=port;
	}
	public static void setServletPage(String page)
	{
		s_strServletPage=page;
	}
	private String getServerMethodPath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(s_strHostIP);
		if(s_strPort!=null)
		{
			sb.append(":");
			sb.append(s_strPort);
		}
		sb.append("/");
		sb.append(s_strServletPage);
		
		return sb.toString();
	}
	private HttpURLConnection getConnection() throws IOException
	{
		String url = getServerMethodPath();
		URL uri = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
		conn.setConnectTimeout(s_iRequestTimeOut); //建立连接的最长时间
		conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        //conn.setRequestProperty("connection", "keep-alive");
        //conn.setRequestProperty("Charsert", CHARSET);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        return conn;
	}
	/**
	 * 将参数转换成相应的传输数据
	 * @param param key表示参数的name，value表示传到服务器的值
	 * @return 符合文本格式的数据
	 */
	private String combineRequestParam(Map<String,String>[] param)
	{
		StringBuilder stringBuilder = new StringBuilder();
		if (param != null && !param[0].isEmpty()) {  
            for (Map.Entry<String, String> entry : param[0].entrySet()) {  
                try {  
                    stringBuilder  
                            .append(entry.getKey())  
                            .append("=")  
                            .append(URLEncoder.encode(entry.getValue(), CHARSET))  
                            .append("&");  
                } catch (UnsupportedEncodingException e) {  
                    e.printStackTrace();
                }  
            } 
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		}
            return stringBuilder.toString();
	}
	
	@Override
	public String sendMessage(Map<String, String>[] param)
			throws Exception {
		HttpURLConnection conn = getConnection();
		//组拼参数
		String strParam = combineRequestParam(param);
		//发送数据
		byte[] mydata = strParam.getBytes();
		conn.setRequestProperty("Content-Length", String.valueOf(mydata.length));
		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());//getoutputstream 隐含连接操作，outputstream就够了
		//OutputStream os = conn.getOutputStream();
		outStream.write(mydata,0,mydata.length);
        outStream.flush();
		outStream.close();
		
		// 得到响应码
        int response = conn.getResponseCode();

        InputStream inputStream = null;
        if(response==200)
		 {
			 inputStream = conn.getInputStream();//直到这里,连接成功，开始执行http请求
			 String res = changeInputStream(inputStream,CHARSET);
			 inputStream.close();
			 conn.disconnect();
			 return res;
		 }
        //inputStream.close();
        conn.disconnect();
		return null;
	}
	private String changeInputStream(InputStream inputStream, String encode) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		String result =null;
		int len =0;
		if(inputStream!=null)
		{
			try
			{
				while((len = inputStream.read(buff))!=-1)
				{
					byteArrayOutputStream.write(buff, 0, len);
				}
				result = new String(byteArrayOutputStream.toByteArray(),encode);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		return result;
	}
}
