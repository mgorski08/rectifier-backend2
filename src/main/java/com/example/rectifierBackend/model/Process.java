package com.example.rectifierBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "process")
public class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @NotNull
    private long bathId;

    private String description;

    private Timestamp startTimestamp;

    private Timestamp stopTimestamp;

    private String insertCode;

    private String chromeType;

    private String elementName;

    private String operation;

    private String drawingNumber;

    private String orderNumber;

    private String monter;

    @ManyToOne
    private User operator;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBathId() {
        return bathId;
    }

    public void setBathId(long bathId) {
        this.bathId = bathId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Timestamp getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(Timestamp stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public String getInsertCode() {
        return insertCode;
    }

    public void setInsertCode(String insertCode) {
        this.insertCode = insertCode;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDrawingNumber() {
        return drawingNumber;
    }

    public void setDrawingNumber(String drawingNumber) {
        this.drawingNumber = drawingNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getMonter() {
        return monter;
    }

    public void setMonter(String monter) {
        this.monter = monter;
    }

    public User getOperator() {
        return operator;
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }

    public String getChromeType() {
        return chromeType;
    }

    public void setChromeType(String chromeType) {
        this.chromeType = chromeType;
    }
}
