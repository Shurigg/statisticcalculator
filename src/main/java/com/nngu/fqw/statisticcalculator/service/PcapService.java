package com.nngu.fqw.statisticcalculator.service;

import com.nngu.fqw.statisticcalculator.model.FrameData;
import com.nngu.fqw.statisticcalculator.repo.FrameDataRepo;
import com.nngu.fqw.statisticcalculator.util.PacketConverter;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.EOFException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class PcapService {

    @Autowired
    private PacketConverter packetConverter;

    @Autowired
    private FrameDataRepo repo;

    private final List<FrameData> buffer = new ArrayList<>(10001);

    public void readFile(String filePath) throws PcapNativeException, TimeoutException, NotOpenException {
        long count = 0L;
        PcapHandle handler = Pcaps.openOffline(filePath);
        Timestamp timestamp = null;
        Integer length;

        try {
            Packet packet = handler.getNextPacketEx();
            while (packet != null) {
                if (timestamp == null || !timestamp.equals(handler.getTimestamp())) {
                    timestamp = handler.getTimestamp();
                    length = handler.getOriginalLength();
                    savePacket(packet, timestamp, length);
                }
                count++;
                if (count % 1_000_000 == 0) {
                    System.out.println(count);
                }
                try {
                    packet = handler.getNextPacketEx();
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (EOFException ex) {
            if (!buffer.isEmpty()) {
                repo.saveAll(buffer);
                buffer.clear();
            }
        }
    }

    private void savePacket(Packet packet, Timestamp timestamp, Integer length) {
        if (packet instanceof EthernetPacket) {
            FrameData data = packetConverter.convertToFrameData((EthernetPacket) packet);
            data.setTime(timestamp.toLocalDateTime());
            data.setLength(length);

            if (data.getProtocol() == null || data.getSource() == null || data.getDestination() == null) {
                System.out.println();
            }

            buffer.add(data);
        }

        if (buffer.size() >= 10_000) {
            repo.saveAll(buffer);
            buffer.clear();
        }
    }
}
