package com.example.rectifierBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "sample")
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @JsonIgnore
    @ManyToOne
    private Process process;

    private long bathId;

    private double voltage;
    private double current;
    private double temperature;
    private Timestamp timestamp;
    private int status;

    public Sample() {
        status = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public long getBathId() {
        return bathId;
    }

    public void setBathId(long bathId) {
        this.bathId = bathId;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
