package io.stardog.starwizard.services.media.interfaces;

import org.im4java.core.InfoException;

public interface ImageInfoInterface {
    String getImageFormat(String file) throws InfoException;
}
