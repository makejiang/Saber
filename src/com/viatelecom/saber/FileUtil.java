package com.viatelecom.saber;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;
import android.os.StatFs;
import java.util.ArrayList;   
import java.util.Collections;
import java.util.List;   

import org.apache.http.util.EncodingUtils;

import android.util.Log;

public class FileUtil {
    
    
    private File mDir = null;
    private String mPath = null;
    private ArrayList<String> mFileNames = null;//put all filenames   
    private ArrayList<String> mFilePaths = null;//put all file paths 
    
    public FileUtil (String path) {
        mDir = new File(path);
        mPath = path;
        
        Log.i(Application.TagApp, "file util:"+mPath);
        getFileAndPathList();        
    }
    
    public static boolean sdCardExist () {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d(Application.TagApp, "sdCardExist true");
            return true;
        } else {
            Log.d(Application.TagApp, "sdCardExist false");
            return false;
        }
    }
    
    private static String getCbpDir()
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/cbp/";
        //return Environment.getExternalStorageDirectory().getAbsolutePath() + "/external_sd/cbp/";
        //return "/mnt/ext_sdcard/cbp/";
    }
    
    public static String getCfgDir(){
        return getCbpDir() + "cfg/";
    }

    public static String getLogDir(){
        return getCbpDir() + "log/";
    }
    
    public static String getImgDir(){
        return getCbpDir() + "img/";
    }
    
    private static String streamRead(InputStream is) throws IOException {  
        int buffersize = is.available();        // 取得输入流的字节长度  
        byte buffer[] = new byte[buffersize];  
        is.read(buffer);                        // 将数据读入数组  
        is.close();                             // 读取完毕后要关闭流。  
        String result = EncodingUtils.getString(buffer, "UTF-8");   // 设置取得的数据编码，防止乱码  
        return result;  
    }

    public static String readFile(String path_file)throws IOException {
        FileInputStream inputStream = new FileInputStream(path_file);  
        String result = streamRead(inputStream);
        
        return result;
    }
    

    private boolean fileWithSuffix (String filename, String suffix) {
        if (filename == null) {
            Log.d(Application.TagApp, "fileWithSuffix filename is null!");
            return false;
        }    
        
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            String fileExtention = filename.substring(index + 1);
            if( fileExtention.equalsIgnoreCase(suffix)) {
                Log.d(Application.TagApp, "fileWithSuffix suffix equal!");
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the filename array and filepath array
     * .bcfg with CONFIG_PATH 
     * boot.img and cp.img with IMG_PATH
     * 
     * @return count of the proper files
     */    
    private void getFileAndPathList () {
        mFileNames = new ArrayList<String>();   
        mFilePaths = new ArrayList<String>();   
         
        File[] files = mDir.listFiles();
        if (files != null) {
            for(File file:files){
                if (mPath.equals(getCfgDir())) {
                    Log.d(Application.TagApp, "getFileAndPathList CONFIG PATH");
                    if (fileWithSuffix(file.getName(), "bcfg")) {
                        Log.d(Application.TagApp, "name:" + file.getName() + "path:" + file.getPath());
                        mFileNames.add(file.getName());
                        mFilePaths.add(file.getPath());                
                    }
                } else if (mPath.equals(getImgDir())) {
                    Log.d(Application.TagApp, "getFileAndPathList IMG PATH");
                    if(fileWithSuffix(file.getName(), "rom")) {
                        Log.d(Application.TagApp, "name:" + file.getName() + "path:" + file.getPath());
                        mFileNames.add(file.getName());
                        mFilePaths.add(file.getPath());
                    }
                }
            }
            Collections.sort(mFileNames);
            Collections.sort(mFilePaths);
        }         
    }
    
    public ArrayList<String> getFileNamesList () {
        return mFileNames;
    }

    public ArrayList<String> getFilePathsList () {
        return mFilePaths;
    }
}