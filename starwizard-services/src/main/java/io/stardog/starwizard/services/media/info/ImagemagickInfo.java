package io.stardog.starwizard.services.media.info;

import io.stardog.starwizard.services.media.interfaces.ImageInfo;
import org.im4java.core.Info;
import org.im4java.core.InfoException;

import javax.inject.Singleton;

@Singleton
public class ImagemagickInfo implements ImageInfo {
    /**
     * Get the format for the file
     *
     * @param file the file path
     * @return the format
     * @throws InfoException
     */
    public String getImageFormat(String file) throws InfoException {
        try {
            Info imageInfo = new Info(file, true);
            return imageInfo.getImageFormat();
        } catch (Exception e) {
            throw new InfoException("Unable to get file format: " + e.getMessage());
        }
    }
}
