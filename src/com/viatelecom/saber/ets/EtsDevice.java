package com.viatelecom.saber.ets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import com.viatelecom.saber.Application;
import com.viatelecom.saber.ets.cfg.CfgBase;
import com.viatelecom.saber.ets.cfg.CfgCBP7;
import com.viatelecom.saber.serialport.SerialPort;

public abstract class EtsDevice {

    static protected final CfgBase mCfg = new CfgCBP7();
    protected Application mApplication;
    
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    
    private ReadThread mReadThread=null;
    
    public EtsDevice(Application app){
        mApplication = app;
    }

    private class ReadThread extends Thread {
        
        @Override
        public void run() {
            super.run();
            Log.i(Application.TagApp, "read thread start");
            
            byte[] buf_read = new byte[8192];
            byte[] buf = new byte[4096];
            
            // read data
            while(!isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null) return;

                    size = mInputStream.read(buf);
                    if (size > 0) {
                        if(size==4096)
                        {
                            Log.i(Application.TagApp, "revice: " + size + " bytes");
                        }
                        
                        //Log.v(Application.TagApp, "revice: " + size + " bytes");
                        System.arraycopy(buf, 0, buf_read, EtsMsg.size_last, size);
                        
                        int size_total = EtsMsg.size_last+size;
                        //Log.v(Application.TagApp, "Total: " + size_total + " bytes");
                        
                        onDataReceived(buf_read, size_total); // this will refresh the EtsMsg.size_last
                        
                        //Log.v(Application.TagApp, "left: " + EtsMsg.size_last + " bytes after parse");
                        System.arraycopy(buf_read, size_total-EtsMsg.size_last, buf_read, 0, EtsMsg.size_last);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            
            Log.i(Application.TagApp, "read thread exit");
        }
    }

    protected Boolean create(){
        
        try {
            mSerialPort = mApplication.getSerialPort();
            
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 
        return true;
    }
    
    protected void StartRead(){
        /* Create a receiving thread */
        mReadThread = new ReadThread();
        mReadThread.start();
    }

    protected abstract void onDataReceived(final byte[] buffer, final int size);

    protected void destroy() {
        if (mReadThread != null)
        {
            mReadThread.interrupt();
            mReadThread = null;
        }
        
        mApplication.closeSerialPort();
        mSerialPort = null;
    }

    /**
     * Write
     * @param msg
     */
    protected void write(EtsMsg msg) {
        try {
            //Log.v(Application.TagApp, "write a msg:" + msg.getId());
            byte[] buf = msg.getBuf();
            mOutputStream.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
