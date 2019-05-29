package com.nngu.fqw.statisticcalculator.util;

import com.nngu.fqw.statisticcalculator.model.FrameData;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.namednumber.EtherType;
import org.springframework.stereotype.Component;

@Component
public class PacketConverter {

    public FrameData convertToFrameData(EthernetPacket packet) {
        FrameData data = new FrameData();

        EtherType packetType = packet.getHeader().getType();

        if (EtherType.IPV4.equals(packetType)) {
            setIPv4Data(data, (IpV4Packet) packet.getPayload());
        }
        if (EtherType.IPV6.equals(packetType) || EtherType.ARP.equals(packetType)
                || EtherType.PPPOE_DISCOVERY_STAGE.equals(packetType)) {
            setEthernetData(data, packet);
        }
        if (packetType.name().equals("unknown")) {
            setUnknownProtocolData(data, packet);
        }
        if (packetType.name().equals("Length")) {
            setLengthProtocolData(data, packet);
        }

        return data;
    }

    private void setIPv4Data(FrameData data, IpV4Packet payload) {
        data.setProtocol(payload.getHeader().getProtocol().name());
        data.setSource(payload.getHeader().getSrcAddr().getHostAddress());
        data.setDestination(payload.getHeader().getDstAddr().getHostAddress());
    }

    private void setEthernetData(FrameData data, EthernetPacket packet) {
        data.setProtocol(packet.getHeader().getType().name());
        data.setSource(packet.getHeader().getSrcAddr().toString());
        data.setDestination(packet.getHeader().getDstAddr().toString());
    }

    private void setUnknownProtocolData(FrameData data, EthernetPacket packet) {
        data.setProtocol(packet.getHeader().getType().valueAsString());
        data.setSource(packet.getHeader().getSrcAddr().toString());
        data.setDestination(packet.getHeader().getDstAddr().toString());
    }

    private void setLengthProtocolData(FrameData data, EthernetPacket packet) {
        data.setProtocol(packet.getHeader().getType().toString());
        data.setSource(packet.getHeader().getSrcAddr().toString());
        data.setDestination(packet.getHeader().getDstAddr().toString());
    }
}
