package com.viatelecom.saber.ets;

import java.util.LinkedList;

import com.viatelecom.saber.Application;

import android.util.Log;

public class EtsMsgQueue extends LinkedList<EtsMsg> {

    @Override
    public boolean offer(EtsMsg o) {
        //Log.v(Application.TagApp, "offer a msg:"+o.getId());
        boolean ret = false;
        synchronized(this){
            if(super.offer(o)) {
                notify();
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public synchronized EtsMsg poll() {
        return super.poll();
    }
    
    /**
     * find a msg witch special id
     * @param id
     * @return
     */
    private EtsMsg findMsg(short id) {
        EtsMsg ret = null;
        while (!isEmpty()) {
            EtsMsg msg = null;
            synchronized(this) {
                msg = poll();
            }
            if (id==(short)0xFFFF||id==msg.getId()) {
                ret=msg;
                break;
            }
        }
        return ret;
    }

    public EtsMsg waitForMsg(short id, long timeout) {
        EtsMsg ret = null;
        //Log.v(Application.TagApp, "wait for msg:" + id);
        
        long end = System.currentTimeMillis() + timeout;
        try {
            while (System.currentTimeMillis()<end) {
                
                if(isEmpty()){
                    Log.v(Application.TagApp, "cache is empty, wait for " + timeout/3 + " ms");
                    synchronized(this){
                        wait(timeout/3);
                    }
                }
                
                ret = findMsg(id);
                if (ret==null){
                    Log.v(Application.TagApp, "time out, wait for 100 ms to continue");
                    Thread.sleep(100);
                    continue;
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if(ret==null){
            Log.w(Application.TagApp, "can't get the special msg");
        }
        
        return ret;
    }
    
}
