package com.viatelecom.saber.ets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import com.viatelecom.saber.Application;
import com.viatelecom.saber.ets.cfg.CfgBase;
import com.viatelecom.saber.ets.cfg.CfgLenovoK1;
import com.viatelecom.saber.serialport.SerialPort;

public abstract class EtsDevice {

    static protected final CfgBase mCfg = new CfgLenovoK1();
    protected Application mApplication;
    
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    
    public EtsDevice(Application app){
        mApplication = app;
    }

    private class ReadThread extends Thread {
        
        @Override
        public void run() {
            super.run();
            Log.i("EtsDevice", "read thread start");
            
            // read data
            while(!isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null) return;
                    
                    byte[] buffer = new byte[2048];
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        //Log.v("EtsDevice", "revice " + size + " bytes");
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            
            Log.i("EtsDevice", "read thread exit");
        }
    }

    protected Boolean create(){
        
        try {
            mSerialPort = mApplication.getSerialPort();
            
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            /* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 
        return true;
    }

    protected abstract void onDataReceived(final byte[] buffer, final int size);

    protected void destroy() {
        if (mReadThread != null)
            mReadThread.interrupt();
        
        mApplication.closeSerialPort();
        mSerialPort = null;
    }

    /**
     * Write
     * @param msg
     */
    protected void write(EtsMsg msg) {
        try {
            //Log.v("EtsDevice", "write a msg:" + msg.getId());
            byte[] buf = msg.getBuf();
            mOutputStream.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
