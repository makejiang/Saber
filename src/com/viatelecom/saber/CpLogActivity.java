package com.viatelecom.saber;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.viatelecom.saber.R;
import com.viatelecom.saber.ets.EtsException;

public class CpLogActivity extends ListActivity{

    private static final String UPDATE_INFO_ACTION = "android.intent.action.UPDATE_INFO";
    private Button mButton = null;
    private TextView mText = null;  
    private ArrayList<String> fileNames = null;
    private ArrayList<String> filePaths = null;
    private boolean mState = false;
    private String mPath = null;
    private ListView mList = null;
    private final int LOG_UPDATE =1;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cplog);

        Intent intent = getIntent();
        fileNames = intent.getStringArrayListExtra("com.viatelecom.saber.names");
        filePaths = intent.getStringArrayListExtra("com.viatelecom.saber.paths");

        mText = (TextView) findViewById(R.id.tiptext);
        mText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mText.setMaxLines(15);
        mButton = (Button) findViewById(R.id.bt_start);
        mList = getListView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_INFO_ACTION);
        registerReceiver(mReceiver, filter);


        mButton.setOnClickListener(new android.view.View.OnClickListener() {

            public void onClick(View v) { 
                // TODO Auto-generated method stub
                if (v == mButton) {
                    if (mState == false) {
                        if (mPath!= null && FileUtil.sdCardExist()) {
                            try {
                                mText.setText("");
                                startEtsLogService();
                                mState = true;
                                mButton.setText(R.string.bt_stop_title);
                                mList.setVisibility(View.GONE);

                            } catch (EtsException e) {
                                Log.e("etslog_exception", e.getMessage());
                                mText.setText(e.getMessage());
                            }

                        } else if (mPath == null){
                            mText.setText(R.string.no_configfilesel);
                        } else if (!FileUtil.sdCardExist()) {
                            mText.setText(R.string.no_sdcard);
                        }
                    } else {
                        stopEtsLogService();
                        mState = false;
                        mButton.setText(R.string.bt_start_title);
                        mText.clearFocus();
                        mText.setText(R.string.tip_text);
                        mList.setVisibility(View.VISIBLE);
                    }
                }
            }
       });
       setListContent();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.e("onReceive", intent+"");
        if (intent.getAction().equals(UPDATE_INFO_ACTION)) {
            Log.e("onReceive", "Action ==LOG_INFO_UPDATE");
            Bundle data = new Bundle();
            data.putCharSequence("LogInfo", intent.getStringExtra("LogInfo"));
            Message msg = Message.obtain(mHandler, LOG_UPDATE);
            msg.setData(data);
            mHandler.sendMessageDelayed(msg, 1000);
        }
      }
    };
    
    private Handler mHandler = new Handler() {
         @Override
         public void handleMessage (Message msg) {
            super.handleMessage(msg);
             switch (msg.what) {
                 case LOG_UPDATE:                  
                    if (mState) {
                        mText.append(msg.getData().getCharSequence("LogInfo"));
                    }                     
                    break;                 
                 default:
                     break;
            }
         }
    };

    private void startEtsLogService() {
        Intent intent = new Intent();
        intent.setClass(this, CpLogService.class);  
        intent.putExtra("PATH", mPath);
        startService(intent);
    }

    private void stopEtsLogService() {
        Intent intent = new Intent();
        intent.setClass(this, CpLogService.class);                        
        stopService(intent);

    }

    private void setListContent () {
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, fileNames));
        final ListView listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); 
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("CpLogActivity", "onDestroy");
        unregisterReceiver(mReceiver);
    }

    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {   
        //the file path need to pass
        mPath = filePaths.get(position);
    }

}
