package com.viatelecom.saber.ets;

import android.util.Log;

import com.viatelecom.saber.Application;
import com.viatelecom.saber.ets.EtsDnlder.CBPMode;
import com.viatelecom.saber.ets.EtsDnlder.DnldStatus;
import com.viatelecom.saber.ets.EtsDnlder.EtsDnlderCallback;

public class EtsDnlderThread extends Thread {

    private EtsDnlder _dnlder;
    
    public EtsDnlderThread(EtsDnlder dnlder) {
        _dnlder = dnlder;
    }
    
    private void close() {
        _dnlder.close();
        
    }

    @Override
    public void run() {
        Log.i(Application.TagApp, "Donwloader Thread started");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        
        // loopback test
        int retry = 3;
        while (retry>0) {
            if(_dnlder.loopback()){
                break;
            } else {
                --retry;
            }
        }
        
        if(retry==0){
            _dnlder.getCallback().onProcess(DnldStatus.Error, 0, "Do loopback failed");
            close();
            return;
        } 
        _dnlder.getCallback().onProcess(DnldStatus.Readying, 0, "Do loopback success");
        
        // check the mode of cbp
        
        // to boot
        if(!_dnlder.jump2load(CBPMode.Boot, true))
        {
            _dnlder.getCallback().onProcess(DnldStatus.Error, 0, "Reset device to boot failed");
            close();
            return;
        }
        _dnlder.getCallback().onProcess(DnldStatus.WaitingBoot, 0, "Reset device to boot succuss");
        
        for (String imgPath:_dnlder.getImgFiles()){
            byte flashSection = _dnlder.getFlashSectionIndex(imgPath);
            if (flashSection<0 ||flashSection>=EtsDnlder.sectionName.length){
                continue;
            }
            
            if(!_dnlder.downloadFlash(flashSection, imgPath)){
                close();
                _dnlder.getCallback().onProcess(DnldStatus.Error, 0, "Download flash failed");
                return;
            }
    
        }
                
        _dnlder.jump2load(CBPMode.CP, false);
        _dnlder.getCallback().onProcess(DnldStatus.Finishied, 0, "Download flash finished");
    }

    
}
