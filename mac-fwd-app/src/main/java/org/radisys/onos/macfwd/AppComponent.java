/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.radisys.onos.macfwd;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.ARP;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPacket;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.*;
import org.onosproject.net.topology.PathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    private ApplicationId appId;

    ReactivePacketProcessor pktProcessor = new ReactivePacketProcessor();
    HostListeners hostListener = new HostListeners();
    HashMap<String, ArpPacket> hmArpHosts = new
            HashMap<>();


    @Activate
    protected void activate() {
        log.info("Started");
        appId = coreService.registerApplication(getClass().getPackage().getName());
        packetService.addProcessor(pktProcessor, PacketProcessor.director(2));
        log.info("prince ==>> packetService   implemented");

        Iterable<Device> devices = deviceService.getDevices();

        for (Device device : devices) {
            log.info("deviceid :  {}", device.id().toString());
        }

        requestIntercepts();
        hostService.addListener(hostListener);

//        TrafficSelector.Builder selectBuilder = DefaultTrafficSelector
//                .builder();
//        TrafficSelector selector = selectBuilder.

    }

    private void addFlow(){
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        //TrafficSelector selector = selectorBuilder.
    }

    @Deactivate
    protected void deactivate() {
        withdrawIntercepts();
        log.info("Stopped");
    }

    private void requestIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
        selector.matchEthType(Ethernet.TYPE_ARP);
        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
        selector.matchEthType(Ethernet.TYPE_IPV6);
    }

    private void withdrawIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        selector.matchEthType(Ethernet.TYPE_ARP);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        selector.matchEthType(Ethernet.TYPE_IPV6);
    }


    class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext packetContext) {

            if (packetContext.isHandled()) {
                return;
            }

            InboundPacket pkt = packetContext.inPacket();

            Ethernet ethPkt = pkt.parsed();

            if (ethPkt == null) {
                return;
            }

            // If PacketOutOnly or ARP packet than forward directly to output port
            if (ethPkt.getEtherType() == Ethernet.TYPE_ARP) {
                log.info("Prince received an arp packet =====> : {}",ethPkt
                        .toString());
                IPacket ipPkt = ethPkt.getPayload();
                ARP arp = (ARP) ipPkt;
                log.info("prince trying to find the destination ip.  ===>>>>>" +
                        " src-ip : {} , src mac address : {} , dst ip : {} ",
                        arp.getSenderProtocolAddress(),arp
                                .getSenderHardwareAddress(), arp.getTargetProtocolAddress());
                String srcIp = Arrays.toString(arp
                        .getSenderProtocolAddress()).replace("[","")
                        .replace("]","")
                        .replace(",", ".").replace(" ", "");

                ArpPacket arpPacket = new ArpPacket(srcIp , ethPkt
                        .getSourceMAC().toString(), ethPkt.getDestinationMAC
                        ().toString());

                hmArpHosts.put(ethPkt
                        .getSourceMAC().toString(), arpPacket
                );
                //packetOut(packetContext, PortNumber.FLOOD);
                log.info("Prince ==============arpPacket  == : {} ",arpPacket);
                log.info("Prince ================packetContext  : {} ",packetContext);
                return;
            }
            MacAddress srcMac = ethPkt.getSourceMAC();
            MacAddress dstMac = ethPkt.getDestinationMAC();

            if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                log.info("prince ==>> looks like we have got an ipv4 . The source mac : {} and dest mac : {}", srcMac, dstMac);
                Host srcHost = hostService.getHost(HostId.hostId(ethPkt.getSourceMAC()));
                Host dstHost = hostService.getHost(HostId.hostId(ethPkt.getDestinationMAC()));
                log.info("prince ===>> the srcHost is : {}   and the dest Host  {}", srcHost, dstHost);
                log.info("prince ===>>location of the  the srcHost is : {}   and the dest Host  {}", srcHost.location(), dstHost.location());
                Device srcDevice = deviceService.getDevice(srcHost.location().deviceId());
                Device dstDevice = deviceService.getDevice(dstHost.location().deviceId());

                String srcHostMac = srcHost.mac().toString();
                String dstHostMac = dstHost.mac().toString();
                long hostEntryPort = Integer.parseInt(srcHost.location().toString().split("/")[1]);
                long hostExitPort = Integer.parseInt(dstHost.location().toString().split("/")[1]);
                ArrayList<FlowPkt> listFlowPkt = new ArrayList<>();
                FlowPkt flowpkt = new FlowPkt();

                if (srcHost.location().deviceId().equals(dstHost.location().deviceId())){
                    log.info("prince, hoping only one switch in the list ===>>> ");
                    flowpkt.setSrcMac(srcHostMac);
                    flowpkt.setDstMac(dstHostMac);
                    flowpkt.setInPort(hostEntryPort);
                    flowpkt.setOutPort(hostExitPort);
                    flowpkt.setDevice(srcHost.location().deviceId().toString());
                    listFlowPkt.add(flowpkt);

                    FlowPkt revFlowpkt = new FlowPkt(
                            flowpkt.getOutPort(),
                            flowpkt.getInPort(),
                            flowpkt.getDstMac(),
                            flowpkt.getSrcMac(),
                            flowpkt.getDevice());
                    listFlowPkt.add(revFlowpkt);

                    pushFlows(listFlowPkt);

                    return;
                }


                Set<Path> paths = pathService.getPaths(srcHost.location().deviceId(), dstHost.location().deviceId());
                log.info("Prince : ==== paths are : {}", paths);





                flowpkt.setSrcMac(srcHostMac);
                flowpkt.setDstMac(dstHostMac);
                flowpkt.setInPort(hostEntryPort);

                Iterator<Path> pathIterator = paths.iterator();
                int index = 0;
                String nextDevice = "";
                long outPort = 0;
                while(pathIterator.hasNext()){
                    DefaultPath path = (DefaultPath)pathIterator.next();
                    List<Link> links =path.links();
                    for(Link link : links){
                        index++;
                        ConnectPoint srcCp = link.src();
                        ConnectPoint dstCp = link.dst();
                        if (index == 1){
                            flowpkt.setOutPort(srcCp.port().toLong());
                            flowpkt.setDevice(srcCp.deviceId().toString());
                            listFlowPkt.add(flowpkt);

                            FlowPkt revFlowpkt = new FlowPkt(
                                    flowpkt.getOutPort(),
                                    flowpkt.getInPort(),
                                    flowpkt.getDstMac(),
                                    flowpkt.getSrcMac(),
                                    flowpkt.getDevice());
                            listFlowPkt.add(revFlowpkt);

                        } else {

                            flowpkt = new FlowPkt();
                            flowpkt.setSrcMac(srcHostMac);
                            flowpkt.setDstMac(dstHostMac);
                            flowpkt.setInPort(outPort);
                            flowpkt.setOutPort(srcCp.port().toLong());
                            flowpkt.setDevice(srcCp.deviceId().toString());
                            listFlowPkt.add(flowpkt);

                            FlowPkt revFlowpkt = new FlowPkt(
                                    flowpkt.getOutPort(),
                                    flowpkt.getInPort(),
                                    flowpkt.getDstMac(),
                                    flowpkt.getSrcMac(),
                                    flowpkt.getDevice());
                            listFlowPkt.add(revFlowpkt);

                        }
                        nextDevice = dstCp.deviceId().toString();
                        outPort = dstCp.port().toLong();
                    }

                    //########################

                    flowpkt = new FlowPkt();
                    flowpkt.setSrcMac(srcHostMac);
                    flowpkt.setDstMac(dstHostMac);
                    flowpkt.setInPort(outPort);
                    flowpkt.setOutPort(hostExitPort);
                    flowpkt.setDevice(nextDevice);
                    listFlowPkt.add(flowpkt);

                    FlowPkt revFlowpkt = new FlowPkt(
                            flowpkt.getOutPort(),
                            flowpkt.getInPort(),
                            flowpkt.getDstMac(),
                            flowpkt.getSrcMac(),
                            flowpkt.getDevice());
                    listFlowPkt.add(revFlowpkt);
                    //########################

                    log.info("Hi Prince, this is going to be complete list of flows : {}",listFlowPkt);


                    pushFlows(listFlowPkt);


                    log.info("Prince : ==== default paths are : {}", path);
                    break;
                }


            }

            HostId id = HostId.hostId(ethPkt.getDestinationMAC());


            log.info("The source mac : {} and dest mac : {}", srcMac, dstMac);
            log.info("Prince =====> packet  : {}", ethPkt);

