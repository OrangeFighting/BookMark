package com.mclab.lcc.entity;

public class NewsEntity {
	private String m_sNewsURL=null;//新闻网页网址
	private String m_sNewsTime= null;//新闻的时间
	private String m_sNewsTitle=null;//新闻的标题
	private String m_sNewsImageURL=null;//新闻中一张图片的地址
	
	private int m_iVisitCount=0;//阅读数
	public NewsEntity()
	{}
	public NewsEntity(String newsURL,String newsTime,String newsTitle,String newsImageURL,int visitCount)
	{
		this.m_sNewsURL = newsURL;
		this.m_sNewsTime = newsTime;
		this.m_sNewsTitle = newsTitle;
		this.m_sNewsImageURL = newsImageURL;
		this.m_iVisitCount = visitCount;
	}
	
	public String getNewsURL()
	{
		return this.m_sNewsURL;
	}
	public void setNewsURL(String url)
	{
		this.m_sNewsURL=url;
	}
	public String getNewsTime()
	{
		return this.m_sNewsTime;
	}
	public void setNewsTime(String time)
	{
		this.m_sNewsTime=time;
	}
	public String getNewsTitle()
	{
		return this.m_sNewsTitle;
	}
	public void setNewsTitle(String title)
	{
		this.m_sNewsTitle = title;
	}
	public String getNewsImageURL()
	{
		return this.m_sNewsImageURL;
	}
	public void setNewsImageURL(String url)
	{
		this.m_sNewsImageURL = url;
	}
	public int getVisitCount()
	{
		return this.m_iVisitCount;
	}
	public void setVisitCount(int count)
	{
		this.m_iVisitCount = count;
	}
	
}
