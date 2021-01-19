package com.example.rectifierBackend.driver;

import com.example.rectifierBackend.model.Sample;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Random;

@Component("mock")
public class MockRectifierDriver implements RectifierDriver {

    private final Random random = new Random();

    @Override
    public Sample readSample(long bathId) {
        Sample sample = new Sample();
        sample.setStatus(10);
        if(bathId == 10) {
            sample.setCurrent(0);
            sample.setVoltage(0);
        } else {
            sample.setCurrent(10000 + 1000*random.nextGaussian());
            sample.setVoltage(12 + random.nextGaussian());
        }
        sample.setTemperature(30 + random.nextGaussian());
        sample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
        return sample;
    }
}
