package com.viatelecom.saber.ets.cfg;

public class CfgBase {

    protected int timeoutCp2Boot = 5000;
    protected int timeoutBoot2Cp = 5000;
    
    protected int timoutEraseBoot = 30*1000;
    protected int timoutEraseCp = 200*1000;
    
    protected int bytesBlock = 260;
    protected int windows = 3;
    
    protected String externalsd = "";
    
    public String getExternalsd() {
        return externalsd;
    }
    public int getTimeoutCp2Boot() {
        return timeoutCp2Boot;
    }
    public int getTimeoutBoot2Cp() {
        return timeoutBoot2Cp;
    }
    public int getTimoutEraseBoot() {
        return timoutEraseBoot;
    }
    public int getTimoutEraseCp() {
        return timoutEraseCp;
    }
    public int getBytesBlock() {
        return bytesBlock;
    }
    public int getWindows() {
        return windows;
    }
}
