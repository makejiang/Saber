/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.viatelecom.saber;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android.os.SystemProperties;
import android.util.Log;

import com.viatelecom.saber.serialport.SerialPort;
import com.viatelecom.saber.serialport.SerialPortFinder;

public class Application extends android.app.Application {
   
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("Application","Created");
    }

    @Override
    public void onTerminate() {
        Log.v("Application","Terminated");
        super.onTerminate();
    }

    //public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    
    private String getEtsDevPath(){
        String dev_path = "/dev/ttyUSB"+SystemProperties.get("cbp.ets","1");
        return dev_path;
    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
            
            String path = getEtsDevPath();
            Log.i("Application", "Device:"+path);
            int baudrate = 115200;

            /* Check parameters */
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
    
    
}
