
package com.viatelecom.saber;

import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;


import com.viatelecom.saber.ets.EtsLog;
import com.viatelecom.saber.ets.EtsLog.EtsLogCallback;
import com.viatelecom.saber.ets.EtsLog.LogStatus;

public class CpLogService extends Service{
    private static final String TAG = "CpLogService";
    private static final String UPDATE_INFO_ACTION = "android.intent.action.UPDATE_INFO";
    private EtsLog etsLog = null;
    private final int LOG_UPDATE =1;

    
    private EtsLogCallback _cbEtsLog = new EtsLogCallback(){
        public void onProcess(LogStatus status, String info) {
            if (status == LogStatus.Error || status == LogStatus.Logging){
                Intent intent = new Intent();
                intent.putExtra("LogInfo", info+"\n");
                intent.setAction(UPDATE_INFO_ACTION);
                sendBroadcast(intent);
                Log.i("etsLog", info);
            }
        }
    };
    
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    public void onCreate() {
        Log.d(TAG, "onStartCommand");  
        
        etsLog = new EtsLog((Application)getApplication(), _cbEtsLog);
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");  
        String path = intent.getStringExtra("PATH");
        etsLog.start(path);
        setForeground(true);
        return START_STICKY; 
    }

    public void onLowMemory (){
        Log.d(TAG, "onLowMemory");
    }
    
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        etsLog.stop();
    }

}

