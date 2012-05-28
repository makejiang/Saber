package com.viatelecom.saber.ets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

import com.viatelecom.saber.Application;

public class EtsDnlder extends EtsDevice{

    public enum CBPMode{
        Boot,
        CP,
        Unknown
    }
    
    /**
     * download status
     * @author kma
     *
     */
    public enum DnldStatus{
        Readying,        // read for beginning
        WaitingBoot,    // waiting for the cbp to boot state
        Erasing,        // erasing the flash
        Downloading,    // writing the flash
        Finishied,        // download finished
        Error            // error
    }
    
    /**
     * callback for downloading image
     * @author kma
     *
     */
    public interface EtsDnlderCallback{
        void onProcess(DnldStatus status, int progress, String info);
    }
    
    private EtsDnlderCallback _callback = null;

    public EtsDnlderCallback getCallback() {
        return _callback;
    }

    private EtsMsgQueue _msgCache = new EtsMsgQueue();

    private EtsDnlderThread _dnlderThr;
    
    /**
     * the constructor
     * @param app
     * @param callback
     */
    public EtsDnlder(Application app, EtsDnlderCallback callback) {
        super(app);
        
        // set the callback
        if (callback!=null){
            _callback = callback;        
        } else {
            _callback = new EtsDnlderCallback(){
                public void onProcess(DnldStatus status, int progress, String info) {
                    if (status == DnldStatus.Error){
                        Log.e(Application.TagApp, info);
                    } else if (status == DnldStatus.Downloading) {
                        Log.i(Application.TagApp, info + " progress:"+progress);
                    } else {
                        Log.i(Application.TagApp, info);
                    }
                }
            };
        }
    }

    private List<String> _imgFiles;
    static public String[] sectionName = {
        "BOOT",
        "CP"
    };

    public List<String> getImgFiles() {
        return _imgFiles;
    }
    
    public byte getFlashSectionIndex(String imgFile){
        byte flashSectionIndex = -1;
        String pathname_file = imgFile.toLowerCase();
        
        if (pathname_file.indexOf("boot")>0){
            flashSectionIndex=0;
        } else if (pathname_file.indexOf("cp")>0){
            flashSectionIndex=1;
        } else {
            Log.e(Application.TagApp, "unknown section for image:\""+imgFile+"\"");
        }

        return flashSectionIndex;
    }
    
    public void close() {
        this.destroy();
    }
    
    private boolean setResetMode(String mode){
        
        String resetMode = SystemProperties.get("persist.cp.reset.mode");
        Log.v(Application.TagApp, "current \"don't reset modem\" = "+ resetMode);
        
        if (resetMode.equals(mode)) {
            return true;
        }
        
        Log.v(Application.TagApp, "set the \"don't reset modem\" = " + mode);
        SystemProperties.set("persist.cp.reset.mode", mode);
        
        resetMode = SystemProperties.get("persist.cp.reset.mode");
        Log.v(Application.TagApp, "\"don't reset modem\" = "+ resetMode + " after set.");
        
        return resetMode.equals(mode);
    }
    
    /**
     * for test
     */
    public boolean test() {
        if (!create()) {
            Log.e("test","open the ets device failed");
            return false;
        }
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        
        /*
        List<String> imgPaths = new ArrayList<String>();
        imgPaths.add(android.os.Environment.getExternalStorageDirectory()+"/cbp/img/boot.rom");
        imgPaths.add(android.os.Environment.getExternalStorageDirectory()+"/cbp/img/cp.rom");
        
        try {
            start(imgPaths);
        } catch (EtsException e) {
            Log.e(Application.TagApp, e.getMessage());
        }
        */
        
        Log.i("test","do loopback");
        int retry = 3;
        while (retry>0) {
        if(loopback()){
                break;
            } else {
                --retry;
            }
        }
        
        if(retry==0){
            close();
            return false;
        } 
                
        Log.i("test","do jump to boot mode");
        if(!jump2load(CBPMode.Boot, true))
        {
            close();
            return false;
        }
        
        Log.i("test","do erase the cp section");
        if(!eraseFlash((byte)1)) {
            close();
            return false;
        }
        
        Log.i("test", "test end with successful");
        close();
        return true;
    }

