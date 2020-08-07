package com.example.rectifierBackend.driver;

import com.example.rectifierBackend.model.Sample;

public interface RectifierDriver {
    Sample readSample(long bathId);
}
