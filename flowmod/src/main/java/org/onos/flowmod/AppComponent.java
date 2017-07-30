/*
 * Copyright 2017-present Open Networking Laboratory
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
package org.onos.flowmod;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected FlowRuleService flowRuleService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();
    private ApplicationId appId;

    @Activate
    protected void activate() {
        log.info("Started");
        appId = coreService.registerApplication("org.onos.flowmod");

        log.info("Started", appId.id());

        packetService.addProcessor(processor, PacketProcessor.director(2));
        log.info("prince ==>> packetService   imlemented");

        Iterable<Device> devices = deviceService.getDevices();
        for (Device device : devices) {
            log.info("Device name : " + device.id());
        }
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        TrafficSelector selector = selectorBuilder.matchInPort(PortNumber.portNumber(1)).build();
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(PortNumber.portNumber(2))
                .build();
        FlowRule.Builder flowRuleBuilder1 = DefaultFlowRule.builder()
                .withPriority(125)
                .withSelector(selector)
                .withTreatment(treatment)
                .makePermanent()
                .fromApp(appId);
        FlowRule.Builder flowRuleBuilder2 = DefaultFlowRule.builder()
                .withPriority(125)
                .withSelector(DefaultTrafficSelector.builder().matchInPort(PortNumber.portNumber(2)).build())
                .withTreatment(DefaultTrafficTreatment.builder().setOutput(PortNumber.portNumber(1)).build())
                .makePermanent()
                .fromApp(appId);
        flowRuleService.applyFlowRules(flowRuleBuilder1.forDevice(
                DeviceId.deviceId("of:0000000000000001")).build(),
                flowRuleBuilder2.forDevice(DeviceId.deviceId("of:0000000000000001")).build());
        flowRuleService.applyFlowRules(flowRuleBuilder1.forDevice(
                DeviceId.deviceId("of:0000000000000002")).build(),
                flowRuleBuilder2.forDevice(DeviceId.deviceId("of:0000000000000002")).build());
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

}

class ReactivePacketProcessor implements PacketProcessor {

    private final Logger log = LoggerFactory.getLogger(getClass());

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

        MacAddress macAddress = ethPkt.getSourceMAC();
        log.info("Prince ==> process packets : src MacAdress : {} , Dest Mac address : {} ", ethPkt.getSourceMAC(),
                ethPkt.getDestinationMAC());

    }
}