    public boolean testJump2Boot() {
        if (!create()) {
            Log.e("test","open the ets device failed");
            return false;
        }
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        
        Log.i("test","do loopback");
        int retry = 3;
        while (retry>0) {
        if(loopback()){
                break;
            } else {
                --retry;
            }
        }
        
        if(retry==0){
            close();
            return false;
        } 
                
        Log.i("test","do jump to boot mode");
        if(!jump2load(CBPMode.Boot, true))
        {
            close();
            return false;
        }
        
        Log.i("test", "test end with successful");
        close();
        return true;
    }

    /**
     * start download a image to cbp
     * @param imgPath
     */
    public void start(List<String> imgPaths) throws EtsException {
    
        // check the filename and get section
        _callback.onProcess(DnldStatus.Readying, 0, "Checking the image file");
        if (imgPaths==null || imgPaths.isEmpty()){
            throw new EtsException("no image filenames");
        }
        _imgFiles = imgPaths;
        
        // set the reset mode
        //_callback.onProcess(DnldStatus.Readying, 0, "set the \"don't reset modem\" to 1");
        //if (!setResetMode("1")){
        //    throw new EtsException("set the \"don't reset modem\" to 1 failed");
        //}
    
        // create the ets port device
        _callback.onProcess(DnldStatus.Readying, 0, "Openning the ets device");
        if (!create()) {
            throw new EtsException("Open the ets device failed");
        }
        StartRead();
        
        // create the thread and start it
        _dnlderThr = new EtsDnlderThread(this);
        _dnlderThr.start();
    }

    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        // get msgs from buffer
        List<EtsMsg> msgs = EtsMsg.parse(buffer, size); 
        for(EtsMsg msg:msgs) {
            _msgCache.offer(msg);
        }        
    }
    
    private boolean cmpBytes(byte[] data1, byte[] data2){
        if (data1.length!=data2.length){
            return false;
        }
        
        for(int i=0;i<data1.length;++i) {
            if(data1[i]!=data2[i]){
                return false;
            }
        }
        
        return true;
    }
    
    private EtsMsg sendAndWait(EtsMsg msgReq, short id, long timeout) {
        write(msgReq);
        return _msgCache.waitForMsg(id, timeout);
    }

    /**
     * feature loopback
     * @return
     */
    public boolean loopback(){
        Log.v(Application.TagApp, "do loopback");
        
        short id = 0x0000;
        byte[] data = new byte[]{(byte)(System.currentTimeMillis()&0xFF)}; 
        
        EtsMsg msg = sendAndWait(new EtsMsg(id, data), id, 2000);
        if(msg==null){
            Log.e(Application.TagApp, "loopback failed");
            return false;
        }
        
        boolean ret = cmpBytes(msg.getData(), data);
        if(ret){
            Log.i(Application.TagApp, "loopback success");
        } else {
            Log.e(Application.TagApp, "loopback failed");
        }
        return ret;
    }
    
    
    /**
     * boot or cp or error
     * @return 0:no response, 1:boot, 2:cp 
     */
    public CBPMode checkMode(){
        Log.v(Application.TagApp, "do check mode");
        
        short id = 0x00C8;
        EtsMsg msg = sendAndWait(new EtsMsg(id, null), (short) 0xFFFF, 2000);
        
        CBPMode ret = CBPMode.Unknown;
        if(msg!=null){
            if (msg.getId()==id){
                ret = CBPMode.CP;
            } else {
                ret = CBPMode.Boot;
            }
        } 
        
        Log.v(Application.TagApp, ret.name() + " mode");
        return ret; // boot
    }
    
    /**
     * jump to loader, into boot state
     * @return
     */
    public boolean jump2load(CBPMode toMode, boolean openAgain) {
        Log.v(Application.TagApp, "do jump to loader to " + toMode.name());
        
        CBPMode curMode = checkMode();
        if (curMode == CBPMode.Unknown) {
            Log.e(Application.TagApp, "no response from device?");
            return false;
        }
        
        if (curMode == toMode) {
            return true;
        }
        
        short id = 0x00DC; // jump to loader
        write(new EtsMsg(id, null));
        destroy();
        
        if(openAgain) {
            Log.v(Application.TagApp, "wait to open device in " + toMode + " mode");
            try {
                Thread.sleep(toMode==CBPMode.Boot?mCfg.getTimeoutCp2Boot():mCfg.getTimeoutBoot2Cp());
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            
            if (!create()) {
                Log.e(Application.TagApp, "create port failed");
                return false;
            }
            StartRead();
            
            if (toMode == CBPMode.Boot){
                id = 0x00E0; // boot to loader
                EtsMsg msg = _msgCache.waitForMsg(id, 5*1000); 
                if (msg==null){
                    Log.e(Application.TagApp, "check boot2load msg failed, try to check the mode");
                    if (checkMode() == CBPMode.Boot){
                        return true;
                    }
                    Log.e(Application.TagApp, "the cbp is not in boot mode");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * erase the flash section
     * @param section
     * @return
     */
    public boolean eraseFlash(byte flashSection) {
        short id = 0x04B1;
        EtsMsg msg = sendAndWait(new EtsMsg(id, new byte[]{flashSection}), id, flashSection==0?mCfg.getTimoutEraseBoot():mCfg.getTimoutEraseCp());
        if (msg==null){
            Log.e(Application.TagApp, "erase flash time out");
            return false;
        }
        Log.i(Application.TagApp, "erase flash success");
        return true;
    }
    
    /**
     * download file into flash
     * @return
     */
    public boolean downloadFlash(byte flashSection, String imgPath) {
        boolean ret = true;
        
        try {
            File f = new File(imgPath);
            byte[] dataImg = new byte[(int)f.length()];
            
            FileInputStream fileImg = new FileInputStream(f);
            int sizeImg = fileImg.read(dataImg);
            if(sizeImg!=dataImg.length){
                _callback.onProcess(DnldStatus.Error, 0, "Read image file failed");
                return false;
            }
            
            fileImg.close();
            
            // get length and checksum of the image file
            _callback.onProcess(DnldStatus.Readying, 0, "Computing the checksum of image");
            byte[] imgLength = EtsUtil.int2bytes(sizeImg);
            byte[] imgChecksum = EtsUtil.int2bytes(EtsUtil.checkSum2(dataImg));
            
            byte[] imgInfo = new byte[8];
            int index = 0;
            for(byte b:imgLength){
                imgInfo[index++] = b;
            }
            for(byte b:imgChecksum){
                imgInfo[index++] = b;
            }
            
            // begin the process of download
            int seqToWrite = 0;
            
            // init flash
            _callback.onProcess(DnldStatus.Erasing, 0, "Erasing flash, section="+EtsDnlder.sectionName[flashSection]);
            EtsMsg msg = sendAndWait(new EtsMsgProgCmd(++seqToWrite, EtsMsgProgCmd.ERASE, flashSection, imgInfo), 
                                                       EtsMsgProgCmd.ID,  flashSection==0?mCfg.getTimoutEraseBoot():mCfg.getTimoutEraseCp());
            if (msg==null){
                Log.e(Application.TagApp, "erase flash time out");
                return false;
            }
            Log.i(Application.TagApp, "erase flash success");
            
            // write flash
            _callback.onProcess(DnldStatus.Downloading, 0, "Downloading flash, section="+EtsDnlder.sectionName[flashSection]);
            
            int sizeBlock = mCfg.getBytesBlock();
            byte[] block = new byte[sizeBlock];
            
            int numBlocks = (int) (sizeImg/sizeBlock+2); //+1 for program write sequence begin with 2
            int seqAck = 0;
            
            index = 0;
            do{
                
                int size = sizeImg-index>sizeBlock?sizeBlock:sizeImg-index;
                
                if (size>0){
                    // get data
                    byte[] data = block;
                    if (size<sizeBlock){
                        data = new byte[size];
                    }
                    
                    for (int i=0;i<size;++i){
                        data[i] = dataImg[index++];
                    }
                    
                    write(new EtsMsgProgCmd(++seqToWrite, EtsMsgProgCmd.WRITE, flashSection, data));
                }
                
                if (seqToWrite-seqAck>mCfg.getWindows()||size<=0) { // window size is 3
                     msg = _msgCache.waitForMsg(EtsMsgProgCmd.ID, 2000);
                     if (msg==null){
                        Log.e(Application.TagApp, "don't get response");
                        ret = false;
                        break;
                     }
                     
                     seqAck = msg.getProgRspSequence();                     
                     if (msg.getProgRspAck()==0x01) {
                         Log.e(Application.TagApp, "get NAK!");
                         ret = false;
                         break;
                     }
                }
                
                //Log.v(Application.TagApp, "current cmd seq:"+ seqToWrite +",ack seq:"+seqAck+",total blocks:"+numBlocks+", this block size:"+size);
                _callback.onProcess(DnldStatus.Downloading, (seqAck*100)/numBlocks, "Downloading flash " + sectionName[flashSection]);

            }while (seqToWrite>seqAck);
            
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        return ret;
    }
}
