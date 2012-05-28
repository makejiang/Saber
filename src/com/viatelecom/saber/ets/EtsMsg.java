package com.viatelecom.saber.ets;

import java.util.ArrayList;
import java.util.List;

import com.viatelecom.saber.Application;

import android.util.Log;

public class EtsMsg {

    public static int size_last = 0;
    
    private short _id;
    protected byte[] _data = null;
    
    
    public short getId() {
        return _id;
    }

    public byte[] getData() {
        return _data;
    }
    
    public byte[] getLogEntry() {
        byte[] buf = new byte[_data.length+14];
        
        // log tickcount and txrx
        long time_now = System.currentTimeMillis();
        byte[] time = EtsUtil.long2bytes(time_now);
        System.arraycopy(time, 0, buf, 0, 8);
        
        // rx
        buf[8] = 0x00;
        buf[9] = 0x00;
        
        // length
        byte[] tmp = EtsUtil.short2bytes(getLength());
        System.arraycopy(tmp, 0, buf, 10, 2);
        
        // id
        tmp = EtsUtil.short2bytes(_id);
        System.arraycopy(tmp, 0, buf, 12, 2);
        
        // data
        System.arraycopy(_data, 0, buf, 14, _data.length);
        
        return buf;
    }

    public byte[] getBuf() {
        if (_data==null){
            return null;
        }
        
        byte[] buf = new byte[_data.length+8];
        
        // header
        buf[0] = (byte)0xFE;
        buf[1] = (byte)0xDC;
        buf[2] = (byte)0xBA;
        buf[3] = (byte)0x98;
        
        // length 
        short length = (short)(_data.length+2);
        buf[4] = (byte)(length&0xFF);
        buf[5] = (byte)(length>>8&0xFF);
        
        // id
        buf[6] = (byte)(_id&0xFF);
        buf[7] = (byte)(_id>>8&0xFF);
        
        // data
        System.arraycopy(_data, 0, buf, 8, _data.length);
        
        return buf;
    }
    
    public short getLength() {
        return (short)(_data.length+2);
    }
    
    public int getProgRspSequence(){
        if (_id!=1202) {
            return -1;
        }
        
        return EtsUtil.bytes2int(_data);
    }
    
    public byte getProgRspAck(){
        if (_id!=1202) {
            return -1;
        }
        
        return _data[4];
    }
    
    public EtsMsg(short id, byte[] data){
        _id = id;
        
        if(data!=null){
            _data = new byte[data.length];
            System.arraycopy(data, 0, _data, 0, data.length);
            
        } else {
            _data = new byte[0];
        }
        
    }
    

    
    /**
     * 
     * @param buf: raw bytes received from ETS device
     * @return
     */
    public static List<EtsMsg> parse(byte buf[], int size){
        int index = 0;
        List<EtsMsg> msgs = new ArrayList<EtsMsg>();
        
        size_last = size;
        while(size_last>=8) {
            // find the begin
            Boolean findHeader = false;
            for (;index<=size-8;++index) {
                if( buf[index] == (byte)0xFE &&
                    buf[index+1] == (byte)0xDC && 
                    buf[index+2] == (byte)0xBA &&
                    buf[index+3] == (byte)0x98){
                    
                    findHeader = true;
                    break;
                }
            }
            
            if (!findHeader){
                Log.i(Application.TagApp, "can't find the header(index="+index+")");
                size_last = 0;
                break;
            }
            size_last = size-index; // the begin of this message
            index += 4;
        
            // length
            byte[] temp = new byte[2];
            temp[0] = buf[index++];
            temp[1] = buf[index++];
            
            short length = EtsUtil.bytes2short(temp);
            if(length<2) {
                Log.e(Application.TagApp, "invlid length(length="+length+")");
                size_last = size-index; 
                continue;
            }
            
            if(size<index+length) {
                Log.i(Application.TagApp, "not enough data for this message(length="+length+", last buf size="+(size-index));
                break;
            }

            // id
            temp[0] = buf[index++];
            temp[1] = buf[index++];
            short id = EtsUtil.bytes2short(temp);

            // data
            temp = new byte[length-2];
            System.arraycopy(buf, index, temp, 0, length-2);
            index += length-2;
            
            // new msg
            msgs.add(new EtsMsg(id, temp));
            
            // refresh the size_last
            size_last = size-index;
        }

        // msg
        Log.v(Application.TagApp, "parsed " + msgs.size() + " msgs");
        return msgs; 
    }
    
    /**
     * 
     * @param val: like "14:52:19.8[ Raw Tx: Len=5, 0x65 0x00 0x00 0x00 0x00"
     * @return
     */
    public static EtsMsg parse(String line){
        
        int index = line.indexOf("Raw Tx"); // or Rx
        if(index<0) {
            return null;
        }
        
        index = line.indexOf(',', index);
        if(index<0) {
            return null;
        }
        index += 2;
        
        String dataLine = line.substring(index);
        byte[] msgBin = hexStr2bytes(dataLine);
        
        // id
        short id = EtsUtil.bytes2short(msgBin);
        
        // data
        byte[] data = new byte[msgBin.length-2];
        for(int i=0;i<msgBin.length-2;i++) {
            data[i] = msgBin[i+2];
        }
        
        return new EtsMsg(id, data);
    }
    
    /**
     * 
     * @param data: like 0x65 0x00 0x00 0x00 0x00
     * @return
     */
    private static byte[] hexStr2bytes(String src) {
        String[] datas = src.split(" ");
        
        byte[] ret = new byte[datas.length];
        for (int i=0;i<datas.length;++i){
            ret[i] = Integer.decode(datas[i]).byteValue();
        }
        
        return ret;
    }
    
    
}
