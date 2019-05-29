package com.nngu.fqw.statisticcalculator.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "frames")
public class FrameData {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "Time", nullable = false, unique = true)
    private LocalDateTime time;
    @Column(name = "Protocol", nullable = false)
    private String protocol;
    @Column(name = "Length", nullable = false)
    private Integer length;
    @Column(name = "Source", nullable = false)
    private String source;
    @Column(name = "Destination", nullable = false)
    private String destination;

    public FrameData() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrameData frameData = (FrameData) o;
        return Objects.equals(time, frameData.time) &&
                Objects.equals(source, frameData.source) &&
                Objects.equals(destination, frameData.destination) &&
                Objects.equals(protocol, frameData.protocol) &&
                Objects.equals(length, frameData.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, source, destination, protocol, length);
    }

    @Override
    public String toString() {
        return "FrameData{" +
                "id=" + id +
                ", time=" + time +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", protocol='" + protocol + '\'' +
                ", length=" + length +
                '}';
    }
}
