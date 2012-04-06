package com.viatelecom.saber.ets;

import java.util.zip.Adler32;
import java.util.zip.Checksum;

import android.util.Log;

public class EtsMsgProgCmd extends EtsMsg {
    public static short ID = 1202;
    
    public static byte ERASE = 0;
    public static byte WRITE = 1;
    
    private int _sequence;
    private byte _type;
    private byte _section;
        
    /**
     * for command
     * @param sequence
     * @param type
     * @param section
     * @param data
     */
    public EtsMsgProgCmd(int sequence, byte type, byte section, byte[] data) {
        super(ID, new byte[(data==null?0:data.length)+10]);
        
        _sequence = sequence;
        byte[] temp = EtsUtil.int2bytes(sequence);
        System.arraycopy(temp, 0, _data, 0, 4);
        
        _data[4] = _type = type;
        _data[5] = _section = section;
        
        if(data!=null){
            // checksum
            short checksum = EtsUtil.checkSum(data);
            temp = EtsUtil.short2bytes(checksum);
            System.arraycopy(temp, 0, _data, 6, 2);
            
            // size
            temp = EtsUtil.short2bytes((short)(data.length&0xFFFF));
            System.arraycopy(temp, 0, _data, 8, 2);
            
            // data
            System.arraycopy(data, 0, _data, 10, data.length);
            
            
        } else {
            _data[6] = 0x00;
            _data[7] = 0x00;
            _data[8] = 0x00;
            _data[9] = 0x00;
        }
        
    }
    
    
    /**
     * for response
     * @param sequence
     * @param ack
     */
    /*
    public EtsMsgFlashProgramCmd(int sequence, byte ack) {
        super(ID, new byte[5]);
        
        byte[] temp = EtsMsg.int2bytes(sequence);
        for(int i=0;i<4;++i) {
            _data[i] = temp[i];
        }
        
        _data[4] = ack;
    }
    */
    
    /**
     * parse response from ets device
     * @param buf
     * @param size
     * @return
     */
    
    /*
    public static List<EtsMsg> parse(byte buf[], int size){
        int index = 0;
        List<EtsMsg> msgs = new ArrayList<EtsMsg>();
        
        int oldSize = size;
        while(size>=13) {
            // find the begin
            Boolean findHeader = false;
            for (;index<=oldSize-13;++index) {
                if( buf[index] == (byte)0xFE &&
                    buf[index+1] == (byte)0xDC && 
                    buf[index+2] == (byte)0xBA &&
                    buf[index+3] == (byte)0x98){
                    findHeader = true;
                    break;
                }
            }
            
            if (!findHeader){
                Log.w("EtsMsgFlashProgram", "can't find the header(index="+index+")");
                break;
            }            
            index += 4;
        
            // length
            byte[] temp = new byte[2];
            temp[0] = buf[index++];
            temp[1] = buf[index++];
            
            short length = bytes2short(temp);
            if(oldSize<index+length) {
                Log.w("EtsMsgFlashProgram", "invalid data length(length="+length+", (index="+index+")");
                break;
            }
            
            if(length!=7){
                Log.i("EtsMsgFlashProgram", "the length is not 7");
                continue;
            }            

            // id
            temp[0] = buf[index++];
            temp[1] = buf[index++];
            
            short id = bytes2short(temp);
            if(id!=EtsMsgFlashProgram.ID) {
                Log.i("EtsMsgFlashProgram", "the id is not a flash program response");
                continue;
            }
            
            // sequence
            temp = new byte[4];
            temp[0] = buf[index++];
            temp[1] = buf[index++];
            temp[2] = buf[index++];
            temp[3] = buf[index++];
            
            int sequence = bytes2int(temp);
            
            // ack
            byte ack = buf[index++];

            // new msg
            msgs.add(new EtsMsgFlashProgram(sequence, ack));
            size = oldSize - index;
        }

        // msg
        return msgs; 
    }
    */
}
