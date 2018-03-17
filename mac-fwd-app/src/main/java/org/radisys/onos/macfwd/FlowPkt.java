package org.radisys.onos.macfwd;

public class FlowPkt {

    private long inPort;
    private  long outPort;
    private String srcMac;
    private String dstMac;
    private String device;

    public FlowPkt() {
        this.inPort = inPort;
        this.outPort = outPort;
        this.srcMac = srcMac;
        this.dstMac = dstMac;
        this.device = device;
    }

    public FlowPkt(long inPort, long outPort, String srcMac, String dstMac, String device) {
        this.inPort = inPort;
        this.outPort = outPort;
        this.srcMac = srcMac;
        this.dstMac = dstMac;
        this.device = device;
    }

    public long getInPort() {
        return inPort;
    }

    public void setInPort(long inPort) {
        this.inPort = inPort;
    }

    public long getOutPort() {
        return outPort;
    }

    public void setOutPort(long outPort) {
        this.outPort = outPort;
    }

    public String getSrcMac() {
        return srcMac;
    }

    public void setSrcMac(String srcMac) {
        this.srcMac = srcMac;
    }

    public String getDstMac() {
        return dstMac;
    }

    public void setDstMac(String dstMac) {
        this.dstMac = dstMac;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "FlowPkt{" +
                "inPort=" + inPort +
                ", outPort=" + outPort +
                ", srcMac='" + srcMac + '\'' +
                ", dstMac='" + dstMac + '\'' +
                ", device='" + device + '\'' +
                '}';
    }
}
