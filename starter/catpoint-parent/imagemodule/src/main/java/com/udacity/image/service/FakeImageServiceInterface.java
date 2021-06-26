package com.udacity.image.service;

import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;

import java.awt.image.BufferedImage;

public interface FakeImageServiceInterface {
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);
}
