package io.stardog.starwizard.services.media.interfaces;

import io.stardog.starwizard.services.media.data.ImageDimensions;

import java.io.File;

/**
 * An ImageResizer is capable of resizing an image, given certain parameters, and returning the dimensions of an image.
 */
public interface ImageResizer {
    ImageDimensions getDimensions(File localFile);
    void resizeImage(File sourceFile, File destFile, int width, int height, boolean isSquare);
}
