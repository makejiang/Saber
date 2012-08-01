package com.viatelecom.saber;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.viatelecom.saber.R;
import com.viatelecom.saber.ets.EtsDnlder;

public class VIASaberActivity extends Activity {

    private Button mBtDownload = null;
    private Button mBtCpLog = null;
    private TextView mText = null;     

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mText = (TextView) findViewById(R.id.tiptext);
        
        
        mBtDownload = (Button) findViewById(R.id.bt_download);
        mBtDownload.setOnClickListener(new android.view.View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (v == mBtDownload) {
                    if (!FileUtil.sdCardExist()) {
                        mText.setText(R.string.no_sdcard);
                        return;
                    }
                    LaunchDownloadActivity();
                }
            }

        });
        
        Application app = (Application)getApplication(); 
        if(app.IsFlashLess())
        {
            Log.i(Application.TagApp, "It is a flash less chip");
            mBtDownload.setVisibility(View.GONE);
        }
        
        
        mBtCpLog =  (Button) findViewById(R.id.bt_cplog);
        mBtCpLog.setOnClickListener(new android.view.View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (v == mBtCpLog) {
                    if (!FileUtil.sdCardExist()) {
                        mText.setText(R.string.no_sdcard);
                        return;
                    }
                    LaunchCpLogActivity();
                }
            }
        });
        
        
       
    }
    
    private void LaunchDownloadActivity () {    
        Intent intent = new Intent(this, DownloadActivity.class);
        FileUtil files = new FileUtil(FileUtil.getImgDir());
        ArrayList<String> fileNames = files.getFileNamesList();
        ArrayList<String> filePaths = files.getFilePathsList();
        if (fileNames.isEmpty()|| filePaths.isEmpty()) {
            mText.setText(R.string.no_imgfile);
            return;
        }
        intent.putStringArrayListExtra("com.viatelecom.saber.names", fileNames);
        intent.putStringArrayListExtra("com.viatelecom.saber.paths", filePaths);

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    
    private void LaunchCpLogActivity () {    
        Intent intent = new Intent(this, CpLogActivity.class);
        FileUtil files = new FileUtil(FileUtil.getCfgDir());
        ArrayList<String> fileNames = files.getFileNamesList();
        ArrayList<String> filePaths = files.getFilePathsList();
        if (fileNames.isEmpty() || filePaths.isEmpty()) {
            mText.setText(R.string.no_configfile);
            return;
        }
        intent.putStringArrayListExtra("com.viatelecom.saber.names", fileNames);
        intent.putStringArrayListExtra("com.viatelecom.saber.paths", filePaths);

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}