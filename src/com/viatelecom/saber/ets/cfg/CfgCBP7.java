package com.viatelecom.saber.ets.cfg;

public class CfgCBP7 extends CfgBase {

    public CfgCBP7() {
        timeoutCp2Boot = 8000;
        timeoutBoot2Cp = 6000;
        
        timoutEraseBoot = 30*1000;
        timoutEraseCp = 500*1000;
        
        bytesBlock = 260;
        windows = 3;
        
        externalsd = "";
    }

}
