package com.mclab.lcc.model;

/**
 * Created by Administrator on 2015/4/7.
 */
@SuppressWarnings("serial")
public class CallInfoServerMethodException extends Exception {


    private int m_iErrorCode;
    private String m_strMessage;


    public CallInfoServerMethodException(int errorCode,String msg)
    {
        super(msg);
        m_iErrorCode=errorCode;
        m_strMessage=msg;
    }
    public int getErrorCode()
    {
        return m_iErrorCode;
    }
    public String GetErrorMessage()
    {
        return m_strMessage;
    }
    @Override
    public String getLocalizedMessage()
    {
        return m_strMessage;
    }
    @Override
    public String getMessage() {
        return m_strMessage;
    }

}
