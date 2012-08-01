package com.viatelecom.saber.ets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class EtsUtil {
    
    /*
    private static short checkSum2(byte[] data){

        Checksum checksumEngine = new Adler32();
        checksumEngine.update(data, 0, data.length);
        long checksum = checksumEngine.getValue(); // ?? 
        if(checksum>=0){
            checksum-=0x10;
        } else {
            checksum+=0x10;
        }
        
        return (short)(checksum&0x0000FFFF);
    }
    */

    public static short checkSum(byte[] data){
        long checksum = 0;
        for(byte d:data){
            short element = 0;
            element |= 0x00FF&d;
            
            checksum += element;
        }
        
        return (short)(checksum&0xFFFF);
    }
    
    public static int checkSum2(byte[] data){
        long checksum = 0;
        int length = data.length;
        
        byte[] temp = new byte[2];
        for(int index=0;index<length-1;index+=2){
            temp[0] = data[index];
            temp[1] = data[index+1];
            

            int val = bytes2short(temp)&0x0000FFFF;
            checksum += val;
        }
        
        return (int)(checksum&0xFFFFFFFF);
    }

    public static int checkSum(File f){
        long checksum = 0;
        try {
            FileInputStream fileImg = new FileInputStream(f);
    
            do{
                byte[] block = new byte[2];
                int size = fileImg.read(block);
                
                if(size!=2){
                    break; 
                }    
                
                int val = bytes2short(block)&0x0000FFFF;
                checksum += val;
                
            }while(true);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        
        return (int)(checksum&0xFFFFFFFF);
    }

    public static byte[] short2bytes(short val) {
        byte[] data = new byte[2];
        
        data[0] = (byte)(val&0xFF);
        data[1] = (byte)(val>>8&0xFF);
        
        return data;
    }

    public static short bytes2short(byte[] data){
        short val = 0;
        
        val |= data[0]&0x00FF;
        val |= data[1]<<8;
        
        return val;
    }
    
    public static byte[] int2bytes(int val) {
        byte[] data = new byte[4];
        
        data[0] = (byte)(val&0xFF);
        data[1] = (byte)(val>>8&0xFF);
        data[2] = (byte)(val>>16&0xFF);
        data[3] = (byte)(val>>24&0xFF);
        
        return data;
    }
    
    public static int bytes2int(byte[] data){
        int val = 0;
        
        val |= data[0]&0x000000FF;
        val |= (data[1]<<8)&0x0000FF00;
        val |= (data[2]<<16)&0x00FF0000;
        val |= (data[3]<<24)&0xFF000000;
        
        return val;
    }
    
    public static byte[] long2bytes(long val) {
        byte[] data = new byte[8];
        
        data[0] = (byte)(val&0xFF);
        data[1] = (byte)(val>>8&0xFF);
        data[2] = (byte)(val>>16&0xFF);
        data[3] = (byte)(val>>24&0xFF);
        data[4] = (byte)(val>>32&0xFF);
        data[5] = (byte)(val>>40&0xFF);
        data[6] = (byte)(val>>48&0xFF);
        data[7] = (byte)(val>>56&0xFF);
        
        return data;
    }
    
    public static byte[] doubleToByte(double d){
        byte[] bytes = new byte[8];
        
        long l = Double.doubleToLongBits(d);
        for(int i = 0; i < bytes.length; i++ ){
            bytes[i]= new Long(l).byteValue();
            l=l>>8;
        }
        return bytes;
     }
}