//            hostS

        }

        private void pushFlows(ArrayList<FlowPkt> listFlowPkt){
            Iterator<FlowPkt> flowPktIterator = listFlowPkt.iterator();

            while ((flowPktIterator.hasNext())){
                FlowPkt flowPkt = flowPktIterator.next();
                log.info("prince, the pushong packet is  ===>>> {} ", flowPkt.toString());
                TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
                TrafficSelector selector = selectorBuilder
                        .matchInPort(PortNumber.portNumber(flowPkt.getInPort()))
                        .matchEthSrc(MacAddress.valueOf(flowPkt.getSrcMac()))
                        .matchEthDst(MacAddress.valueOf(flowPkt.getDstMac()))
                        .build();
                TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                        .setOutput(PortNumber.portNumber(flowPkt.getOutPort()))
                        .build();
                FlowRule.Builder flowRuleBuilder = DefaultFlowRule.builder()
                        .withPriority(12500)
                        .withSelector(selector)
                        .withTreatment(treatment)
                        .makePermanent()
                        .fromApp(appId);
                flowRuleService.applyFlowRules(flowRuleBuilder.forDevice(DeviceId.deviceId(flowPkt.getDevice())).build());
            }
        }
    }

    class HostListeners implements HostListener{

        @Override
        public void event(HostEvent hostEvent) {
            log.info("Hello prince a host event has come , what it is : {}",hostEvent);
        }
    }

    private void  packetOut(PacketContext context, PortNumber portNumber){
        context.treatmentBuilder().setOutput(portNumber);
        context.send();
    }

}
