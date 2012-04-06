/**
 * 
 */
package com.viatelecom.saber;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.viatelecom.saber.ets.EtsDnlder;
import com.viatelecom.saber.ets.EtsDnlder.DnldStatus;
import com.viatelecom.saber.ets.EtsDnlder.EtsDnlderCallback;
import com.viatelecom.saber.ets.EtsException;


public class DownloadActivity extends ListActivity{
    private Button mBtnStart = null;
    private TextView mText = null;   
    private ProgressBar mPbar = null;
    private ArrayList<String> fileNames = null;
    private ArrayList<String> filePaths = null;
    private boolean mStateDownloading = false;
    private List<String> imgPaths = new ArrayList<String>();
    private ListView mList = null;  
    private EtsDnlder etsDownload = null;
    private final int INFO_UPDATE =1;
    private boolean mBatteryOK = false;
    private boolean mResetModem = false;
    
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){  
    
        @Override  
        public void onReceive(Context arg0, Intent intent) {  
            
            String action = intent.getAction();  
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)){  
                mBatteryOK = false;
                
                int level = intent.getIntExtra("level", 0);  
                int status = intent.getIntExtra("status", 0);  
                /*
                int health = intent.getIntExtra("health", 1);  
                boolean present = intent.getBooleanExtra("present", false);  
                int scale = intent.getIntExtra("scale", 0);  
                int plugged = intent.getIntExtra("plugged", 0);  
                int voltage = intent.getIntExtra("voltage", 0);  
                int temperature = intent.getIntExtra("temperature", 0);  
                String technology = intent.getStringExtra("technology");  
                */
                
                if(level>30){
                    mBatteryOK = true;
                    return;
                }
                
                String statusString = "unknown";  
                switch (status) {  
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:  
                        statusString = "unknown";  
                        break;  
                    case BatteryManager.BATTERY_STATUS_CHARGING:  
                        statusString = "charging";
                        mBatteryOK = true;
                   
                        break;  
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:  
                        statusString = "discharging";  
                        break;  
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:  
                        statusString = "not charging";  
                        break;  
                    case BatteryManager.BATTERY_STATUS_FULL:  
                        statusString = "full";  
                        break;  
                    }     
            
            /*
            String healthString = "unknown";  
                      
            switch (health) {  
                case BatteryManager.BATTERY_HEALTH_UNKNOWN:  
                    healthString = "unknown";  
                    break;  
                case BatteryManager.BATTERY_HEALTH_GOOD:  
                    healthString = "good";  
                    break;  
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:  
                    healthString = "overheat";  
                    break;  
                case BatteryManager.BATTERY_HEALTH_DEAD:  
                    healthString = "dead";  
                    break;  
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:  
                    healthString = "voltage";  
                    break;  
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:  
                    healthString = "unspecified failure";  
                    break;  
            }  
                   
            String acString = "Unknown";  
            switch (plugged) {  
                case BatteryManager.BATTERY_PLUGGED_AC:  
                    acString = "plugged ac";
                    break;  
                case BatteryManager.BATTERY_PLUGGED_USB:  
                    acString = "plugged usb";
                    break;  
                }  
               
            */                 
            } 
        }  
    };  
    
    private EtsDnlderCallback _cbEtsDownload = new EtsDnlderCallback(){
        public void onProcess(DnldStatus status, int progress, String info) {
            if(mStateDownloading)
            {
                Bundle data = new Bundle();
                data.putCharSequence("Dlstatus", status.toString());
                if (status == DnldStatus.Downloading) {
                    //data.putCharSequence("ProgressInfo", progress+"%"+"\n");
                    data.putInt("ProgressInfo", progress);
                    //Log.v("progress", progress+"%");
                } else {
                    data.putCharSequence("DownloadInfo", info + "\n");
                    Log.v("info", info+"\n");
                }   
                //Log.v("status", status+"\n");
                Message msg = Message.obtain(mHandler, INFO_UPDATE);
                msg.setData(data);              
                mHandler.sendMessageDelayed(msg, 1000);
            }
        }
    };
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
                             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.download);
        registerReceiver(mBatInfoReceiver,   new IntentFilter(Intent.ACTION_BATTERY_CHANGED));  
        
        mResetModem = SystemProperties.get("persist.cp.reset.mode","0").equals("1");
        if (!mResetModem) {
            SystemProperties.set("persist.cp.reset.mode","1");
        }
        Intent intent = getIntent();
        
        fileNames = intent.getStringArrayListExtra("com.viatelecom.saber.names");
        filePaths = intent.getStringArrayListExtra("com.viatelecom.saber.paths");
        
        mPbar = (ProgressBar) findViewById(R.id.prbar);
        mPbar.setVisibility(View.GONE);
        
        mText = (TextView) findViewById(R.id.tiptext);
        mText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mText.setMaxLines(15);
        mBtnStart = (Button) findViewById(R.id.bt_start);
        
        mList = getListView();
        
        etsDownload = new EtsDnlder((Application)getApplication(), _cbEtsDownload);
        
        mBtnStart.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View v) {
                if (v == mBtnStart) {
                    if(!mBatteryOK){
                        Toast.makeText(v.getContext(), "Battery is not enough for updating.\nPlease connect your power charger.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    setPathArray();
                    if (mStateDownloading == false) {
                        if (!imgPaths.isEmpty()) {
                            try {
                                mText.setText("");                          
                                mStateDownloading = true;
                                etsDownload.start(imgPaths);
                                
                                mBtnStart.setVisibility(View.GONE);
                                mList.setVisibility(View.GONE);
                                
                            }catch (EtsException e) {
                                mStateDownloading = false;
                                Log.e("etslog_exception", e.getMessage());
                                mText.setText(e.getMessage()+"\n");
                            mPbar.setVisibility(View.GONE);                            
                        }
                        } else {
                            mText.setText(R.string.no_imgfilesel);                          
                        }
                    } 
                }
            }   
        });
        
        setListContent();
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
           super.handleMessage(msg);
            switch (msg.what) {
                case INFO_UPDATE:   
                    if (msg.getData().getCharSequence("Dlstatus").toString().compareTo(DnldStatus.Downloading.toString())== 0) {
                        mText.setText(R.string.download_exit_warning);
                        
                        mPbar.setVisibility(View.VISIBLE);
                        mPbar.setProgress(msg.getData().getInt("ProgressInfo"));
                        
                        if(msg.getData().getInt("ProgressInfo")==100) {
                            // download successful
                            mPbar.setVisibility(View.GONE);
                            mText.setText("");
                            
                            Toast.makeText(mBtnStart.getContext(), "Download image successful", Toast.LENGTH_LONG).show();
                        }
                        
                    } else {
                        mText.append(msg.getData().getCharSequence("DownloadInfo"));
                        
                        if(msg.getData().getCharSequence("Dlstatus").toString().compareTo(DnldStatus.Finishied.toString())== 0 ||                            
                           msg.getData().getCharSequence("Dlstatus").toString().compareTo(DnldStatus.Error.toString()) == 0) {
                        
                            mStateDownloading = false;
                            mBtnStart.setVisibility(View.VISIBLE);
                            mText.clearFocus();
                            
                            mList.clearChoices();
                            mList.setVisibility(View.VISIBLE);                  
                            mPbar.setVisibility(View.GONE);
                        }
                    }                   
                   break;                 
                default:
                    break;
           }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {        
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            if (mStateDownloading == true){
                Toast.makeText(getApplicationContext(), getBaseContext().getString(R.string.download_exit_warning), Toast.LENGTH_SHORT).show();
                return true;
            }                           
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void setListContent () {
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, fileNames));       
        final ListView listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 
    
    }

    private void setPathArray() {
        imgPaths.clear();
        long ids[]=mList.getCheckItemIds();
        for(int i=0;i<ids.length;++i){
            imgPaths.add(filePaths.get((int) ids[i]));
        }
        
    }
    
    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {   
        mText.setText(R.string.tip_text);           
    }

    @Override  
    protected void onResume() {  
        super.onResume();  
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));  
    }  
  
    @Override  
    protected void onPause() {  
       super.onPause();  
       unregisterReceiver(mBatInfoReceiver);  
    } 

    @Override  
    protected void onDestroy() {  
       super.onDestroy();  
        if (!mResetModem) {
            SystemProperties.set("persist.cp.reset.mode","0");
        }
    } 

}
