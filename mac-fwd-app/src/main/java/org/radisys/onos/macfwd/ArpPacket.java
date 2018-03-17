package org.radisys.onos.macfwd;

public class ArpPacket {
    private String srcIpAddress;
    private String srcMacAddress;
    private String dstIpAddress;
    private String dstMacAddress;

    public String getSrcIpAddress() {
        return srcIpAddress;
    }

    public void setSrcIpAddress(String srcIpAddress) {
        this.srcIpAddress = srcIpAddress;
    }

    public String getSrcMacAddress() {
        return srcMacAddress;
    }

    public void setSrcMacAddress(String srcMacAddress) {
        this.srcMacAddress = srcMacAddress;
    }

    public String getDstIpAddress() {
        return dstIpAddress;
    }

    public void setDstIpAddress(String dstIpAddress) {
        this.dstIpAddress = dstIpAddress;
    }

    public String getDstMacAddress() {
        return dstMacAddress;
    }

    public void setDstMacAddress(String dstMacAddress) {
        this.dstMacAddress = dstMacAddress;
    }

    @Override
    public String toString() {
        return "ArpPacket{" +
                "srcIpAddress='" + srcIpAddress + '\'' +
                ", srcMacAddress='" + srcMacAddress + '\'' +
                ", dstIpAddress='" + dstIpAddress + '\'' +
                ", dstMacAddress='" + dstMacAddress + '\'' +
                '}';
    }

    public ArpPacket(String srcIpAddress, String srcMacAddress, String dstIpAddress) {
        this.srcIpAddress = srcIpAddress;
        this.srcMacAddress = srcMacAddress;
        this.dstIpAddress = dstIpAddress;
    }

    public ArpPacket(String srcIpAddress, String srcMacAddress, String dstIpAddress, String dstMacAddress) {
        this.srcIpAddress = srcIpAddress;
        this.srcMacAddress = srcMacAddress;
        this.dstIpAddress = dstIpAddress;
        this.dstMacAddress = dstMacAddress;
    }
}
