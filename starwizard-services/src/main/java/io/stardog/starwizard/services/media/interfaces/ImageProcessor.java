package io.stardog.starwizard.services.media.interfaces;

import io.stardog.starwizard.services.media.data.ImageDimensions;
import io.stardog.starwizard.services.media.data.ImageVersion;

import java.io.File;
import java.util.Collection;

/**
 * An ImageProcessor is capable of processing an image, synchronously or asynchronously, and generating appropriately
 * resized versions.
 */
public interface ImageProcessor {
    ImageDimensions getDimensions(File imageFile);
    void processImage(File imageFile, String storagePath, Collection<ImageVersion> versions);
    void processImageAsync(File imageFile, String storagePath, Collection<ImageVersion> versions);
}
