package com.viatelecom.saber.ets.cfg;

public class CfgLenovoK1 extends CfgBase {

    public CfgLenovoK1() {
        timeoutCp2Boot = 8000;
        timeoutBoot2Cp = 6000;
        
        timoutEraseBoot = 30*1000;
        timoutEraseCp = 200*1000;
        
        bytesBlock = 260;
        windows = 3;
        
        externalsd = "";
    }

}
