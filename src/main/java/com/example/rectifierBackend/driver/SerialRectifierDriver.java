package com.example.rectifierBackend.driver;

import com.example.rectifierBackend.model.Sample;
import com.fazecast.jSerialComm.SerialPort;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component("serial")
public class SerialRectifierDriver implements RectifierDriver {

    private static final int[] shunts = {0, 4, 4, 4, 4, 4, 4, 8, 8, 8, 6, 6, 6, 6, 4};// kA/60mV
    private static final byte[][] temperatureRequests = {{}, {(byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0xe2}, {(byte) 0x02, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0xd1}, {(byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x84, (byte) 0x00}, {(byte) 0x04, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0xb7}, {(byte) 0x05, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x84, (byte) 0x66}, {(byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x84, (byte) 0x55}, {(byte) 0x07, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0x84}, {(byte) 0x08, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0x7b}, {(byte) 0x09, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x84, (byte) 0xaa}, {(byte) 0x0a, (byte) 0x03, (byte) 0x00, (byte) 0x80,
            (byte) 0x00, (byte) 0x01, (byte) 0x84, (byte) 0x99}, {(byte) 0x0b, (byte) 0x03, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0x5c}, {(byte) 0x0c, (byte) 0x03, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x84, (byte) 0xeb}, {(byte) 0x0d, (byte) 0x03, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0x3a}, {(byte) 0x0e, (byte) 0x03, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x85, (byte) 0x09}};
    private final SerialPort COM2;
    private final SerialPort COM3;

    SerialRectifierDriver() {
        COM2 = SerialPort.getCommPort("COM8");
        COM2.setBaudRate(9600);
        COM2.setNumDataBits(8);
        COM2.setNumStopBits(1);
        COM2.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        COM2.setParity(SerialPort.NO_PARITY);
        COM2.openPort();

        COM3 = SerialPort.getCommPort("COM9");
        COM2.setBaudRate(9600);
        COM2.setNumDataBits(8);
        COM2.setNumStopBits(1);
        COM2.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        COM2.setParity(SerialPort.NO_PARITY);
        COM3.openPort();
    }

    public void close() {
        COM2.closePort();
        COM3.closePort();
    }

    @Override
    public Sample readSample(long bathId) {
        SerialPort comPort;
        if (bathId < 8) {
            comPort = COM2;
        } else {
            comPort = COM3;
        }
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 100, 100);
        byte[] readBuffer = new byte[10];
        byte[] writeBuffer = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) (0xF0 + bathId), (byte) 0x80, 0, 0, 0, 0, 0,
                0};
        comPort.writeBytes(writeBuffer, writeBuffer.length);
        //System.out.println(Arrays.toString(writeBuffer));
        comPort.readBytes(readBuffer, readBuffer.length);
        //System.out.println(Arrays.toString(readBuffer));
        Sample sample = new Sample();
        sample.setStatus(readBuffer[3]);
        sample.setVoltage(((readBuffer[4] & 0xFF) + (double) (readBuffer[5] & 0xFF) / 0x100) * 1.023);
        sample.setCurrent(((readBuffer[6] & 0xFF) + (double) (readBuffer[7] & 0xFF) / 0x100) * shunts[(int)bathId] * 62.5 * 1.023);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] readBufferTemperature = new byte[7];
        comPort.writeBytes(temperatureRequests[(int) bathId], temperatureRequests[(int) bathId].length);
        //System.out.println(Arrays.toString(temperatureRequests[(int)bathId]));
        comPort.readBytes(readBufferTemperature, readBufferTemperature.length);
        //System.out.println(Arrays.toString(readBufferTemperature));
        //System.out.println("");


        sample.setTemperature(((readBufferTemperature[3] & 0xFF) * 0x100 + (readBufferTemperature[4] & 0xFF)) / 10.0);
        sample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return sample;
    }
}
